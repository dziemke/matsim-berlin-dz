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

import com.google.inject.Inject;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEvent;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.core.router.TripRouter;
import org.matsim.run.drt.smartPricing.prepare.EstimatePtTrip;
import org.matsim.run.drt.smartPricing.prepare.RealDrtTripInfo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author ikaddoura zmeng
 */
public class SmartDRTFareComputation implements DrtRequestSubmittedEventHandler, PersonArrivalEventHandler, ActivityEndEventHandler {

    private final Logger logger = Logger.getLogger(SmartDRTFareComputation.class);
    @Inject
    private EventsManager events;
    @Inject
    private Scenario scenario;
    @Inject
    private TripRouter tripRouter;
    @Inject
    private SmartDrtFareConfigGroup smartDrtFareConfigGroup;

    private int currentIteration;
    private BufferedWriter bw = null;
    private Map<Id<Person>, RealDrtTripInfo> personId2drtTripInfoCollector = new HashMap<>();
    private Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTrips = new HashMap<>();

    @Override
    public void reset(int iteration) {
        this.currentIteration = iteration;
        personId2drtTripInfoCollector.clear();
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {

        if (this.personId2drtTripInfoCollector.containsKey(event.getPersonId())) {
            var drtTrip = this.personId2drtTripInfoCollector.get(event.getPersonId());
            if (event.getLegMode().equals(smartDrtFareConfigGroup.getDrtMode())) {
                drtTrip.setFindDrtArrivalEvent(true);
                drtTrip.setDrtArrivalEvent(event);
            } else if (drtTrip.isLastArrivalEvent()) {
                drtTrip.setLastArrivalEvent(event);

                if (!this.personId2estimatePtTrips.containsKey(event.getPersonId())) {
                    this.personId2estimatePtTrips.put(event.getPersonId(), new LinkedList<>());
                }

                double departureTime = drtTrip.getRealActivityEndEvent().getTime();
                Id<Link> departureLinkID = drtTrip.getRealActivityEndEvent().getLinkId();
                Id<Link> arrivalLinkID = drtTrip.getLastArrivalEvent().getLinkId();

                var ptTrips = personId2estimatePtTrips.get(event.getPersonId());

                EstimatePtTrip temEstimatePtTrip = new EstimatePtTrip(scenario, departureLinkID, arrivalLinkID, departureTime);
                EstimatePtTrip estimatePtTrip = temEstimatePtTrip.updatePtTrips(ptTrips);

                if (!estimatePtTrip.isHasPtTravelTime()) {
                    estimatePtTrip.setHasPtTravelTime(true);
                    var planElements = tripRouter.calcRoute(TransportMode.pt, estimatePtTrip.getDepartureFacility(), estimatePtTrip.getArrivalFacility(), estimatePtTrip.getDepartureTime(), scenario.getPopulation().getPersons().get(event.getPersonId()));
                    var ptTravelTime = planElements.stream().filter(planElement -> (planElement instanceof Leg)).mapToDouble(planElement -> ((Leg) planElement).getTravelTime()).sum();
                    estimatePtTrip.setPtTravelTime(ptTravelTime);
                    logger.info("Calculate a new PtTrip (departure time : " + estimatePtTrip.getDepartureTime() + ") for agent " + event.getPersonId() + ", result of ptTravelTime is " + ptTravelTime + ".");
               }else {
                    logger.info("ptTrip (departure time : " + estimatePtTrip.getDepartureTime() + ") for agent " + event.getPersonId() + " already has been calculated with a ptTravelTime " + estimatePtTrip.getPtTravelTime() + ".");
                }
                estimatePtTrip.setRealDrtTravelTime(drtTrip.getTotalTripTime());
                double ratio = estimatePtTrip.getPtTravelTime() / drtTrip.getTotalTripTime();
                double ratioThreshold = this.smartDrtFareConfigGroup.getRatioThreshold();

                if ( ratio > ratioThreshold) {
                    logger.info("person with peronId " + event.getPersonId() + " would not get a penalty for using drt: ptTravelTime = " + estimatePtTrip.getPtTravelTime() + " unsharedDrtTravelTime = " + drtTrip.getTotalTripTime() + ".");
                } else {
                    // pt is faster than DRT --> add fare penalty
                    //TODO optimize the penalty system
                    double penalty = this.smartDrtFareConfigGroup.getPenalty();
                    events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), -penalty));
                    estimatePtTrip.setPenalty(penalty);
                    logger.info("person with peronId " + event.getPersonId() + " would get a penalty for using drt: ptTravelTime = " + estimatePtTrip.getPtTravelTime() + " unsharedDrtTravelTime = " + drtTrip.getTotalTripTime() + ".");
                }
                this.personId2drtTripInfoCollector.remove(event.getPersonId());
            }
        }

    }

    @Override
    public void handleEvent(DrtRequestSubmittedEvent event) {
        // store agent Id who really used drt
        if (this.smartDrtFareConfigGroup.getDrtMode().equals(event.getMode()) && this.personId2drtTripInfoCollector.containsKey(event.getPersonId())) {
            var drtTripInfo = this.personId2drtTripInfoCollector.get(event.getPersonId());
            drtTripInfo.setDrtRequestSubmittedEvent(event);
            drtTripInfo.setDrtTrip(true);
        }
    }

    @Override
    public void handleEvent(ActivityEndEvent event) {
        // this is a real person
        if (scenario.getPopulation().getPersons().containsKey(event.getPersonId())) {
            // store real activity_end time, regardless if this agent will use drt
            if (!event.getActType().contains("interaction")) {
                this.personId2drtTripInfoCollector.put(event.getPersonId(), new RealDrtTripInfo(event));
            }
        }
    }

    public void writeLog(){
        String runOutputDirectory = this.scenario.getConfig().controler().getOutputDirectory();
        if (!runOutputDirectory.endsWith("/")) runOutputDirectory = runOutputDirectory.concat("/");

        String fileName = runOutputDirectory + "ITERS/it." + currentIteration + "/" + this.scenario.getConfig().controler().getRunId() + "." + currentIteration + ".info_" + this.getClass().getName() + ".csv";
        File file = new File(fileName);

        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write("it,PersonId,DepartureLink,ArrivalLink,departureTime,UnsharedDrtTime,EstimatePtTime,penalty");
            for (Id<Person> personId : this.personId2estimatePtTrips.keySet()) {
                for(EstimatePtTrip estimatePtTrip : this.personId2estimatePtTrips.get(personId)){
                    bw.newLine();
                    bw.write(this.currentIteration + "," +
                            personId + "," +
                            estimatePtTrip.getDepartureLinkId() + "," +
                            estimatePtTrip.getArrivalLinkId() + "," +
                            estimatePtTrip.getDepartureTime() + "," +
                            estimatePtTrip.getRealDrtTravelTime() + "," +
                            estimatePtTrip.getPtTravelTime() + "," +
                            estimatePtTrip.getPenalty());
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

