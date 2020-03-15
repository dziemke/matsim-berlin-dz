package org.matsim.run.drt.smartPricing.prepare;

import org.matsim.api.core.v01.events.ActivityEndEvent;
import org.matsim.api.core.v01.events.PersonArrivalEvent;
import org.matsim.contrib.drt.passenger.events.DrtRequestSubmittedEvent;

/**
 * @author : zmeng
 * @date :
 */
public class RealDrtTripInfo {
    private ActivityEndEvent realActivityEndEvent;
    private DrtRequestSubmittedEvent drtRequestSubmittedEvent;
    private PersonArrivalEvent drtArrivalEvent;
    private PersonArrivalEvent lastArrivalEvent;
    private boolean isDrtTrip = false;
    private boolean findDrtArrivalEvent = false;

    public RealDrtTripInfo(ActivityEndEvent realActivityEndEvent) {
        this.realActivityEndEvent = realActivityEndEvent;
    }

    public boolean isLastArrivalEvent() {
        return this.isDrtTrip() && this.findDrtArrivalEvent;
    }

    public ActivityEndEvent getRealActivityEndEvent() {
        return realActivityEndEvent;
    }

    public DrtRequestSubmittedEvent getDrtRequestSubmittedEvent() {
        return drtRequestSubmittedEvent;
    }

    public void setDrtRequestSubmittedEvent(DrtRequestSubmittedEvent drtRequestSubmittedEvent) {
        this.drtRequestSubmittedEvent = drtRequestSubmittedEvent;
    }

    public PersonArrivalEvent getLastArrivalEvent() {
        return lastArrivalEvent;
    }

    public void setLastArrivalEvent(PersonArrivalEvent lastArrivalEvent) {
        this.lastArrivalEvent = lastArrivalEvent;
    }

    public boolean isDrtTrip() {
        return isDrtTrip;
    }

    public void setDrtTrip(boolean drtTrip) {
        isDrtTrip = drtTrip;
    }

    public void setFindDrtArrivalEvent(boolean findDrtArrivalEvent) {
        this.findDrtArrivalEvent = findDrtArrivalEvent;
    }

    public void setDrtArrivalEvent(PersonArrivalEvent drtArrivalEvent) {
        this.drtArrivalEvent = drtArrivalEvent;
    }

    public double getWalkTime() {
        return (drtRequestSubmittedEvent.getTime() - realActivityEndEvent.getTime())
                + (lastArrivalEvent.getTime() - drtArrivalEvent.getTime());
    }

    public double getTotalTripTime() {
        return this.getWalkTime() + this.drtRequestSubmittedEvent.getUnsharedRideTime();
    }
    public double getRealDrtTotalTripTime(){
        return this.lastArrivalEvent.getTime() - realActivityEndEvent.getTime();
    }
}
