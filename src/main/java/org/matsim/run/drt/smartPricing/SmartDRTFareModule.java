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

package org.matsim.run.drt.smartPricing;

import org.matsim.core.controler.AbstractModule;
import org.matsim.run.drt.smartPricing.prepare.ProfitUtility;

/**
 * @author : zmeng
 * @date : 20.Feb
 */
public class SmartDRTFareModule extends AbstractModule {
    @Override
    public void install() {
        this.bind(SmartDRTFareComputation.class).asEagerSingleton();
        this.bind(ProfitUtility.class).asEagerSingleton();
        addEventHandlerBinding().to(SmartDRTFareComputation.class);
        addControlerListenerBinding().to(SmartDRTFareControlerListener.class);
    }
}
