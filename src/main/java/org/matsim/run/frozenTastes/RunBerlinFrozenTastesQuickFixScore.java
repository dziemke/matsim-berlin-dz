package org.matsim.run.frozenTastes;

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.locationchoice.frozenepsilons.FrozenTastes;
import org.matsim.contrib.locationchoice.frozenepsilons.FrozenTastesConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.QSimConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.drtSpeedUp.DrtSpeedUpConfigGroup;
import org.matsim.drtSpeedUp.DrtSpeedUpModule;
import org.matsim.run.drt.RunDrtOpenBerlinScenario;

import java.util.Arrays;

import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;

public class RunBerlinFrozenTastesQuickFixScore {

    private static final Logger log = Logger.getLogger(RunBerlinFrozenTastesQuickFixScore.class);

    public static void main(String[] args) {

        for (String arg : args) {
            log.info( arg );
        }

        if ( args.length==0 ) {
            args = new String[] {"scenarios/berlin-v5.5-1pct/input/berlin-v5.5-1pct.config.xml"}  ;
        }
        args = new String[3];
        args[0] = "test/input/frozenTastes/berlin-drt-v5.5-1pct.config_FrozenTastes0.xml";
        args[1] = "0";
        args[2] = "0";
        Config config = prepareConfig( args ) ;
        Scenario scenario = prepareScenario( config ) ;
        for( Person person : scenario.getPopulation().getPersons().values() ){
            person.getPlans().removeIf( (plan) -> plan!=person.getSelectedPlan() ) ;
        }
        Controler controler = prepareControler( scenario ) ;
        controler.run() ;

    }

    public static Controler prepareControler( Scenario scenario ) {
        Gbl.assertNotNull(scenario);
        final Controler controler = new Controler( scenario );

        FrozenTastes.configure( controler );

        if (controler.getConfig().transit().isUsingTransitInMobsim()) {
            // use the sbb pt raptor router
            controler.addOverridingModule( new AbstractModule() {
                @Override
                public void install() {
                    install( new SwissRailRaptorModule() );
                }
            } );
        } else {
            log.warn("Public transit will be teleported and not simulated in the mobsim! "
                    + "This will have a significant effect on pt-related parameters (travel times, modal split, and so on). "
                    + "Should only be used for testing or car-focused studies with a fixed modal split.  ");
        }

        // use the (congested) car travel time for the teleported ride mode
        controler.addOverridingModule( new AbstractModule() {
            @Override
            public void install() {
                addTravelTimeBinding( TransportMode.ride ).to( networkTravelTime() );
                addTravelDisutilityFactoryBinding( TransportMode.ride ).to( carTravelDisutilityFactoryKey() );
            }
        } );

        return controler;
    }

    public static Scenario prepareScenario(Config config) {
        Gbl.assertNotNull( config );

        final Scenario scenario = ScenarioUtils.loadScenario( config );

        return scenario;
    }

    public static Config prepareConfig(String[] args, ConfigGroup... customModules) {

        OutputDirectoryLogging.catchLogEntries();

        String[] typedArgs = Arrays.copyOfRange( args, 3, args.length );

        Config config = ConfigUtils.loadConfig( args[ 0 ], customModules ); // I need this to set the context
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
            scalFac = scalFac + args[1] + "," + args[2] + ",";
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
        FrozenTastesConfigGroup dccg = ConfigUtils.addOrGetModule( config, FrozenTastesConfigGroup.class );;
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

        ConfigUtils.applyCommandline( config, typedArgs ) ;



        return config ;
    }

}
