/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2017 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.run.debug;

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptorModule;
import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.locationchoice.frozenepsilons.FrozenTastes;
import org.matsim.contrib.locationchoice.frozenepsilons.FrozenTastesConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigGroup;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup.ActivityParams;
import org.matsim.core.config.groups.QSimConfigGroup.TrafficDynamics;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.config.groups.VspExperimentalConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.controler.OutputDirectoryLogging;
import org.matsim.core.gbl.Gbl;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.scoring.ScoringFunction;
import org.matsim.core.scoring.ScoringFunctionFactory;
import org.matsim.core.scoring.SumScoringFunction;
import org.matsim.core.scoring.functions.*;
import org.matsim.prepare.population.reLocation.MyScoringFunction;

import java.util.Arrays;

import static org.matsim.core.config.groups.ControlerConfigGroup.RoutingAlgorithmType.FastAStarLandmarks;

/**
* @author ikaddoura
*/

public final class RunBerlinScenarioDebug {

	private static final Logger log = Logger.getLogger(RunBerlinScenarioDebug.class );

	public static void main(String[] args) {
		
		for (String arg : args) {
			log.info( arg );
		}
		
		if ( args.length==0 ) {
			args = new String[] {"scenarios/berlin-v5.5-1pct/input/berlin-v5.5-1pct.config.xml"}  ;
		}

		Config config = prepareConfig( args ) ;
		Scenario scenario = prepareScenario( config ) ;
		Controler controler = prepareControler( scenario ) ;
		controler.run() ;

	}

	public static Controler prepareControler( Scenario scenario ) {
		// note that for something like signals, and presumably drt, one needs the controler object
		
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
//		controler.addOverridingModule( new AbstractModule(){
//			@Override public void install() {
//				this.bindScoringFunctionFactory().to( MyScoringFunction.class ) ;
//			}
//		} );

		return controler;
	}
	
	public static Scenario prepareScenario( Config config ) {
		Gbl.assertNotNull( config );
		
		// note that the path for this is different when run from GUI (path of original config) vs.
		// when run from command line/IDE (java root).  :-(    See comment in method.  kai, jul'18
		// yy Does this comment still apply?  kai, jul'19

		final Scenario scenario = ScenarioUtils.loadScenario( config );

		return scenario;
	}
	
