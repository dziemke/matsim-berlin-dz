package org.matsim.run.drt.smartPricing.prepare;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.ActivityStartEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.ActivityStartEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : zmeng
 * @date : 1.April
 */
public class TripsInfoCollector implements PersonArrivalEventHandler, ActivityEndEventHandler, ActivityStartEventHandler {
    @Inject
    private Scenario scenario;

    Map<Id<Person>,List<TripsInfo>> personId2TripsInfo = new HashMap<>();
    Map<Id<Person>,TripsInfo> temPersonId2TripsInfo = new HashMap<>();

    @Override
    public void reset(int iteration) {
        for (Id<Person> personId: scenario.getPopulation().getPersons().keySet()){
            this.personId2TripsInfo.put(personId,new ArrayList<>());
        }
    }


    @Override
    public void handleEvent(ActivityEndEvent activityEndEvent) {
        // skip unreal activity and unreal Person
        if(!activityEndEvent.getActType().contains("interaction")){
            Id<Person> personId = activityEndEvent.getPersonId();
            if(personId2TripsInfo.containsKey(personId)){
                if(temPersonId2TripsInfo.containsKey(personId)){
                    try {
                        throw new Exception("person with id: " + personId + " has unfinisched trips");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                temPersonId2TripsInfo.put(personId,new TripsInfo(activityEndEvent));
            }
        }
    }

    @Override
    public void handleEvent(ActivityStartEvent activityStartEvent) {
        if(!activityStartEvent.getActType().contains("interaction")){
            Id<Person> personId = activityStartEvent.getPersonId();
            if(personId2TripsInfo.containsKey(personId)){
                if(temPersonId2TripsInfo.containsKey(personId)){
                    TripsInfo tripsInfo = temPersonId2TripsInfo.get(personId);
                    try {
                        tripsInfo.activityStartEvent = activityStartEvent;
                        tripsInfo.process();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    this.personId2TripsInfo.get(personId).add(tripsInfo);
                    this.temPersonId2TripsInfo.remove(personId);
                } else {
                    try {
                        throw new Exception("person with id: "+ personId+" has unregistered trip");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public void collect(){
        for (Id<Person> personId :
                temPersonId2TripsInfo.keySet()) {
            TripsInfo tripsInfo = temPersonId2TripsInfo.get(personId);
            try {
                tripsInfo.process();
            } catch (Exception e) {
                e.printStackTrace();
            }
            this.personId2TripsInfo.get(personId).add(tripsInfo);
        }
        String runOutputDirectory = this.scenario.getConfig().controler().getOutputDirectory();
        if (!runOutputDirectory.endsWith("/")) runOutputDirectory = runOutputDirectory.concat("/");

        String fileName = runOutputDirectory + this.getClass().getName() + ".csv";
        File file = new File(fileName);

        try {
            var bw = new BufferedWriter(new FileWriter(file));
            bw.write("personId,trip_num,departureLink,arrivalLink,departureTime,arrivalTime,mode");
            for (Id<Person> personId : this.personId2TripsInfo.keySet()) {
                int a =1;
                for(TripsInfo tripsInfo : this.personId2TripsInfo.get(personId)){
                    bw.newLine();
                    bw.write(personId + "," +
                            a + "," +
                            tripsInfo.depLink + "," +
                            tripsInfo.arrLink + "," +
                            tripsInfo.depTime + "," +
                            tripsInfo.arrTime + "," +
                            tripsInfo.mode);
                    a++;
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void handleEvent(PersonArrivalEvent personArrivalEvent) {
        if(temPersonId2TripsInfo.containsKey(personArrivalEvent.getPersonId())){
            temPersonId2TripsInfo.get(personArrivalEvent.getPersonId()).personArrivalEvents.add(personArrivalEvent);
        }

    }

    private class TripsInfo {
        private List<PersonArrivalEvent> personArrivalEvents;
        private ActivityEndEvent activityEndEvent;
        private ActivityStartEvent activityStartEvent;
        private Id<Link> depLink;
        private Id<Link> arrLink;
        private double depTime;
        private double arrTime;
        private String mode;

        public TripsInfo(ActivityEndEvent activityEndEvent) {
            this.activityEndEvent = activityEndEvent;
            this.personArrivalEvents = new ArrayList<>();
        }

        public void process() throws Exception {
            this.depTime = this.activityEndEvent.getTime();
            this.depLink = this.activityEndEvent.getLinkId();
            int size = this.personArrivalEvents.size();
            if(this.activityStartEvent == null){
                this.arrTime = -1;
                this.arrLink = Id.createLinkId("-1");
                this.mode="ab";
            } else {
                this.arrTime = this.personArrivalEvents.get(size-1).getTime();
                this.arrLink = this.personArrivalEvents.get(size-1).getLinkId();
                this.mode = getMode();
            }
        }

        private String getMode() throws Exception {
                List<String> modes = personArrivalEvents.stream().map(personArrivalEvent -> personArrivalEvent.getLegMode()).collect(Collectors.toList());
                if(modes.size() ==1){
                    return  modes.get(0);
                } else {
                    modes = modes.stream().filter(mode -> !mode.contains("walk")).distinct().collect(Collectors.toList());
                    if(modes.size() != 1){
                        throw new Exception("check the modes: "+modes.toString());
                    } else {
                        return modes.get(0);
                    }
                }
        }
    }

    public void setScenario(Scenario scenario) {
        this.scenario = scenario;
    }
}
