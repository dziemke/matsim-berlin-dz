package org.matsim.run.frozenTastes;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.locationchoice.frozenepsilons.FrozenTastes;
import org.matsim.contrib.locationchoice.frozenepsilons.FrozenTastesConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.run.RunBerlinScenarioTest;
import org.matsim.testcases.MatsimTestUtils;

import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;

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
            Config config = prepareConfig();
            config.plans().setInputFile("test-Person-noEndTime.xml");
            config.planCalcScore().addActivityParams(
                    new PlanCalcScoreConfigGroup.ActivityParams("car interaction").setScoringThisActivityAtAll(false)
            );

            Scenario scenario = ScenarioUtils.loadScenario( config );
            Controler controler = new Controler( scenario );

            FrozenTastes.configure( controler );

            controler.run();

            log.info( "Done with endDurationTest"  );
            log.info("") ;

        } catch ( Exception ee ) {
            throw new RuntimeException(ee) ;
        }
    }

    @Test
    // previously named backwardPathTest, but backWardPath was fixed
    public final void calPlanWithMarginalUtilityOfDistance_mOrMonetaryDistanceCostRate() {
        try {
            Config config = prepareConfig();
            config.plans().setInputFile("test-Person.xml");
            config.planCalcScore().addModeParams(new PlanCalcScoreConfigGroup.ModeParams("car").setMarginalUtilityOfDistance(-6));
            config.planCalcScore().addActivityParams(
                    new PlanCalcScoreConfigGroup.ActivityParams("car interaction").setScoringThisActivityAtAll(false)
            );

            Scenario scenario = ScenarioUtils.loadScenario( config );
            Controler controler = new Controler( scenario );

            FrozenTastes.configure( controler );

            controler.run();

            log.info("Done with backwardPathTest");
            log.info("");

        } catch (Exception ee) {
            throw new RuntimeException(ee);
        }
    }

    @Test
    public final void modeInteractionTest () {
        try {
            Config config = prepareConfig();
            config.plans().setInputFile("test-Person2.xml");
//            config.controler().setLastIteration(2);
            config.planCalcScore().addActivityParams(
                    new PlanCalcScoreConfigGroup.ActivityParams("car interaction").setScoringThisActivityAtAll(false)
            );

            Scenario scenario = ScenarioUtils.loadScenario( config );
            Controler controler = new Controler( scenario );

            FrozenTastes.configure( controler );

            controler.run();

            log.info("Done with modeInteractionTest");
            log.info("");

        } catch (Exception ee) {
            throw new RuntimeException(ee);
        }
    }

    private Config prepareConfig() {

        Config config = ConfigUtils.loadConfig("test/input/frozenTastes/berlin-v5.5-1pct.config.xml");
        config.controler().setOutputDirectory(utils.getOutputDirectory());
        config.facilities().setInputFile("twoFacilities2" +
                ".xml");

        config.controler().setWriteEventsInterval(1);
        config.controler().setWritePlansInterval(1);
        config.controler().setLastIteration(1);
        config.controler().setWriteEventsUntilIteration(1);
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        config.transit().setUseTransit(true);

        // berlin stuff
        {
            config.controler().setRoutingAlgorithmType(FastAStarLandmarks);
            config.subtourModeChoice().setProbaForRandomSingleTripMode(0.5);
            config.plansCalcRoute().setRoutingRandomness(3.);
            config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles(true);

            // vsp defaults
            config.vspExperimental().setVspDefaultsCheckingLevel(VspExperimentalConfigGroup.VspDefaultsCheckingLevel.info);
            config.plansCalcRoute().setInsertingAccessEgressWalk(true);
            // config.qsim().setUsingTravelTimeCheckInTeleportation( true );
            config.qsim().setTrafficDynamics(QSimConfigGroup.TrafficDynamics.kinematicWaves);

            // activities
            config.planCalcScore().addActivityParams(new PlanCalcScoreConfigGroup.ActivityParams("home").setTypicalDuration(7200).setScoringThisActivityAtAll(false));
            config.planCalcScore().addActivityParams(new PlanCalcScoreConfigGroup.ActivityParams("leisure").setTypicalDuration(7200).setOpeningTime(9. * 3600.).setClosingTime(27. * 3600.).setScoringThisActivityAtAll(false));
            config.planCalcScore().addActivityParams(new PlanCalcScoreConfigGroup.ActivityParams("shopping").setTypicalDuration(1200).setOpeningTime(8. * 3600.).setClosingTime(20. * 3600.).setScoringThisActivityAtAll(false));

            config.strategy().setFractionOfIterationsToDisableInnovation(Double.POSITIVE_INFINITY);

        }

		// using relocation contrib
        {
            config.strategy().addStrategySettings(new StrategyConfigGroup.StrategySettings().setStrategyName(FrozenTastes.LOCATION_CHOICE_PLAN_STRATEGY).setWeight(1).setDisableAfter(10).setSubpopulation("person"));

            FrozenTastesConfigGroup dccg = ConfigUtils.addOrGetModule(config, FrozenTastesConfigGroup.class);

            dccg.setEpsilonScaleFactors("1.0,1.0");
            dccg.setAlgorithm(FrozenTastesConfigGroup.Algotype.bestResponse);
            dccg.setFlexibleTypes("leisure,shopping");
            dccg.setTravelTimeApproximationLevel(FrozenTastesConfigGroup.ApproximationLevel.localRouting);
            dccg.setRandomSeed(2);
            dccg.setDestinationSamplePercent(100.);
            dccg.setpkValuesFile("D:/Arbeit/Code/matsim-berlin/test/input/frozenTastes/personsKValues.xml");
            dccg.setfkValuesFile("D:/Arbeit/Code/matsim-berlin/test/input/frozenTastes/facilitiesKValues.xml");
            dccg.setMaxEpsFile("D:/Arbeit/Code/matsim-berlin/test/input/frozenTastes/personsMaxDCScoreUnscaled.xml");
        }

        return config ;
    }

}
