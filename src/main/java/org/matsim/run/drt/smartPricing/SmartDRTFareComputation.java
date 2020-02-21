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

import ch.sbb.matsim.routing.pt.raptor.SwissRailRaptor;
import com.google.inject.Inject;
import lombok.*;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.api.core.v01.events.PersonDepartureEvent;
import org.matsim.api.core.v01.events.PersonMoneyEvent;
import org.matsim.api.core.v01.events.handler.ActivityEndEventHandler;
import org.matsim.api.core.v01.events.handler.PersonArrivalEventHandler;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEvent;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEventHandler;
import org.matsim.core.api.experimental.events.EventsManager;
import org.matsim.facilities.Facility;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private SwissRailRaptor swissRailRaptor;
    @Inject
    private SmartDrtFareConfigGroup smartDrtFareConfigGroup;

    private BufferedWriter bw = null;
    private int currentIteration;
    private Map<Id<Person>, DrtTripInfoCollector> personId2drtTripInfoCollector = new HashMap<>();
    private Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTrips = new HashMap<>();

    @SneakyThrows
    @Override
    public void reset(int iteration) {
        this.currentIteration = iteration;
        if (this.smartDrtFareConfigGroup.isWriteLog() && bw == null) {
            File file = new File(scenario.getConfig().controler().getOutputDirectory() + "smartDrtFareComputationLog.csv");
            bw = new BufferedWriter(new FileWriter(file));
            logger.info("begin to write smartDrtFareLog to: "+ file.getCanonicalPath());
            bw.write("it,PersonId,DepartureLink,ArrivalLink,departureTime,UnsharedDrtTime,EstimatePtTime,Calculated,penalty");
        }
    }

    @SneakyThrows
    @Override
    public void handleEvent(PersonArrivalEvent event) {
        if (this.personId2drtTripInfoCollector.containsKey(event.getPersonId())) {
            var drtTrip = this.personId2drtTripInfoCollector.get(event.getPersonId());
            if (event.getLegMode().equals(smartDrtFareConfigGroup.getDrtMode())) {
                drtTrip.setFindDrtArrivalEvent(true);
            } else if (drtTrip.isLastArrivalEvent()) {
                drtTrip.setLastArrivalEvent(event);

                if (!this.personId2estimatePtTrips.containsKey(event.getPersonId())) {
                    this.personId2estimatePtTrips.put(event.getPersonId(), new LinkedList<>());
                }

                double departureTime = drtTrip.getRealActivityEndEvent().getTime();
                Id<Link> departureLinkID = drtTrip.getRealActivityEndEvent().getLinkId();
                Id<Link> arrivalLinkID = drtTrip.getLastArrivalEvent().getLinkId();

                var ptTrips = personId2estimatePtTrips.get(event.getPersonId());

                EstimatePtTrip temEstimatePtTrip = new EstimatePtTrip(departureLinkID, arrivalLinkID, departureTime);
                EstimatePtTrip estimatePtTrip = temEstimatePtTrip.updatePtTrips(ptTrips);

                if (!estimatePtTrip.hasPtTravelTime) {
                    estimatePtTrip.setHasPtTravelTime(true);
                    var legs = swissRailRaptor.calcRoute(estimatePtTrip.getDepartureFacility(), estimatePtTrip.getArrivalFacility(), estimatePtTrip.getDepartureTime(), scenario.getPopulation().getPersons().get(event.getPersonId()));
                    if (legs != null) {
                        var ptTravelTime = legs.stream().mapToDouble(Leg::getTravelTime).sum();
                        estimatePtTrip.setPtTravelTime(ptTravelTime);
                        logger.info("Calculate a new PtTrip (departure time : " + estimatePtTrip.getDepartureTime() + ") for agent " + event.getPersonId() + ", result of ptTravelTime is " + ptTravelTime + ".");
                    } else {
                        double ptTravelTime = -1.;
                        estimatePtTrip.setPtTravelTime(ptTravelTime);
                        logger.info("Calculate a new PtTrip (departure time : " + estimatePtTrip.getDepartureTime() + ") for agent " + event.getPersonId() + ", result of ptTravelTime is none of ptRoute is found.");
                    }
                    writeLine(event, drtTrip, estimatePtTrip, true);

                } else {
                    logger.info("ptTrip (departure time : " + estimatePtTrip.getDepartureTime() + ") for agent " + event.getPersonId() + " already has been calculated with a ptTravelTime " + estimatePtTrip.getPtTravelTime() + ".");
                    writeLine(event, drtTrip, estimatePtTrip, false);
                }

                double ratio = estimatePtTrip.getPtTravelTime() / drtTrip.getDrtRequestSubmittedEvent().getUnsharedRideTime();
                double ratioThreshold = this.smartDrtFareConfigGroup.getRatioThreshold();

                if (ratio < 0 || ratio > ratioThreshold) {
                    // no DRT penalty
                    writeBoolean(false);
                    logger.info("person with peronId " + event.getPersonId() + " would not get a penalty for using drt: ptTravelTime = " + estimatePtTrip.getPtTravelTime() + " unsharedDrtTravelTime = " + drtTrip.getDrtRequestSubmittedEvent().getUnsharedRideTime() + ".");
                } else {
                    // pt is faster than DRT --> add fare penalty
                    //TODO optimize the penalty system
                    double penalty = this.smartDrtFareConfigGroup.getPenalty();
                    events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), -penalty));
                    writeBoolean(true);
                    logger.info("person with peronId " + event.getPersonId() + " would get a penalty for using drt: ptTravelTime = " + estimatePtTrip.getPtTravelTime() + " unsharedDrtTravelTime = " + drtTrip.getDrtRequestSubmittedEvent().getUnsharedRideTime() + ".");
                }
                if(smartDrtFareConfigGroup.isWriteLog())
                    bw.flush();

                this.personId2drtTripInfoCollector.remove(event.getPersonId());
            }
        }

    }

    private void writeBoolean(boolean b) throws IOException {
        if(!smartDrtFareConfigGroup.isWriteLog())
            return;
        bw.write("," + b);
    }

    private void writeLine(PersonArrivalEvent event, DrtTripInfoCollector drtTrip, EstimatePtTrip estimatePtTrip, boolean b) throws IOException {
        if(!smartDrtFareConfigGroup.isWriteLog())
            return;
        bw.newLine();
        bw.write(this.currentIteration + "," + event.getPersonId() + "," +
                estimatePtTrip.getDepartureLinkId() + "," +
                estimatePtTrip.getArrivalLinkId() + "," +
                estimatePtTrip.getDepartureTime() + "," +
                drtTrip.getDrtRequestSubmittedEvent().getUnsharedRideTime() + "," +
                estimatePtTrip.getPtTravelTime());
        writeBoolean(b);
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
                this.personId2drtTripInfoCollector.put(event.getPersonId(), new DrtTripInfoCollector(event));
            }
        }
    }

    @RequiredArgsConstructor
    @ToString
    @Setter
    @Getter
    private static class DrtTripInfoCollector {
        @NonNull
        private ActivityEndEvent realActivityEndEvent;
        private DrtRequestSubmittedEvent drtRequestSubmittedEvent;
        private PersonDepartureEvent drtDepartureEvent;
        private PersonArrivalEvent lastArrivalEvent;
        private boolean isDrtTrip = false;
        private boolean findDrtArrivalEvent = false;

        private boolean isLastArrivalEvent() {
            return this.isDrtTrip() && this.findDrtArrivalEvent;
        }
    }

    @Getter
    @ToString
    class EstimatePtTrip {
        private Id<Link> departureLinkId;
        private Id<Link> arrivalLinkId;
        private double departureTime;
        @Setter
        private double ptTravelTime;
        @ToString.Exclude
        @Setter
        private boolean hasPtTravelTime = false;
        @ToString.Exclude
        private Facility departureFacility;
        @ToString.Exclude
        private Facility arrivalFacility;


        public EstimatePtTrip(Id<Link> departureLinkId, Id<Link> arrivalLinkId, double departureTime) {
            this.departureLinkId = departureLinkId;
            this.arrivalLinkId = arrivalLinkId;
            this.departureTime = departureTime;

            departureFacility = new Facility() {
                @Override
                public Id<Link> getLinkId() { return departureLinkId; }

                @Override
                public Coord getCoord() { return scenario.getNetwork().getLinks().get(departureLinkId).getCoord(); }

                @Override
                public Map<String, Object> getCustomAttributes() { return null; }
            };

            arrivalFacility = new Facility() {
                @Override
                public Id<Link> getLinkId() { return arrivalLinkId; }

                @Override
                public Coord getCoord() { return scenario.getNetwork().getLinks().get(arrivalLinkId).getCoord(); }

                @Override
                public Map<String, Object> getCustomAttributes() { return null; }
            };
        }

        private boolean isSameTrip(EstimatePtTrip estimatePtTrip) {
            return (this.arrivalLinkId == estimatePtTrip.getArrivalLinkId() && this.departureLinkId == estimatePtTrip.getDepartureLinkId());
        }

        private boolean hasSameDepartureTime(EstimatePtTrip estimatePtTrip) {
            return this.departureTime == estimatePtTrip.departureTime;
        }

        private EstimatePtTrip updatePtTrips(List<EstimatePtTrip> estimatePtTrips) throws RuntimeException {
            var filteredTrips = estimatePtTrips.stream().filter(this::isSameTrip).collect(Collectors.toList());
            if (filteredTrips.size() == 0) {
                estimatePtTrips.add(this);
                return this;
            } else if (filteredTrips.size() == 1) {
                if (filteredTrips.get(0).hasSameDepartureTime(this)) {
                    return filteredTrips.get(0);
                } else {
                    estimatePtTrips.remove(filteredTrips.get(0));
                    estimatePtTrips.add(this);
                    return this;
                }
            } else {
                throw new RuntimeException("each agent can only store one ptTripsInfo for a trip, this agent has " + filteredTrips.size());
            }
        }
    }


}

