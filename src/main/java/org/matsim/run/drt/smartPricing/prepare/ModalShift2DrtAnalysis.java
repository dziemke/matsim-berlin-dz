package org.matsim.run.drt.smartPricing.prepare;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.Population;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.router.MainModeIdentifier;
import org.matsim.core.router.MainModeIdentifierImpl;
import org.matsim.core.router.TripStructureUtils;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : zmeng
 * @date :
 */
public class ModalShift2DrtAnalysis {
    String mode;
    Map<String, Integer> fromOtherMode2ThisMode = new HashMap<>();
    Map<String,Integer> fromThisMode2otherMode = new HashMap<>();
    Population originalPop;
    Population outputPop;

    public ModalShift2DrtAnalysis(String mode, Population originalPop, Population outputPop){
        this.mode = mode;
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
        fromOtherMode2ThisMode.forEach((a, b) -> System.out.println(a + "," +b));
        fromThisMode2otherMode.forEach((a, b) -> System.out.println(a + "," +b));
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
        MainModeIdentifier mainModeIdentifier = new MainModeIdentifierImpl();
        String originalMode = mainModeIdentifier.identifyMainMode(originalTrip.getTripElements());
        String outputMode = mainModeIdentifier.identifyMainMode(outputTrip.getTripElements());


        if(originalMode.equals(outputMode)){
            return;
        } else if(outputMode.equals(mode)){
            if(!this.fromOtherMode2ThisMode.containsKey(originalMode)){
                this.fromOtherMode2ThisMode.put(originalMode,0);
            }
            this.fromOtherMode2ThisMode.put(originalMode, this.fromOtherMode2ThisMode.get(originalMode)+1);
        } else if (originalMode.equals(mode)){
            if(!this.fromThisMode2otherMode.containsKey(outputMode)){
                this.fromThisMode2otherMode.put(outputMode,0);
            }
            this.fromThisMode2otherMode.put(outputMode, this.fromThisMode2otherMode.get(outputMode) - 1);
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
        Population outputPop = getPopulation("EPSG:31468","/Users/zhuoxiaomeng/Forschung/berlin-drt-v5.5-1pct_drt1-3s4.output_plans.xml.gz");
        Population originalPop = getPopulation("GK4","/Users/zhuoxiaomeng/Forschung/berlin-v5.5-1pct.output_plans.xml");
        int a = 0;
        int b = 0;
        for(Person person: outputPop.getPersons().values()){
            var trips = TripStructureUtils.getTrips(person.getSelectedPlan());
            var drtTrips = trips.stream().filter(trip ->  new MainModeIdentifierImpl().identifyMainMode(trip.getTripElements()).equals("drt")).collect(Collectors.toList()).size();

            a+= trips.size();
            b+= drtTrips;

        }
        System.out.println(a);
        System.out.println(b);
        ModalShift2DrtAnalysis modalShift2DrtAnalysis = new ModalShift2DrtAnalysis(TransportMode.drt,originalPop,outputPop);
        modalShift2DrtAnalysis.analysis();
    }
}
