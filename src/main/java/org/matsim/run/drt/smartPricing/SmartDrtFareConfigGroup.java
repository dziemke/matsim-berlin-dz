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

import org.matsim.core.config.ReflectiveConfigGroup;

/**
 * @author : zmeng
 * @date : 19.Feb
 */
public class SmartDrtFareConfigGroup extends ReflectiveConfigGroup {
    public static final String GROUP_NAME = "smartDrtPricing";
    private static final String DRT_MODE = "drtMode";
    private static final String PENALTY_FACTOR = "penalty_factor";
    private static final String RATIO_THRESHOLD = "ratioThreshold";
    private static final String WRITE_FILE_INTERVAL = "writeFileInterval";
    private static final String FARE_TIME_BIN_SIZE = "fareTimeBinSize";
    private static final String COST_PER_VEHICLE_PER_SECOND = "costPerVehiclePerSecond";
    private static final String COST_PER_VEHICLE_PER_METER = "costPerVehPerMeter";
    private static final String RATIO_THRESHOLD_FACTOR_A = "ratioThresholdFactorA";
    private static final String RATIO_THRESHOLD_FACTOR_B = "ratioThresholdFactorB";
    private static final String RATIO_THRESHOLD_FACTOR_C = "ratioThresholdFactorC";

    public SmartDrtFareConfigGroup() {
        super(GROUP_NAME);
    }

    private double costPerVehPerMeter = 1.079*6.7 / 100 / 1000.;
    private double costPerVehiclePerSecond = 0.5 /900. ;
    private double fareTimeBinSize = 129601;
    private String drtMode = "drt";
    private double penaltyFactor = 1;
    private double ratioThreshold = 0;
    private double ratioThresholdFactorA = 0;
    private double ratioThresholdFactorB = 0;
    private double ratioThresholdFactorC = 0;
    private int writeFileInterval = 1;

    @StringSetter(WRITE_FILE_INTERVAL)
    public void setWriteFileInterval(int writeFileInterval) { this.writeFileInterval = writeFileInterval; }
    @StringGetter(WRITE_FILE_INTERVAL)
    public int getWriteFileInterval() { return writeFileInterval; }
    @StringGetter(DRT_MODE)
    public String getDrtMode(){ return drtMode; }
    @StringSetter(DRT_MODE)
    public void setDrtMode(String drtMode) { this.drtMode = drtMode; }
    @StringGetter(PENALTY_FACTOR)
    public double getPenaltyFactor() { return penaltyFactor; }
    @StringSetter(PENALTY_FACTOR)
    public void setPenalty(double penaltyFactor) { this.penaltyFactor = penaltyFactor; }
    @StringGetter(RATIO_THRESHOLD)
    public double getRatioThreshold() { return ratioThreshold; }
    @StringSetter(RATIO_THRESHOLD)
    public void setRatioThreshold(double ratioThreshold) { this.ratioThreshold = ratioThreshold; }
    @StringGetter(COST_PER_VEHICLE_PER_METER)
    public double getCostPerVehPerMeter() { return costPerVehPerMeter; }
    @StringSetter(COST_PER_VEHICLE_PER_METER)
    public void setCostPerVehPerMeter(double costPerVehPerMeter) { this.costPerVehPerMeter = costPerVehPerMeter; }
    @StringGetter(COST_PER_VEHICLE_PER_SECOND)
    public double getCostPerVehiclePerSecond() { return costPerVehiclePerSecond; }
    @StringSetter(COST_PER_VEHICLE_PER_SECOND)
    public void setCostPerVehiclePerSecond(double costPerVehiclePerSecond) { this.costPerVehiclePerSecond = costPerVehiclePerSecond; }
    @StringGetter(FARE_TIME_BIN_SIZE)
    public double getFareTimeBinSize() { return fareTimeBinSize; }
    @StringSetter(FARE_TIME_BIN_SIZE)
    public void setFareTimeBinSize(double fareTimeBinSize) { this.fareTimeBinSize = fareTimeBinSize; }
    @StringGetter(RATIO_THRESHOLD_FACTOR_A)
    public double getRatioThresholdFactorA() { return ratioThresholdFactorA; }
    @StringSetter(RATIO_THRESHOLD_FACTOR_A)
    public void setRatioThresholdFactorA(double ratioThresholdFactorA) { this.ratioThresholdFactorA = ratioThresholdFactorA; }
    @StringGetter(RATIO_THRESHOLD_FACTOR_B)
    public double getRatioThresholdFactorB() { return ratioThresholdFactorB; }
    @StringSetter(RATIO_THRESHOLD_FACTOR_B)
    public void setRatioThresholdFactorB(double ratioThresholdFactorB) { this.ratioThresholdFactorB = ratioThresholdFactorB; }
    @StringGetter(RATIO_THRESHOLD_FACTOR_C)
    public double getRatioThresholdFactorC() { return ratioThresholdFactorC; }
    @StringSetter(RATIO_THRESHOLD_FACTOR_C)
    public void setRatioThresholdFactorC(double ratioThresholdFactorC) { this.ratioThresholdFactorC = ratioThresholdFactorC; }

}
