package org.matsim.run.drt.smartPricing;

import com.jogamp.common.util.ArrayHashSet;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.RoutingModeMainModeIdentifier;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.run.drt.OpenBerlinIntermodalPtDrtRouterAnalysisModeIdentifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author : zmeng
 * @date :
 */
public class PlansAnalysis {
    public static void main(String[] args) {
        String runOutputDirectory;
        Config config;

            runOutputDirectory = "/Users/zhuoxiaomeng/testFolder/";
            config = ConfigUtils.createConfig();
            config.global().setCoordinateSystem("EPSG:25832");
            config.plans().setInputFile(runOutputDirectory + "baseCase_021.output_plans.xml.gz");

        Scenario scenario = ScenarioUtils.loadScenario(config);

        String fileName = runOutputDirectory + "planAnalysis2.csv";
        File file = new File(fileName);

        MainModeIdentifier mainModeIdentifier = new OpenBerlinIntermodalPtDrtRouterAnalysisModeIdentifier();

        try {
            var bw = new BufferedWriter(new FileWriter(file));
            bw.write("personId;departureLink;arrivalLink;mode;mainMode");
            for (Id<Person> personId : scenario.getPopulation().getPersons().keySet()) {
                Plan plan = scenario.getPopulation().getPersons().get(personId).getSelectedPlan();
                var trips = TripStructureUtils.getTrips(plan);
                for(TripStructureUtils.Trip trip : trips){
                    List<String> modes = new ArrayList<>();
                    for (PlanElement planElement:
                         trip.getTripElements()) {
                        if(planElement instanceof Leg){
                            modes.add(((Leg) planElement).getMode());
                        }
                    }
                    bw.newLine();
                    bw.write(personId + ";" +
                           trip.getOriginActivity().getLinkId()+ ";" +
                            trip.getDestinationActivity().getLinkId()+ ";" +
                            modes + ";" +
                            new RoutingModeMainModeIdentifier().identifyMainMode(trip.getTripElements()));
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
