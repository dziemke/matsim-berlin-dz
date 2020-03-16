package org.matsim.run.drt.smartPricing.prepare;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.api.core.v01.population.PopulationWriter;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.run.drt.OpenBerlinIntermodalPtDrtRouterAnalysisModeIdentifier;

import java.util.HashMap;
import java.util.Map;

/**
 * @author : zmeng
 * @date :
 */
public class ModalShift2DrtAnalysis {
    Map<Map<String,String>,Integer> mode2mode2num = new HashMap<>();
    Population originalPop;
    Population outputPop;

    public ModalShift2DrtAnalysis(Population originalPop, Population outputPop){
        this.originalPop = originalPop;
        this.outputPop = outputPop;
    }

    void analysis() throws Exception {

        for (Id<Person> personId :
                originalPop.getPersons().keySet()) {
            var originalPlan = originalPop.getPersons().get(personId).getSelectedPlan();
            var outputPlan = outputPop.getPersons().get(personId).getSelectedPlan();
            comparePlan(originalPlan,outputPlan);
        }

        mode2mode2num.forEach((mode2mode,num) -> System.out.println(mode2mode +" "+ num));
    }

    private void comparePlan(Plan originalPlan, Plan outputPlan) throws Exception {
        var originalTrips = TripStructureUtils.getTrips(originalPlan);
        var outputTrips = TripStructureUtils.getTrips(outputPlan);

        if(originalTrips.size() != outputTrips.size()){
            throw new Exception("originalPlan's tripNumber : "+ originalTrips.size() + "is not equal with outputPlan's tripNumber : "+ outputTrips.size());
        } else {
            for (int i = 0; i < originalTrips.size(); i++) {
                var originalTrip = originalTrips.get(i);
                var outputTrip = outputTrips.get(i);

                if(!originalTrip.getOriginActivity().getLinkId().equals(outputTrip.getOriginActivity().getLinkId())){
                    throw new Exception("These can be two different trips");
                } else {
                    compareTrip(originalTrip, outputTrip);
                }
            }
        }
    }
    private void compareTrip(TripStructureUtils.Trip originalTrip, TripStructureUtils.Trip outputTrip){
        MainModeIdentifier mainModeIdentifier = new OpenBerlinIntermodalPtDrtRouterAnalysisModeIdentifier();
        String originalMode = mainModeIdentifier.identifyMainMode(originalTrip.getTripElements());
        String outputMode = mainModeIdentifier.identifyMainMode(outputTrip.getTripElements());

        System.out.println(originalMode + "," + outputMode);

        Map<String,String> mode2mode = new HashMap<>();
        mode2mode.put(originalMode,outputMode);
        if(!this.mode2mode2num.containsKey(mode2mode)){
            this.mode2mode2num.put(mode2mode,1);
        } else {
            this.mode2mode2num.put(mode2mode, this.mode2mode2num.get(mode2mode) + 1);
        }
    }

    private static Population getPopulation(String geo, String plansFile){
        Config config = ConfigUtils.createConfig();
        config.global().setCoordinateSystem(geo);
        config.plans().setInputFile(plansFile);
        Scenario scenario = ScenarioUtils.loadScenario(config);
        return scenario.getPopulation();
    }

    public static void main(String[] args) throws Exception {
        Population originalPop = getPopulation("GK4","originalPop");

        Population outputPop = getPopulation("EPSG:31468","outputPop");
        //outputPopWriter(outputPop,"outputPop");
//
        ModalShift2DrtAnalysis modalShift2DrtAnalysis = new ModalShift2DrtAnalysis(originalPop,outputPop);
        modalShift2DrtAnalysis.analysis();
    }

    private static void outputPopWriter(Population population,String string) {
        Population outputPopulation = ScenarioUtils.createScenario(ConfigUtils.createConfig()).getPopulation();
        for (Person person : population.getPersons().values()) {

            Person outPerson = outputPopulation.getFactory().createPerson(person.getId());

            Plan selectedPlan = person.getSelectedPlan();

            Plan outputPlan = outputPopulation.getFactory().createPlan();
            PopulationUtils.copyFromTo(selectedPlan, outputPlan);
            outPerson.addPlan(outputPlan);
            outputPopulation.addPerson(outPerson);

        }
        new PopulationWriter(outputPopulation).write(string);
    }
}
