package org.matsim.run.drt.smartPricing;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.listener.IterationEndsListener;

/**
 * @author : zmeng
 * @date :
 */
public class SmartDRTFareControlerListener implements IterationEndsListener {

    @Inject
    private SmartDrtFareConfigGroup smartDrtFareConfigGroup;
    @Inject
    private SmartDRTFareComputation smartDRTFareComputation;
    @Inject
    private Scenario scenario;

    @Override
    public void notifyIterationEnds(IterationEndsEvent iterationEndsEvent) {
        if(iterationEndsEvent.getIteration() == scenario.getConfig().controler().getLastIteration() && smartDrtFareConfigGroup.isWriteLog()){
            smartDRTFareComputation.writeLog();
        } else if (iterationEndsEvent.getIteration() % smartDrtFareConfigGroup.getWriteLogInterval() == 0 && smartDrtFareConfigGroup.isWriteLog()) {
            smartDRTFareComputation.writeLog();
        }
    }
}