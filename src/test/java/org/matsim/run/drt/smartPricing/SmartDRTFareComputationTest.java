package org.matsim.run.drt.smartPricing;

import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.run.drt.RunDrtOpenBerlinScenario;
import org.matsim.testcases.MatsimTestUtils;

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
                "--config:strategy.fractionOfIterationsToDisableInnovation", "0.8",
                "--config:controler.runId", "testSmartDrtPriceRun",
                "--config:controler.lastIteration", "3",
                "--config:controler.outputDirectory", utils.getOutputDirectory()};

        Config config = RunDrtOpenBerlinScenario.prepareConfig(args);
        ConfigUtils.addOrGetModule(config, SmartDrtFareConfigGroup.class);


        Scenario scenario = RunDrtOpenBerlinScenario.prepareScenario(config);

        Controler controler = RunDrtOpenBerlinScenario.prepareControler(scenario);
        controler.addOverridingModule(new SmartDRTFareModule());
        controler.run();
    }
}