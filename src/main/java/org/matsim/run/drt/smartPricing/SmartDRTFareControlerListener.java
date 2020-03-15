package org.matsim.run.drt.smartPricing;

import com.google.inject.Inject;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.controler.events.IterationEndsEvent;
import org.matsim.core.controler.events.IterationStartsEvent;
import org.matsim.core.controler.events.ShutdownEvent;
import org.matsim.core.controler.listener.IterationEndsListener;
import org.matsim.core.controler.listener.IterationStartsListener;
import org.matsim.core.controler.listener.ShutdownListener;
import org.matsim.run.drt.smartPricing.prepare.ProfitUtility;

/**
 * @author : zmeng
 * @date :
 */
public class SmartDRTFareControlerListener implements IterationEndsListener, IterationStartsListener, ShutdownListener {

    @Inject
    private SmartDrtFareConfigGroup smartDrtFareConfigGroup;
    @Inject
    private SmartDRTFareComputation smartDRTFareComputation;
    @Inject
    private EventsManager eventsManager;
    @Inject
    private Scenario scenario;
    @Inject
    private ProfitUtility profitUtility;

    @Override
    public void notifyIterationEnds(IterationEndsEvent iterationEndsEvent) {
        smartDRTFareComputation.writeLog();
        if(iterationEndsEvent.getIteration() == scenario.getConfig().controler().getLastIteration()){
            smartDRTFareComputation.writeFile();
        } else if (iterationEndsEvent.getIteration() % smartDrtFareConfigGroup.getWriteFileInterval() == 0) {
            smartDRTFareComputation.writeFile();
        }
    }

    @Override
    public void notifyIterationStarts(IterationStartsEvent iterationStartsEvent) {
        if(iterationStartsEvent.getIteration() == scenario.getConfig().controler().getLastIteration()){
            eventsManager.addHandler(profitUtility);
        }
    }

    @Override
    public void notifyShutdown(ShutdownEvent shutdownEvent) {
        profitUtility.writeProfitInfo();
    }
}