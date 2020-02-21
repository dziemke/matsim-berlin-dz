package org.matsim.run.drt.smartPricing;

import org.matsim.core.controler.AbstractModule;

/**
 * @author : zmeng
 * @date : 20.Feb
 */
public class SmartDRTFareModule extends AbstractModule {
    @Override
    public void install() {
        this.addEventHandlerBinding().to(SmartDRTFareComputation.class);
    }
}
