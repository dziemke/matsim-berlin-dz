package org.matsim.run.frozenTastes;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.run.RunBerlinScenarioTest;
import org.matsim.run.debug.RunBerlinScenarioDebug;
import org.matsim.testcases.MatsimTestUtils;

public class RunBerlinFrozenTastesScenarioTest {

    private static final Logger log = Logger.getLogger( RunBerlinScenarioTest.class ) ;

    @Rule
    public MatsimTestUtils utils = new MatsimTestUtils() ;

    @Test
    public final void aTestTest() {
        // a dummy test to satisfy the matrix build by travis.
        log.info( "Done with aTestTest"  );
        log.info("") ;
        Assert.assertTrue( true );
    }

    @Test
    public final void endDurationTest () {
        try {
            String configFilename = "test/input/frozenTastes/berlin-v5.5-1pct.config.xml";
            final String[] args = {configFilename,
                    "--config:controler.runId", "test-run-ID"};

            Config config = RunBerlinScenarioDebug.prepareConfig( args );
            config.controler().setOutputDirectory(utils.getOutputDirectory());
            config.plans().setInputFile("test-Person-noEndTime.xml");
            config.facilities().setInputFile("twoFacilities.xml");
            config.planCalcScore().addActivityParams(
                    new PlanCalcScoreConfigGroup.ActivityParams("car interaction").setScoringThisActivityAtAll(false)
            );
            Scenario scenario = RunBerlinScenarioDebug.prepareScenario(config);
            Controler controler = RunBerlinScenarioDebug.prepareControler(scenario);

            controler.run();

            Assert.assertEquals("Wrong parameter from command line", "test-run-ID", config.controler().getRunId());
            log.info( "Done with endDurationTest"  );
            log.info("") ;

        } catch ( Exception ee ) {
            throw new RuntimeException(ee) ;
        }
    }

    @Test
    public final void backwardPathTest () {
        try {
            String configFilename = "test/input/frozenTastes/berlin-v5.5-1pct.config.xml";
            final String[] args = {configFilename,
                    "--config:controler.runId", "test-run-ID"};

            Config config = RunBerlinScenarioDebug.prepareConfig(args);
            config.controler().setOutputDirectory(utils.getOutputDirectory());
            config.plans().setInputFile("test-Person.xml");
            config.facilities().setInputFile("twoFacilities.xml");
            config.planCalcScore().addModeParams(new PlanCalcScoreConfigGroup.ModeParams("car").setMarginalUtilityOfDistance(-5));
            config.planCalcScore().addActivityParams(
                    new PlanCalcScoreConfigGroup.ActivityParams("car interaction").setScoringThisActivityAtAll(false)
            );
            Scenario scenario = RunBerlinScenarioDebug.prepareScenario(config);
            Controler controler = RunBerlinScenarioDebug.prepareControler(scenario);

            controler.run();

            Assert.assertEquals("Wrong parameter from command line", "test-run-ID", config.controler().getRunId());
            log.info("Done with backwardPathTest");
            log.info("");

        } catch (Exception ee) {
            throw new RuntimeException(ee);
        }
    }

    @Test
    public final void modeInteractionTest () {
        try {
            String configFilename = "test/input/frozenTastes/berlin-v5.5-1pct.config.xml";
            final String[] args = {configFilename,
                    "--config:controler.runId", "test-run-ID"};

            Config config = RunBerlinScenarioDebug.prepareConfig(args);
            config.controler().setOutputDirectory(utils.getOutputDirectory());
            config.plans().setInputFile("test-Person.xml");
            config.facilities().setInputFile("twoFacilities.xml");

            Scenario scenario = RunBerlinScenarioDebug.prepareScenario(config);
            Controler controler = RunBerlinScenarioDebug.prepareControler(scenario);

            controler.run();

            Assert.assertEquals("Wrong parameter from command line", "test-run-ID", config.controler().getRunId());
            log.info("Done with modeInteractionTest");
            log.info("");

        } catch (Exception ee) {
            throw new RuntimeException(ee);
        }
    }
}
