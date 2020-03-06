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

package org.matsim.run.frozenTastes;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.locationchoice.frozenepsilons.FrozenTastes;
import org.matsim.contrib.locationchoice.frozenepsilons.FrozenTastesConfigGroup;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.config.groups.StrategyConfigGroup;
import org.matsim.core.controler.Controler;
import org.matsim.run.RunBerlinScenario;

/**
* @author ikaddoura
*/

public class RunFrozenTastesOpenBerlinScenario {
	private static final Logger log = Logger.getLogger(RunFrozenTastesOpenBerlinScenario.class);

	public static void main(String[] args) {
		for (String arg : args) {
			log.info( arg );
		}

		if ( args.length==0 ) {
			args = new String[] {"scenarios/berlin-v5.5-1pct/input/berlin-v5.5-1pct.config.xml"}  ;
		}

		String[] arg = new String[args.length-4];
		int x = 0;
		for (int i = 0; i < args.length; i++) {
			if (i == 1 || i == 2 || i == 3 || i == 4) {
				continue;
			}
			arg[x] = args[i];
			x++;
		}

		Config config = RunBerlinScenario.prepareConfig( arg ) ;

		// config adjustments for the locationchoice contrib:
		// activities:
		String flexTyp = "";
		String scalFac = "";
		for ( long ii = 600 ; ii <= 97200; ii+=600 ) {
			flexTyp = flexTyp + "leisure_" + ii + ".0,";
			flexTyp = flexTyp + "shopping_" + ii + ".0,";
			scalFac = scalFac + args[1] + "," + args[2] + ",";
		}
		
		scalFac = scalFac.substring(0, scalFac.length() - 1);
		flexTyp = flexTyp.substring(0, flexTyp.length() - 1);

		config.facilities().setInputFile(args[3]);

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
		dccg.setDestinationSamplePercent( Double.parseDouble(args[4]) );
		
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

		Scenario scenario = RunBerlinScenario.prepareScenario( config ) ;
		for( Person person : scenario.getPopulation().getPersons().values() ){
			person.getPlans().removeIf( (plan) -> plan!=person.getSelectedPlan() ) ;
		}
		
		Controler controler = RunBerlinScenario.prepareControler( scenario ) ;
		
		// required by locationchoice contrib
		FrozenTastes.configure( controler );

		controler.run() ;

		log.info("starting analyse");

		RunBerlinScenario.runAnalysis(controler);
	}

}

