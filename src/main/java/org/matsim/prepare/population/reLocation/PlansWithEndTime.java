package org.matsim.prepare.population.reLocation;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class PlansWithEndTime {

    public static void main(String[] args) throws MalformedURLException {

        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);

        PopulationReader reader = new PopulationReader(scenario);
        reader.readURL(new URL("https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.5-1pct/input/berlin-v5.5-1pct.plans_uncalibrated.xml.gz"));

        Population population = scenario.getPopulation();

        for (Person person : population.getPersons().values()) {
            Plan plan = person.getSelectedPlan();
            double preTime = 0;
            for (PlanElement planElement : plan.getPlanElements()) {
                if (planElement instanceof Activity) {
                    Activity activity = (Activity) planElement;
                    if (activity.getEndTime() > 0) {
                        preTime = activity.getEndTime();
                    } else {
                        activity.setMaximumDuration(0);
                        activity.setEndTime(preTime + 1800);
                    }
                }

            }
        }
        new PopulationWriter(population).write("D:/Arbeit/Berlin/ReLocation/angepassteplans/plans.xml.gz");

        
    }


}