	public static Config prepareConfig( String [] args, ConfigGroup... customModules ) {
		OutputDirectoryLogging.catchLogEntries();
		
		String[] typedArgs = Arrays.copyOfRange( args, 1, args.length );

		final Config config = ConfigUtils.loadConfig( args[ 0 ], customModules ); // I need this to set the context
		
		config.controler().setRoutingAlgorithmType( FastAStarLandmarks );
		
		config.subtourModeChoice().setProbaForRandomSingleTripMode( 0.5 );
		
		config.plansCalcRoute().setRoutingRandomness( 3. );
		config.plansCalcRoute().removeModeRoutingParams(TransportMode.ride);
		config.plansCalcRoute().removeModeRoutingParams(TransportMode.pt);
		config.plansCalcRoute().removeModeRoutingParams(TransportMode.bike);
		config.plansCalcRoute().removeModeRoutingParams("undefined");
		
		// TransportMode.non_network_walk has no longer a default, copy from walk
//		ModeRoutingParams walkRoutingParams = config.plansCalcRoute().getOrCreateModeRoutingParams(TransportMode.walk);
//		ModeRoutingParams non_network_walk_routingParams = new ModeRoutingParams(TransportMode.non_network_walk);
//		non_network_walk_routingParams.setBeelineDistanceFactor(walkRoutingParams.getBeelineDistanceFactor());
//		non_network_walk_routingParams.setTeleportedModeSpeed(walkRoutingParams.getTeleportedModeSpeed());
//		config.plansCalcRoute().addModeRoutingParams(non_network_walk_routingParams);
	
		config.qsim().setInsertingWaitingVehiclesBeforeDrivingVehicles( true );
				
		// vsp defaults
		config.vspExperimental().setVspDefaultsCheckingLevel( VspExperimentalConfigGroup.VspDefaultsCheckingLevel.info );
		config.plansCalcRoute().setInsertingAccessEgressWalk( true );
//		config.qsim().setUsingTravelTimeCheckInTeleportation( true );
		config.qsim().setTrafficDynamics( TrafficDynamics.kinematicWaves );
				
		// activities:
			config.planCalcScore().addActivityParams( new ActivityParams( "home" ).setTypicalDuration( 7200 ).setScoringThisActivityAtAll(false) );
			config.planCalcScore().addActivityParams( new ActivityParams( "leisure" ).setTypicalDuration( 7200 ).setOpeningTime(9. * 3600. ).setClosingTime(27. * 3600. ).setScoringThisActivityAtAll(false) );
			config.planCalcScore().addActivityParams( new ActivityParams( "shopping").setTypicalDuration( 1200 ).setOpeningTime(8. * 3600. ).setClosingTime(20. * 3600. ).setScoringThisActivityAtAll(false) );
		config.planCalcScore().addActivityParams( new ActivityParams( "freight" ).setTypicalDuration( 12.*3600. ).setScoringThisActivityAtAll(false) );

		config.plans().setInputFile("D:/Arbeit/Berlin/ReLocation/debug frozen tastes/berlin-v5.5-plans_onePerson.xml.gz");
		config.controler().setLastIteration(2);
		config.controler().setWriteEventsUntilIteration(2);
		config.controler().setOutputDirectory("D:/Arbeit/Berlin/ReLocation/debug frozen tastes/output");

		config.controler().setWriteEventsUntilIteration(2);
		config.strategy().setFractionOfIterationsToDisableInnovation(Double.POSITIVE_INFINITY);

//		for (StrategyConfigGroup.StrategySettings strategy : config.strategy().getStrategySettings()) {
//			if (strategy.getSubpopulation().equals("person")) {
//				strategy.setWeight(0);
//			}
//		}

		config.facilities().setInputFile("D:/Arbeit/Berlin/ReLocation/debug frozen tastes/facilitiesOpenBerlinDebug.xml.gz");

		config.strategy().addStrategySettings( new StrategyConfigGroup.StrategySettings( ).setStrategyName( FrozenTastes.LOCATION_CHOICE_PLAN_STRATEGY ).setWeight( 1 ).setDisableAfter( 10 ).setSubpopulation("person") );

		FrozenTastesConfigGroup dccg = ConfigUtils.addOrGetModule( config, FrozenTastesConfigGroup.class );;
		dccg.setEpsilonScaleFactors("1.0, 1.0");
		dccg.setAlgorithm( FrozenTastesConfigGroup.Algotype.bestResponse );
		dccg.setFlexibleTypes("leisure, shopping");
		dccg.setTravelTimeApproximationLevel( FrozenTastesConfigGroup.ApproximationLevel.localRouting );
		dccg.setRandomSeed( 2 );
		dccg.setDestinationSamplePercent( 1. );

		config.planCalcScore().addActivityParams(
				new ActivityParams("car interaction").setScoringThisActivityAtAll(false)
		);
		config.planCalcScore().addActivityParams(
				new ActivityParams("pt interaction").setScoringThisActivityAtAll(false)
		);
		config.planCalcScore().addActivityParams(
				new ActivityParams("ride interaction").setScoringThisActivityAtAll(false)
		);
		config.planCalcScore().addActivityParams(
				new ActivityParams("freight interaction").setScoringThisActivityAtAll(false)
		);

		config.controler().setWriteEventsInterval(1);
		config.controler().setWritePlansInterval(1);
		config.controler().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);

		config.transit().setUseTransit(false);
//		config.planCalcScore().addModeParams(new PlanCalcScoreConfigGroup.ModeParams("car").setMarginalUtilityOfTraveling(0.0));

		for (ActivityParams activityParam : config.planCalcScore().getActivityParams()) {
			activityParam.setScoringThisActivityAtAll(false);
		}

		config.plansCalcRoute().removeModeRoutingParams(TransportMode.car);

		ConfigUtils.applyCommandline( config, typedArgs ) ;

		return config ;
	}

}

