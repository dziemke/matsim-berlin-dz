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
        String dic = args[0];
        Config config = ConfigUtils.loadConfig(dic + "berlin-drt-v5.5-1pct.output_config.xml");
        config.controler().setOutputDirectory(dic);
        config.plans().setInputFile(dic + "berlin-drt-v5.5-1pct.output_plans.xml.gz");
        Scenario scenario = ScenarioUtils.loadScenario(config);
        TripsInfoCollector tripsInfoCollector = new TripsInfoCollector();
        tripsInfoCollector.setScenario(scenario);
        tripsInfoCollector.reset(1);
        String eventsFile= dic + "/berlin-drt-v5.5-1pct.output_events.xml.gz";
        EventsManager eventsManager = EventsUtils.createEventsManager();
        eventsManager.addHandler(tripsInfoCollector);
        MatsimEventsReader reader = new MatsimEventsReader(eventsManager);
        reader.readFile(eventsFile);
        tripsInfoCollector.collect();



    }
}
