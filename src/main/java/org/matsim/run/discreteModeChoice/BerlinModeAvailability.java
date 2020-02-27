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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;

import com.google.inject.Inject;

import ch.ethz.matsim.discrete_mode_choice.model.DiscreteModeChoiceTrip;
import ch.ethz.matsim.discrete_mode_choice.model.mode_availability.ModeAvailability;

public class BerlinModeAvailability implements ModeAvailability {
	
	private final Collection<String> personModes;

	/*
	 * Return all possible routing modes (except for freight agents), even if they should not be available such as ride.
	 * Those have to be available here and will be excluded in a constraint module, because ride trips have to pass through here.
	 * TODO: allow virtual routing modes not existent as leg mode and not in planCalcScoreConfig
	 */
	@Inject
	BerlinModeAvailability (PlanCalcScoreConfigGroup planCalcScoreConfig) {
		personModes = new HashSet<>();
		personModes.addAll(planCalcScoreConfig.getAllModes());
		personModes.remove("freight");
	}

	@Override
	public Collection<String> getAvailableModes(Person person, List<DiscreteModeChoiceTrip> trips) {
		if (person.getId().toString().contains("freight")) {
			return Collections.singleton("freight");
		}

		return personModes;
	}
	
}
