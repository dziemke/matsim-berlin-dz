package org.matsim.run.frozenTastes;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.matsim.api.core.v01.Scenario;
import org.matsim.contrib.locationchoice.frozenepsilons.FrozenTastes;
import org.matsim.core.config.Config;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.run.RunBerlinScenarioTest;
import org.matsim.run.drt.RunDrtOpenBerlinScenarioWithDrtSpeedUp;
import org.matsim.testcases.MatsimTestUtils;

public class RunDrtAndFrozenTastesTest {

    private static final Logger log = Logger.getLogger(RunBerlinScenarioTest.class);

    @Rule
    public MatsimTestUtils utils = new MatsimTestUtils();

    @Test
    public final void aTestTest() {
        // a dummy test to satisfy the matrix build by travis.
        log.info("Done with aTestTest");
        log.info("");
        Assert.assertTrue(true);
    }

    @Test
    public final void drtWithFrozenTastes() {



    }

}




