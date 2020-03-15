package org.matsim.run.drt.smartPricing;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.run.drt.RunDrtOpenBerlinScenario;

/**
 * @author : zmeng
 * @date :
 */
public class RunDrtOpenBerlinScenarioWithSmartDrtFare {
    private static final Logger log = Logger.getLogger(RunDrtOpenBerlinScenarioWithSmartDrtFare.class);
    public static void main(String[] args) {

        for (String arg : args) {
            log.info( arg );
        }

        if ( args.length==0 ) {
            args = new String[] {"scenarios/berlin-v5.5-1pct/input/drt/berlin-drt-v5.5-1pct.config.xml"}  ;
        }

        Config config = RunDrtOpenBerlinScenario.prepareConfig(args);

        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = ConfigUtils.addOrGetModule(config, SwissRailRaptorConfigGroup.class);
        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(false);

        ConfigUtils.addOrGetModule(config, SmartDrtFareConfigGroup.class);

        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);

        for( Person person : scenario.getPopulation().getPersons().values() ){
            person.getPlans().removeIf( (plan) -> plan!=person.getSelectedPlan() ) ;
        }

        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);
        controler.addOverridingModule(new SmartDRTFareModule());
        controler.run();
    }
}
