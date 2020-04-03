/* *********************************************************************** *
 * project: org.matsim.*
 * Controler.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2007 by the members listed in the COPYING,        *
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

package org.matsim.run;

import org.matsim.core.config.Config;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;

class RunOpenBerlinSchoolScenario {

    private static final String CONFIG_PATH_1PCT = "../../svn/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-1pct-schools/input/berlin-v5.4-1pct-schools.config.xml";
    private static final String OUTPUT_DIR_1PCT = "../../svn/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-1pct-schools/output-berlin-v5.4-1pct-schools/";

    private static final String CONFIG_PATH_10PCT = "../../svn/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct-schools/input/berlin-v5.4-10pct-schools.config.xml";
    private static final String OUTPUT_DIR_10PCT = "../../svn/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.4-10pct-schools/output-berlin-v5.4-10pct-schools/";

    public static void main(String[] args) {

        boolean doOnePercent = false;



        String configFile, outputDir;
        if(doOnePercent){
             configFile = CONFIG_PATH_1PCT;
             outputDir = OUTPUT_DIR_1PCT;
        } else {
            configFile = CONFIG_PATH_10PCT;
            outputDir = OUTPUT_DIR_10PCT;
        }

        Config config = RunBerlinScenario.prepareConfig(new String[]{configFile});

        //school attendees have normal "home" activities without any suffix..
        config.planCalcScore().addActivityParams((new PlanCalcScoreConfigGroup.ActivityParams("home")).setTypicalDuration((double) 8*3600));

        config.planCalcScore().addActivityParams((new PlanCalcScoreConfigGroup.ActivityParams("educ_kiga")).setTypicalDuration((double) 8*3600));
        config.planCalcScore().addActivityParams((new PlanCalcScoreConfigGroup.ActivityParams("educ_primary")).setTypicalDuration((double) 8*3600));
        config.planCalcScore().addActivityParams((new PlanCalcScoreConfigGroup.ActivityParams("educ_secondary")).setTypicalDuration((double) 8*3600));

//        config.planCalcScore().setFractionOfIterationsToStartScoreMSA(0.8); //do not know why it is not set beforehand.. does not matter here anyways
        config.controler().setOutputDirectory(outputDir);
        config.controler().setRunId("output-berlin-v5.4-1pct-schools");
        config.controler().setLastIteration(0);

        RunBerlinScenario.prepareControler(RunBerlinScenario.prepareScenario(config)).run();
    }

}
