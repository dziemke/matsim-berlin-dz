package org.matsim.prepare.population;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.ArrayList;
import java.util.List;

public class SelectedPlans {

    public static void main(String[] args) {

//        String FileName = args[0];
        String FileName = "D:/Bachelor/randomReLocation/preparingPlans/randomReLocation.zone_2000.xml.gz";

        Scenario scenario = ScenarioUtils.loadScenario(ConfigUtils.createConfig());
        PopulationReader populationReader = new PopulationReader(scenario);
        populationReader.readFile(FileName);

        Population population = scenario.getPopulation();

        for (Person person : population.getPersons().values()) {
            List<Plan> plans = new ArrayList<>();
            for (Plan plan : person.getPlans()) {
                if (plan.equals(person.getSelectedPlan())) {
                    continue;
                }
                plans.add(plan);
            }
            for (Plan plan : plans) {
                person.removePlan(plan);
            }
        }

        System.out.println("writing");
//        new PopulationWriter(outPutPopulation).write(args[1]);
        new PopulationWriter(population).write("D:/Bachelor/randomReLocation/preparingPlans/2000.xml.gz");
        System.out.println("Done");


    }

}
