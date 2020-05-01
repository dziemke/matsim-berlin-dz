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
    private static final String MAX_DRT_DISTANCE = "maxDrtDistance";

    private static final String PENALTY_STRATEGY = "penaltyStrategy";
    private static final String PENALTY_FACTOR = "penaltyFactor";
    private static final String PENALTY_RATIO_THRESHOLD = "penaltyRatioThreshold";
    private static final String PENALTY_RATIO_THRESHOLD_FACTOR_A = "penaltyRatioThresholdFactorA";
    private static final String PENALTY_RATIO_THRESHOLD_FACTOR_B = "penaltyRatioThresholdFactorB";
    private static final String PENALTY_RATIO_THRESHOLD_FACTOR_C = "penaltyRatioThresholdFactorC";

    private static final String REWARD_STRATEGY = "rewardStrategy";
    private static final String REWARD_FACTOR = "rewardFactor";
    private static final String REWARD_RATIO_THRESHOLD = "rewardRatioThreshold";
    private static final String REWARD_RATIO_THRESHOLD_FACTOR_A = "rewardRatioThresholdFactorA";
    private static final String REWARD_RATIO_THRESHOLD_FACTOR_B = "rewardRatioThresholdFactorB";
    private static final String REWARD_RATIO_THRESHOLD_FACTOR_C = "rewardRatioThresholdFactorC";

    private static final String WRITE_FILE_INTERVAL = "writeFileInterval";
    private static final String FARE_TIME_BIN_SIZE = "fareTimeBinSize";
    private static final String COST_PER_VEHICLE_PER_SECOND = "costPerVehiclePerSecond";
    private static final String COST_PER_VEHICLE_PER_METER = "costPerVehPerMeter";

    public SmartDrtFareConfigGroup() {
        super(GROUP_NAME);
    }

    private double costPerVehPerMeter = 1.079*6.7 / 100 / 1000.;
    private double costPerVehiclePerSecond = 0.5 /900. ;
    private double fareTimeBinSize = 129601;
    private String drtMode = "drt";
    private double maxDrtDistance = 40000;

    private boolean penaltyStrategy = true;
    private double penaltyFactor = 1;
    private double penaltyRatioThreshold = 2.40349274e+00;
    private double penaltyRatioThresholdFactorA = -9.12263322e-06;
    private double penaltyRatioThresholdFactorB = 1.43513764e-03;
    private double penaltyRatioThresholdFactorC = -6.60221445e-02;

    private boolean rewardStrategy = true;
    private double rewardFactor = 0.4;
    private double rewardRatioThreshold = 3.34209421e+00;
    private double rewardRatioThresholdFactorA = -3.23284824e-06;
    private double rewardRatioThresholdFactorB = 8.58041740e-04;
    private double rewardRatioThresholdFactorC = -7.16475576e-02;

    private int writeFileInterval = 1;

    @StringSetter(WRITE_FILE_INTERVAL)
    public void setWriteFileInterval(int writeFileInterval) { this.writeFileInterval = writeFileInterval; }
    @StringGetter(WRITE_FILE_INTERVAL)
    public int getWriteFileInterval() { return writeFileInterval; }
    @StringGetter(DRT_MODE)
    public String getDrtMode(){ return drtMode; }
    @StringSetter(DRT_MODE)
    public void setDrtMode(String drtMode) { this.drtMode = drtMode; }
    @StringGetter(MAX_DRT_DISTANCE)
    public double getMaxDrtDistance() { return maxDrtDistance; }
    @StringSetter(MAX_DRT_DISTANCE)
    public void setMaxDrtDistance(double maxDrtDistance) { this.maxDrtDistance = maxDrtDistance; }

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

    @StringGetter(PENALTY_FACTOR)
    public double getPenaltyFactor() { return penaltyFactor; }
    @StringSetter(PENALTY_FACTOR)
    public void setPenalty(double penaltyFactor) { this.penaltyFactor = penaltyFactor; }
    @StringGetter(PENALTY_RATIO_THRESHOLD)
    public double getPenaltyRatioThreshold() { return penaltyRatioThreshold; }
    @StringSetter(PENALTY_RATIO_THRESHOLD)
    public void setPenaltyRatioThreshold(double penaltyRatioThreshold) { this.penaltyRatioThreshold = penaltyRatioThreshold; }
    @StringGetter(PENALTY_RATIO_THRESHOLD_FACTOR_A)
    public double getPenaltyRatioThresholdFactorA() { return penaltyRatioThresholdFactorA; }
    @StringSetter(PENALTY_RATIO_THRESHOLD_FACTOR_A)
    public void setPenaltyRatioThresholdFactorA(double penaltyRatioThresholdFactorA) { this.penaltyRatioThresholdFactorA = penaltyRatioThresholdFactorA; }
    @StringGetter(PENALTY_RATIO_THRESHOLD_FACTOR_B)
    public double getPenaltyRatioThresholdFactorB() { return penaltyRatioThresholdFactorB; }
    @StringSetter(PENALTY_RATIO_THRESHOLD_FACTOR_B)
    public void setPenaltyRatioThresholdFactorB(double penaltyRatioThresholdFactorB) { this.penaltyRatioThresholdFactorB = penaltyRatioThresholdFactorB; }
    @StringGetter(PENALTY_RATIO_THRESHOLD_FACTOR_C)
    public double getPenaltyRatioThresholdFactorC() { return penaltyRatioThresholdFactorC; }
    @StringSetter(PENALTY_RATIO_THRESHOLD_FACTOR_C)
    public void setPenaltyRatioThresholdFactorC(double penaltyRatioThresholdFactorC) { this.penaltyRatioThresholdFactorC = penaltyRatioThresholdFactorC; }

    @StringGetter(REWARD_FACTOR)
    public double getRewardFactor() { return rewardFactor; }
    @StringSetter(REWARD_FACTOR)
    public void setReward(double rewardFactor) { this.rewardFactor = rewardFactor; }
    @StringGetter(REWARD_RATIO_THRESHOLD)
    public double getRewardRatioThreshold() { return rewardRatioThreshold; }
    @StringSetter(REWARD_RATIO_THRESHOLD)
    public void setRewardRatioThreshold(double rewardRatioThreshold) { this.rewardRatioThreshold = rewardRatioThreshold; }
    @StringGetter(REWARD_RATIO_THRESHOLD_FACTOR_A)
    public double getRewardRatioThresholdFactorA() { return rewardRatioThresholdFactorA; }
    @StringSetter(REWARD_RATIO_THRESHOLD_FACTOR_A)
    public void setRewardRatioThresholdFactorA(double rewardRatioThresholdFactorA) { this.rewardRatioThresholdFactorA = rewardRatioThresholdFactorA; }
    @StringGetter(REWARD_RATIO_THRESHOLD_FACTOR_B)
    public double getRewardRatioThresholdFactorB() { return rewardRatioThresholdFactorB; }
    @StringSetter(REWARD_RATIO_THRESHOLD_FACTOR_B)
    public void setRewardRatioThresholdFactorB(double rewardRatioThresholdFactorB) { this.rewardRatioThresholdFactorB = rewardRatioThresholdFactorB; }
    @StringGetter(REWARD_RATIO_THRESHOLD_FACTOR_C)
    public double getRewardRatioThresholdFactorC() { return rewardRatioThresholdFactorC; }
    @StringSetter(REWARD_RATIO_THRESHOLD_FACTOR_C)
    public void setRewardRatioThresholdFactorC(double rewardRatioThresholdFactorC) { this.rewardRatioThresholdFactorC = rewardRatioThresholdFactorC; }

}
