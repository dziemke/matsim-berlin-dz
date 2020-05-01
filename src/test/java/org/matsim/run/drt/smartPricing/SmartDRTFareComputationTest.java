package org.matsim.run.drt.smartPricing;

import ch.sbb.matsim.config.SwissRailRaptorConfigGroup;
import org.checkerframework.common.value.qual.ArrayLen;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.drtSpeedUp.DrtSpeedUpConfigGroup;
import org.matsim.drtSpeedUp.DrtSpeedUpModule;
import org.matsim.run.RunBerlinScenario;
import org.matsim.run.drt.RunDrtOpenBerlinScenario;
import org.matsim.testcases.MatsimTestUtils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author : zmeng
 * @date : 18.Feb
 */
public class SmartDRTFareComputationTest {
    @Rule
    public MatsimTestUtils utils = new MatsimTestUtils();

    @Test
    public void testSmartDrtPriceRun() {
        String configFilename = "scenarios/berlin-v5.5-1pct/input/drt/berlin-drt-v5.5-1pct.config.xml";
        final String[] args = {configFilename,
                "--config:plans.inputPlansFile", "../../../../test/input/drt/drt-test-agents.xml",
                "--config:strategy.fractionOfIterationsToDisableInnovation", "0.0",
                "--config:controler.runId", "testSmartDrtPriceRun",
                "--config:controler.lastIteration", "2",
                "--config:swissRailRaptor.useIntermodalAccessEgress","false",
                "--config:controler.outputDirectory", utils.getOutputDirectory()};

        Config config = RunDrtOpenBerlinScenario.prepareConfig(args);

        ConfigUtils.addOrGetModule(config, SmartDrtFareConfigGroup.class);
        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);

        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);
        controler.addOverridingModule(new SmartDRTFareModule());
        controler.run();
    }

    @Test
    public void test1pctScenario(){
        String configFilename = "/Users/zhuoxiaomeng/IdeaProjects/matsim-berlin/scenarios/berlin-v5.5-1pct/input/drt/berlin-drt-v5.5-1pct.config.xml";
        final String[] args = {configFilename,
                "--config:strategy.fractionOfIterationsToDisableInnovation", "1",
                "--config:controler.runId", "test1pctScenario",
                "--config:controler.lastIteration", "4",
                "--config:swissRailRaptor.useIntermodalAccessEgress","false",
                "--config:controler.outputDirectory", utils.getOutputDirectory()};

        Config config = RunDrtOpenBerlinScenario.prepareConfig(args);

        ConfigUtils.addOrGetModule(config, SmartDrtFareConfigGroup.class);
        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);

        for( Person person : scenario.getPopulation().getPersons().values() ){
            person.getPlans().removeIf( (plan) -> plan!=person.getSelectedPlan() ) ;
        }

        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);
        controler.addOverridingModule(new SmartDRTFareModule());
        controler.run();
    }

    @Test
    public void test1pctScenarioWithReward(){
        String configFilename = "/Users/zhuoxiaomeng/IdeaProjects/matsim-berlin/scenarios/berlin-v5.5-1pct/input/drt/berlin-drt-v5.5-1pct.config.xml";
        final String[] args = {configFilename,
                "--config:plans.inputPlansFile","/Users/zhuoxiaomeng/Desktop/test1pctScenario.output_plans.xml.gz",
                "--config:strategy.fractionOfIterationsToDisableInnovation", "1",
                "--config:controler.runId", "test1pctScenario",
                "--config:controler.lastIteration", "4",
                "--config:swissRailRaptor.useIntermodalAccessEgress","false",
                "--config:berlinExperimental.populationDownsampleFactor", "1.0",
                "--config:controler.outputDirectory", utils.getOutputDirectory()};

        Config config = RunDrtOpenBerlinScenario.prepareConfig(args);

        ConfigUtils.addOrGetModule(config, SmartDrtFareConfigGroup.class);
        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);

        for( Person person : scenario.getPopulation().getPersons().values() ){
            person.getPlans().removeIf( (plan) -> plan!=person.getSelectedPlan() ) ;
        }

        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);
        controler.addOverridingModule(new SmartDRTFareModule());
        controler.run();
    }

    @Test
    public void test0ptTime(){
        String configFilename = "/Users/zhuoxiaomeng/IdeaProjects/matsim-berlin/scenarios/berlin-v5.5-1pct/input/drt/berlin-drt-v5.5-1pct.config.xml";
        final String[] args = {configFilename,
                "--config:strategy.fractionOfIterationsToDisableInnovation", "0.8",
                "--config:controler.runId", "test1pctScenario",
                "--config:controler.lastIteration", "4",
                "--config:swissRailRaptor.useIntermodalAccessEgress","false",
                "--config:controler.outputDirectory", utils.getOutputDirectory()};

        Config config = RunDrtOpenBerlinScenario.prepareConfig(args);

        ConfigUtils.addOrGetModule(config, SmartDrtFareConfigGroup.class);
        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);
        List<Id<Person>> personIds = new ArrayList<>();
        Id<Person> id = Id.create(1,Person.class);
        for (Id<Person> personId:scenario.getPopulation().getPersons().keySet()){
            if(!personId.equals(id)){
                personIds.add(personId);
            }
        }
        personIds.forEach(personId -> scenario.getPopulation().removePerson(personId));

        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);
        controler.addOverridingModule(new SmartDRTFareModule());
        controler.run();
    }


    @Test
    public void test1pctScenarioDrtSpeedUp(){
        String configFilename = "/Users/zhuoxiaomeng/IdeaProjects/matsim-berlin/scenarios/berlin-v5.5-1pct/input/drt/berlin-drt-v5.5-1pct.config.xml";
        final String[] args = {configFilename,
                "--config:strategy.fractionOfIterationsToDisableInnovation", "0.8",
                "--config:controler.runId", "test1pctScenario",
                "--config:controler.lastIteration", "4",
                "--config:swissRailRaptor.useIntermodalAccessEgress","false",
                "--config:controler.outputDirectory", utils.getOutputDirectory()};

        Config config = RunDrtOpenBerlinScenario.prepareConfig(args, new DrtSpeedUpConfigGroup());

        SwissRailRaptorConfigGroup swissRailRaptorConfigGroup = ConfigUtils.addOrGetModule(config, SwissRailRaptorConfigGroup.class);
        swissRailRaptorConfigGroup.setUseIntermodalAccessEgress(false);

        ConfigUtils.addOrGetModule(config, SmartDrtFareConfigGroup.class);
        DrtSpeedUpModule.adjustConfig(config);

        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);

        for( Person person : scenario.getPopulation().getPersons().values() ){
            person.getPlans().removeIf( (plan) -> plan!=person.getSelectedPlan() ) ;
        }

        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);
        controler.addOverridingModule(new SmartDRTFareModule());
        controler.addOverridingModule(new DrtSpeedUpModule());
        controler.run();

        RunBerlinScenario.runAnalysis(controler);
    }
}