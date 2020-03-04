package org.matsim.prepare.population.reLocation;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.facilities.*;

import java.net.MalformedURLException;
import java.net.URL;

public class GenerateFacilitiesFromPlans {

    public static void main(String[] args) throws MalformedURLException {

        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        createFacilities(scenario);

    }

    public static void createFacilities(Scenario scenario) throws MalformedURLException {
        ActivityFacilities activityFacilities = scenario.getActivityFacilities();
        ActivityFacilitiesFactory activityFacilitiesFactory = new ActivityFacilitiesFactoryImpl();

        PopulationReader populationReader = new PopulationReader(scenario);
        populationReader.readURL(new URL("https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.5-10pct/input/berlin-v5.5-10pct.plans.xml.gz"));

        Population population = scenario.getPopulation();
        long idforFadcilities = 1;

        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                for (PlanElement planElement : plan.getPlanElements()) {
                    if (planElement instanceof Activity) {
                        Activity activity = (Activity) planElement;
                        if (activity.getType().contains("leisure")) {
                            Id<ActivityFacility> id = Id.create(idforFadcilities++, ActivityFacility.class);
                            Coord coord = activity.getCoord();
                            ActivityFacility activityFacility = activityFacilitiesFactory.createActivityFacility(id, coord);
                            for (long ii = 600; ii <= 97200; ii += 600) {
                                String type = "shopping_" + ii + ".0";
                                activityFacility.addActivityOption(activityFacilitiesFactory.createActivityOption(type));
                            }
                            activityFacilities.addActivityFacility(activityFacility);
                        }
                        if (activity.getType().contains("shopping")) {
                            Id<ActivityFacility> id = Id.create(idforFadcilities++, ActivityFacility.class);
                            Coord coord = activity.getCoord();
                            ActivityFacility activityFacility = activityFacilitiesFactory.createActivityFacility(id, coord);
                            for (long ii = 600; ii <= 97200; ii += 600) {
                                String type = "leisure_" + ii + ".0";
                                activityFacility.addActivityOption(activityFacilitiesFactory.createActivityOption(type));
                            }
                            activityFacilities.addActivityFacility(activityFacility);
                        }
                    }
                }
            }
        }

        new FacilitiesWriter(activityFacilities).write("allActivitiesOwnFacilities.xml");
        System.out.println("Done");
    }
}
