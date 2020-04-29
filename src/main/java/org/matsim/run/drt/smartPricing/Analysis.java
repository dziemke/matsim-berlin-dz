package org.matsim.run.drt.smartPricing;


import com.fasterxml.jackson.databind.introspect.TypeResolutionContext;
import org.matsim.analysis.detailedPersonTripAnalysis.BasicPersonTripAnalysisHandler;
import org.matsim.analysis.detailedPersonTripAnalysis.PersonTripAnalysis;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.Event;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.events.EventsUtils;
import org.matsim.core.events.MatsimEventsReader;
import org.matsim.core.events.handler.BasicEventHandler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.run.drt.smartPricing.prepare.TripsInfoCollector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author : zmeng
 * @date :
 */
public class Analysis implements BasicEventHandler {
    List<Integer> ids = new ArrayList<>();

    public Analysis(List<Integer> list) {
        ids = list;
    }

    @Override
    public void reset(int iteration) {

    }

    @Override
    public void handleEvent(Event event) {
        for (Integer integer : ids) {
            if (event.toString().contains(String.valueOf(integer))) {
                System.out.println(event.toString());
                break;
            }
        }
    }

    public static void main(String[] args) {

        Config config = ConfigUtils.loadConfig("/net/ils3/meng/smartDrtPricing/10pct-Berlin-Scenario/10pct-base-speed-up/berlin-drt-v5.5-10pct-smartPricing-noPenalty.config.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        config.controler().setOutputDirectory(args[0]);
        TripsInfoCollector tripsInfoCollector = new TripsInfoCollector();
        tripsInfoCollector.setScenario(scenario);
        tripsInfoCollector.reset(1);
        String eventsFile= args[1];
        EventsManager eventsManager = EventsUtils.createEventsManager();
        eventsManager.addHandler(tripsInfoCollector);
        MatsimEventsReader reader = new MatsimEventsReader(eventsManager);
        reader.readFile(eventsFile);
        tripsInfoCollector.collect();



    }
}
