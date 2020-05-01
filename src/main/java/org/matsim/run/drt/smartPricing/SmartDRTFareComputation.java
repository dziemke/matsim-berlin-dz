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
import org.matsim.contrib.av.robotaxi.fares.drt.DrtFaresConfigGroup;
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
    private TripRouter tripRouter;
    @Inject
    private SmartDrtFareConfigGroup smartDrtFareConfigGroup;
    @Inject
    private DrtFaresConfigGroup drtFaresConfigGroup;

    private int currentIteration;
    private BufferedWriter bw = null;
    private Map<Id<Person>, RealDrtTripInfo> personId2drtTripInfoCollector = new HashMap<>();
    private Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTrips = new HashMap<>();
    private Map<Id<Person>, List<EstimatePtTrip>> personId2estimatePtTripsCurrentIt = new HashMap<>();
    private int newCalculatedNumOfPtTrips= 0;
    private int numOfHasPenaltyDrtUsers = 0;
    private int numOfHasRewardDrtUsers = 0;
    private int totalDrtUsers = 0;

    @Override
    public void reset(int iteration) {
        this.currentIteration = iteration;
        personId2drtTripInfoCollector.clear();
        personId2estimatePtTripsCurrentIt.clear();
        newCalculatedNumOfPtTrips = 0;
        numOfHasPenaltyDrtUsers = 0;
        numOfHasRewardDrtUsers = 0;
        totalDrtUsers = 0;
    }

    @Override
    public void handleEvent(PersonArrivalEvent event) {

        if (this.personId2drtTripInfoCollector.containsKey(event.getPersonId())) {
            RealDrtTripInfo drtTrip = this.personId2drtTripInfoCollector.get(event.getPersonId());
            if (event.getLegMode().equals(smartDrtFareConfigGroup.getDrtMode())) {
                drtTrip.setFindDrtArrivalEvent(true);
                drtTrip.setDrtArrivalEvent(event);
            } else if (drtTrip.isLastArrivalEvent()) {
                totalDrtUsers++;
                drtTrip.setLastArrivalEvent(event);

                if (!this.personId2estimatePtTrips.containsKey(event.getPersonId())) {
                    this.personId2estimatePtTrips.put(event.getPersonId(), new LinkedList<>());
                }

                double departureTime = drtTrip.getRealActivityEndEvent().getTime();
                Id<Link> departureLinkID = drtTrip.getRealActivityEndEvent().getLinkId();
                Id<Link> arrivalLinkID = drtTrip.getLastArrivalEvent().getLinkId();

                List<EstimatePtTrip> ptTrips = personId2estimatePtTrips.get(event.getPersonId());

                EstimatePtTrip temEstimatePtTrip = new EstimatePtTrip(scenario, departureLinkID, arrivalLinkID, departureTime);
                EstimatePtTrip estimatePtTrip = temEstimatePtTrip.updatePtTrips(ptTrips);

                if (!estimatePtTrip.isHasPtTravelTime()) {
                    estimatePtTrip.setHasPtTravelTime(true);
                    List planElements = tripRouter.calcRoute(TransportMode.pt, estimatePtTrip.getDepartureFacility(), estimatePtTrip.getArrivalFacility(), estimatePtTrip.getDepartureTime(), scenario.getPopulation().getPersons().get(event.getPersonId()));
                    double ptTravelTime = planElements.stream().filter(planElement -> (planElement instanceof Leg)).mapToDouble(planElement -> ((Leg) planElement).getTravelTime()).sum();
                    estimatePtTrip.setPtTravelTime(ptTravelTime);
                    newCalculatedNumOfPtTrips++;
                }
                estimatePtTrip.setUnsharedDrtTime(drtTrip.getTotalUnsharedTripTime());
                estimatePtTrip.setArrivalTime(drtTrip.getLastArrivalEvent().getTime());
                estimatePtTrip.setUnsharedDrtDistance(drtTrip.getDrtRequestSubmittedEvent().getUnsharedRideDistance());
                double ratio = estimatePtTrip.getPtTravelTime() / drtTrip.getTotalUnsharedTripTime();
                estimatePtTrip.setRatio(ratio);
                double x = estimatePtTrip.getUnsharedDrtDistance() / 1000;

                double penaltyRatioThreshold = this.smartDrtFareConfigGroup.getPenaltyRatioThresholdFactorA()*Math.pow(x,3) +
                        this.smartDrtFareConfigGroup.getPenaltyRatioThresholdFactorB()*Math.pow(x,2) +
                        this.smartDrtFareConfigGroup.getPenaltyRatioThresholdFactorC() * x +
                        this.smartDrtFareConfigGroup.getPenaltyRatioThreshold();

                double rewardRatioThreshold = this.smartDrtFareConfigGroup.getRewardRatioThresholdFactorA()*Math.pow(x,3) +
                        this.smartDrtFareConfigGroup.getRewardRatioThresholdFactorB()*Math.pow(x,2) +
                        this.smartDrtFareConfigGroup.getRewardRatioThresholdFactorC() * x +
                        this.smartDrtFareConfigGroup.getRewardRatioThreshold();

                estimatePtTrip.setPenaltyRatioThreshold(penaltyRatioThreshold);
                estimatePtTrip.setRewardRatioThreshold(rewardRatioThreshold);
                double baseDistanceFare = this.drtFaresConfigGroup.getDrtFareConfigGroups().stream().filter(drtFareConfigGroup -> drtFareConfigGroup.getMode().equals(TransportMode.drt)).collect(Collectors.toList()).get(0).getDistanceFare_m();
                double baseTripFare = this.drtFaresConfigGroup.getDrtFareConfigGroups().stream().filter(drtFareConfigGroup -> drtFareConfigGroup.getMode().equals(TransportMode.drt)).collect(Collectors.toList()).get(0).getBasefare();
                if(estimatePtTrip.getUnsharedDrtDistance() <= smartDrtFareConfigGroup.getMaxDrtDistance()){
                    if ( ratio <= penaltyRatioThreshold) {
                        // pt is faster than DRT --> add fare penalty
                        double penaltyPerMeter = this.smartDrtFareConfigGroup.getPenaltyFactor() * baseDistanceFare * penaltyRatioThreshold / ratio - baseDistanceFare;
                        double penalty = drtTrip.getDrtRequestSubmittedEvent().getUnsharedRideDistance() * penaltyPerMeter;
                        events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), -penalty,"penalty","drt"));
                        estimatePtTrip.setPenaltyPerMeter(penaltyPerMeter);
                        estimatePtTrip.setPenalty(penalty);
                        numOfHasPenaltyDrtUsers++;
                    } else if(ratio >= rewardRatioThreshold) {
                        double rewardPerMeter = Math.min(this.smartDrtFareConfigGroup.getRewardFactor()*baseDistanceFare,
                                this.smartDrtFareConfigGroup.getRewardFactor() * baseDistanceFare * (ratio/rewardRatioThreshold - 1));
                        double reward = drtTrip.getDrtRequestSubmittedEvent().getUnsharedRideDistance() * rewardPerMeter;
                        events.processEvent(new PersonMoneyEvent(event.getTime(), event.getPersonId(), reward,"reward","drt"));
                        estimatePtTrip.setReward(reward);
                        estimatePtTrip.setRewardPerMeter(rewardPerMeter);
                        numOfHasRewardDrtUsers++;
                    }
                }
                if(!this.personId2estimatePtTripsCurrentIt.containsKey(event.getPersonId())){
                    this.personId2estimatePtTripsCurrentIt.put(event.getPersonId(),new LinkedList<>());
                }
                this.personId2estimatePtTripsCurrentIt.get(event.getPersonId()).add(estimatePtTrip);
                this.personId2drtTripInfoCollector.remove(event.getPersonId());
            }
        }

    }

    @Override
    public void handleEvent(DrtRequestSubmittedEvent event) {
        // store agent Id who really used drt
        if (this.smartDrtFareConfigGroup.getDrtMode().equals(event.getMode()) && this.personId2drtTripInfoCollector.containsKey(event.getPersonId())) {
            RealDrtTripInfo drtTripInfo = this.personId2drtTripInfoCollector.get(event.getPersonId());
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

    public void writeFile(){
        String runOutputDirectory = this.scenario.getConfig().controler().getOutputDirectory();
        if (!runOutputDirectory.endsWith("/")) runOutputDirectory = runOutputDirectory.concat("/");

        String fileName = runOutputDirectory + "ITERS/it." + currentIteration + "/" + this.scenario.getConfig().controler().getRunId() + "." + currentIteration + ".info_" + this.getClass().getName() + ".csv";
        File file = new File(fileName);

        try {
            bw = new BufferedWriter(new FileWriter(file));
            bw.write("it,personId,departureLink,arrivalLink,departureTime,arrivalTime,unsharedDrtTime,unsharedDrtDistance,EstimatePtTime,ratio,penalty_meter,penalty,penaltyRatioThreshold,reward_meter,reward,rewardRatioThreshold");
            for (Id<Person> personId : this.personId2estimatePtTripsCurrentIt.keySet()) {
                for(EstimatePtTrip estimatePtTrip : this.personId2estimatePtTripsCurrentIt.get(personId)){
                    bw.newLine();
                    bw.write(this.currentIteration + "," +
                            personId + "," +
                            estimatePtTrip.getDepartureLinkId() + "," +
                            estimatePtTrip.getArrivalLinkId() + "," +
                            estimatePtTrip.getDepartureTime() + "," +
                            estimatePtTrip.getArrivalTime() + "," +
                            estimatePtTrip.getUnsharedDrtTime() + "," +
                            estimatePtTrip.getUnsharedDrtDistance() + "," +
                            estimatePtTrip.getPtTravelTime() + "," +
                            estimatePtTrip.getRatio() + "," +
                            estimatePtTrip.getPenaltyPerMeter() + "," +
                            estimatePtTrip.getPenalty() + "," +
                            estimatePtTrip.getPenaltyRatioThreshold() + "," +
                            estimatePtTrip.getRewardPerMeter() + "," +
                            estimatePtTrip.getReward() + "," +
                            estimatePtTrip.getRewardRatioThreshold());
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeLog() {
        // num of drt Users
        // num of new calculated pt Travel Time
        // num of has penalty drt users
        logger.info("num of drt users: "+totalDrtUsers+
                " num of new calculated ptTravelTime " + newCalculatedNumOfPtTrips +
                " num of has penalty drt trips: " + numOfHasPenaltyDrtUsers +
                "num of has reward drt trips: " +numOfHasRewardDrtUsers);
    }
}

