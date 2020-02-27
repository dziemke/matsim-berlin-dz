/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
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

package org.matsim.run.discreteModeChoice;

import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;

import com.google.inject.Provides;

import ch.ethz.matsim.discrete_mode_choice.model.mode_availability.ModeAvailability;
import ch.ethz.matsim.discrete_mode_choice.modules.AbstractDiscreteModeChoiceExtension;

public class BerlinModeChoiceModule extends AbstractDiscreteModeChoiceExtension {
	@Override
	protected void installExtension() {
		bindModeAvailability("BerlinModeAvailability").to(BerlinModeAvailability.class);
		bindTripConstraintFactory("KeepRide").to(KeepRideConstraint.Factory.class);
		bindTripConstraintFactory("OnlyFallbackWalkConstraint").to(OnlyFallbackWalkConstraint.Factory.class);
	}
	
	@Provides
	public ModeAvailability provideBerlinModeAvailability(PlanCalcScoreConfigGroup planCalcScoreConfig) {
		return new BerlinModeAvailability(planCalcScoreConfig);
	}
}