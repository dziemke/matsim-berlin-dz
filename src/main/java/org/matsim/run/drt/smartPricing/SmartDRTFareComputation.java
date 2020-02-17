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

import java.util.HashMap;
import java.util.Map;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEvent;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;

import com.google.inject.Inject;

/**
* @author ikaddoura
*/

public class SmartDRTFareComputation implements DrtRequestSubmittedEventHandler, PersonArrivalEventHandler {
    private String mode;
    Map<Id<Person>, DrtRequestSubmittedEvent> lastRequestSubmission = new HashMap<>();

	@Inject
	private EventsManager events;
	
	@Inject
	private Scenario scenario;
	 
	@Override
	public void reset(int iteration) {
		lastRequestSubmission.clear();
	}

	 @Override
	 public void handleEvent(PersonArrivalEvent event) {
		 double fare = 0.;
         DrtRequestSubmittedEvent e = this.lastRequestSubmission.get(event.getPersonId());
         double departureTime = e.getTime();
         Id<Link> deartureLinkID = e.getFromLinkId();    
         Id<Link> arrivalLinkID = e.getToLinkId();
         
		 // TODO: compute pt travel time     
         double ptTravelTime = 0.; // TODO
         
         // TODO (optional / performance only): store pt departure time + travel time
         scenario.getPopulation().getPersons().get(event.getPersonId()).getSelectedPlan();
         
         double ratio = ptTravelTime / e.getUnsharedRideTime();
         
         // TODO: use config group
         if (ratio > 1.0) {
        	 // no DRT penalty
         } else {
        	 // pt is faster than DRT --> add fare penalty
        	 fare += 10.;
         }
		 
		 if (event.getLegMode().equals(this.mode)) {
	         events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), -fare));
	     }
	 }


	    @Override
	    public void handleEvent(DrtRequestSubmittedEvent event) {
	        if (this.mode.equals(event.getMode())) {
	            this.lastRequestSubmission.put(event.getPersonId(), event);
	        }
	    }
	
}

