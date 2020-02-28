package org.matsim.prepare.population.reLocation;

import com.google.inject.Module;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
//import org.matsim.contrib.locationchoice.frozenepsilons.BestReplyLocationChoicePlanStrategy;
import org.matsim.contrib.locationchoice.frozenepsilons.FrozenTastes;
import org.matsim.contrib.locationchoice.frozenepsilons.FrozenTastesConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.controler.*;
import org.matsim.core.controler.corelisteners.ControlerDefaultCoreListenersModule;
import org.matsim.core.replanning.StrategyManager;
import org.matsim.core.scenario.ScenarioByInstanceModule;
import org.matsim.core.scenario.ScenarioUtils;

import java.util.Arrays;

import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;

public class FrozenTastesStrategy {

    public static void main(String[] args) {

        Config config = ConfigUtils.loadConfig("test/input/frozenTastes/berlin-drt-v5.5-1pct.config_FrozenTastes0.xml");
        Scenario scenario = ScenarioUtils.loadScenario(config);

        Module module = new AbstractModule() {
            @Override
            public void install() {
//                install(new StrategyManagerModule());
                install( new NewControlerModule() );
                install( new ControlerDefaultCoreListenersModule() );
                install( new ControlerDefaultsModule() );
                install( new ScenarioByInstanceModule( scenario ) ) ;
            }
        };
        System.out.println("Start doing stuff 1");
        com.google.inject.Injector incejtor = Injector.createInjector(config, module);
        System.out.println("Start doing stuff 2");
        StrategyManager strategyManager = incejtor.getInstance(StrategyManager.class);
        config = null;
        config = getConfig(args);
        System.out.println("Start doing stuff 3");
//        strategyManager.addStrategy(new BestReplyLocationChoicePlanStrategy(), "Person", 1);
        strategyManager.run(scenario.getPopulation(), () -> 0);

    }

    private static Config getConfig(String[] args, ConfigGroup... customModules) {
        OutputDirectoryLogging.catchLogEntries();

        String[] typedArgs = Arrays.copyOfRange( args, 0, args.length );
        Config config = ConfigUtils.loadConfig("test/input/frozenTastes/berlin-drt-v5.5-1pct.config_FrozenTastes0.xml", customModules ); // I need this to set the context
        config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

        config.controler().setRoutingAlgorithmType( FastAStarLandmarks );

        config.subtourModeChoice().setProbaForRandomSingleTripMode( 0.5 );

        config.plansCalcRoute().setRoutingRandomness( 3. );
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.ride);
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.pt);
        config.plansCalcRoute().removeModeRoutingParams(TransportMode.bike);
        config.plansCalcRoute().removeModeRoutingParams("undefined");

        config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles( true );

        config.vspExperimental().setVspDefaultsCheckingLevel( VspExperimentalConfigGroup.VspDefaultsCheckingLevel.info );
        config.plansCalcRoute().setInsertingAccessEgressWalk( true );
        config.qsim().setUsingTravelTimeCheckInTeleportation( true );
        config.qsim().setTrafficDynamics( QSimConfigGroup.TrafficDynamics.kinematicWaves );

        // frozenTastes
        String flexTyp = "";
        String scalFac = "";

        for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
            config.planCalcScore().addActivityParams( new PlanCalcScoreConfigGroup.ActivityParams( "home_" + ii + ".0" ).setTypicalDuration( ii ) );
            config.planCalcScore().addActivityParams( new PlanCalcScoreConfigGroup.ActivityParams( "work_" + ii + ".0" ).setTypicalDuration( ii ).setOpeningTime(6. * 3600. ).setClosingTime(20. * 3600. ) );
            config.planCalcScore().addActivityParams( new PlanCalcScoreConfigGroup.ActivityParams( "leisure_" + ii + ".0" ).setTypicalDuration( ii ).setOpeningTime(9. * 3600. ).setClosingTime(27. * 3600. ) );
            config.planCalcScore().addActivityParams( new PlanCalcScoreConfigGroup.ActivityParams( "shopping_" + ii + ".0" ).setTypicalDuration( ii ).setOpeningTime(8. * 3600. ).setClosingTime(20. * 3600. ) );
            config.planCalcScore().addActivityParams( new PlanCalcScoreConfigGroup.ActivityParams( "other_" + ii + ".0" ).setTypicalDuration( ii ) );
            // frozenTastes
            flexTyp = flexTyp + "leisure_" + ii + ".0,";
            flexTyp = flexTyp + "shopping_" + ii + ".0,";
            scalFac = scalFac + "0" + "," + "0" + ",";
        }

        // frozenTastes
        scalFac = scalFac.substring(0, scalFac.length() - 1);
        flexTyp = flexTyp.substring(0, flexTyp.length() - 1);
        config.facilities().setInputFile("twoFacilities.xml");
//        config.facilities().setInputFile("D:/Arbeit/Berlin/ReLocation/facilitiesOpenBerlin.xml.gz");
        for (StrategyConfigGroup.StrategySettings strategy : config.strategy().getStrategySettings()) {
            if (strategy.getSubpopulation().equals("person")) {
                strategy.setWeight(0);
            }
        }
        config.strategy().addStrategySettings( new StrategyConfigGroup.StrategySettings( ).setStrategyName( FrozenTastes.LOCATION_CHOICE_PLAN_STRATEGY ).setWeight( 1 ).setDisableAfter( 10 ).setSubpopulation("person") );
        FrozenTastesConfigGroup dccg = ConfigUtils.addOrGetModule( config, FrozenTastesConfigGroup.class );

        dccg.setEpsilonScaleFactors(scalFac);
        dccg.setAlgorithm( FrozenTastesConfigGroup.Algotype.bestResponse );
        dccg.setFlexibleTypes(flexTyp);
        dccg.setTravelTimeApproximationLevel( FrozenTastesConfigGroup.ApproximationLevel.localRouting );
        dccg.setRandomSeed( 2 );
        dccg.setDestinationSamplePercent( 100. );

        config.controler().setLastIteration( 1 );
        config.controler().setWriteEventsUntilIteration( 1 );

        // hopefully soon no longer necessary
        {
            config.planCalcScore().addActivityParams(
                    new PlanCalcScoreConfigGroup.ActivityParams("car interaction").setScoringThisActivityAtAll(false)
            );
            config.planCalcScore().addActivityParams(
                    new PlanCalcScoreConfigGroup.ActivityParams("pt interaction").setScoringThisActivityAtAll(false)
            );
            config.planCalcScore().addActivityParams(
                    new PlanCalcScoreConfigGroup.ActivityParams("ride interaction").setScoringThisActivityAtAll(false)
            );
            config.planCalcScore().addActivityParams(
                    new PlanCalcScoreConfigGroup.ActivityParams("freight interaction").setScoringThisActivityAtAll(false)
            );
        }

        config.planCalcScore().addActivityParams( new PlanCalcScoreConfigGroup.ActivityParams( "freight" ).setTypicalDuration( 12.*3600. ) );

        config.transit().setUseTransit(false);

        ConfigUtils.applyCommandline( config, typedArgs ) ;
        return config;
    }

}
