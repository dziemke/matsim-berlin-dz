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

package org.matsim.run.drt.smartPricing.prepare;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.facilities.Facility;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : zmeng
 * @date :
 */
public class EstimatePtTrip {

    private Id<Link> departureLinkId;
    private Id<Link> arrivalLinkId;
    private double departureTime;
    private double arrivalTime;
    private double ptTravelTime;
    private boolean hasPtTravelTime = false;
    private Facility departureFacility;
    private Facility arrivalFacility;
    private double penalty;
    private double penaltyPerMeter;
    private double reward;
    private double rewardPerMeter;
    private double unsharedDrtTime;
    private double unsharedDrtDistance;
    private double ratio;
    private double penaltyRatioThreshold;
    private double rewardRatioThreshold;



    public EstimatePtTrip(Scenario scenario, Id<Link> departureLinkId, Id<Link> arrivalLinkId, double departureTime) {
        this.departureLinkId = departureLinkId;
        this.arrivalLinkId = arrivalLinkId;
        this.departureTime = departureTime;

        departureFacility = new Facility() {
            @Override
            public Id<Link> getLinkId() {
                return departureLinkId;
            }

            @Override
            public Coord getCoord() {
                return scenario.getNetwork().getLinks().get(departureLinkId).getCoord();
            }

            @Override
            public Map<String, Object> getCustomAttributes() {
                return null;
            }
        };

        arrivalFacility = new Facility() {
            @Override
            public Id<Link> getLinkId() {
                return arrivalLinkId;
            }

            @Override
            public Coord getCoord() {
                return scenario.getNetwork().getLinks().get(arrivalLinkId).getCoord();
            }

            @Override
            public Map<String, Object> getCustomAttributes() {
                return null;
            }
        };
    }

    private boolean isSameTrip(EstimatePtTrip estimatePtTrip) {
        return (this.arrivalLinkId == estimatePtTrip.getArrivalLinkId() && this.departureLinkId == estimatePtTrip.getDepartureLinkId());
    }

    private boolean hasSameDepartureTime(EstimatePtTrip estimatePtTrip) {
        return this.departureTime == estimatePtTrip.departureTime;
    }

    public boolean isHasPtTravelTime() {
        return hasPtTravelTime;
    }

    public EstimatePtTrip updatePtTrips(List<EstimatePtTrip> estimatePtTrips) throws RuntimeException {
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

    public void setPenalty(double penalty) {
        this.penalty = penalty;
    }

    public double getPenalty() {
        return penalty;
    }

    public void setPenaltyPerMeter(double penaltyPerMeter) { this.penaltyPerMeter = penaltyPerMeter;}

    public double getPenaltyPerMeter(){return penaltyPerMeter;}

    public double getUnsharedDrtTime() {
        return unsharedDrtTime;
    }

    public void setUnsharedDrtTime(double unsharedDrtTime) {
        this.unsharedDrtTime = unsharedDrtTime;
    }

    public Id<Link> getDepartureLinkId() {
        return departureLinkId;
    }

    public Id<Link> getArrivalLinkId() {
        return arrivalLinkId;
    }

    public double getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(double departureTime) {
        this.departureTime = departureTime;
    }

    public double getPtTravelTime() {
        return ptTravelTime;
    }

    public void setPtTravelTime(double ptTravelTime) {
        if (ptTravelTime <= 0.0) {
            this.ptTravelTime = 0.001;
        } else {
            this.ptTravelTime = ptTravelTime;
        }
    }

    public void setHasPtTravelTime(boolean hasPtTravelTime) {
        this.hasPtTravelTime = hasPtTravelTime;
    }

    public Facility getDepartureFacility() {
        return departureFacility;
    }

    public Facility getArrivalFacility() {
        return arrivalFacility;
    }

    public double getArrivalTime() { return arrivalTime; }

    public void setArrivalTime(double arrivalTime) { this.arrivalTime = arrivalTime; }

    public double getUnsharedDrtDistance() { return unsharedDrtDistance; }

    public void setUnsharedDrtDistance(double unsharedDrtDistance) { this.unsharedDrtDistance = unsharedDrtDistance; }

    public void setRatio(double ratio) { this.ratio = ratio; }

    public double getRatio() { return this.ratio;}

    public void setPenaltyRatioThreshold(double penaltyRatioThreshold) {this.penaltyRatioThreshold = penaltyRatioThreshold;}

    public double getPenaltyRatioThreshold() { return penaltyRatioThreshold; }

    public double getRewardRatioThreshold() { return rewardRatioThreshold; }

    public void setRewardRatioThreshold(double rewardRatioThreshold) { this.rewardRatioThreshold = rewardRatioThreshold; }

    public double getReward() { return reward; }

    public void setReward(double reward) { this.reward = reward; }

    public double getRewardPerMeter() { return rewardPerMeter; }

    public void setRewardPerMeter(double rewardPerMeter) { this.rewardPerMeter = rewardPerMeter; }
}
