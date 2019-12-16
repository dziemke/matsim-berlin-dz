package org.matsim.prepare.population.reLocation;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.matsim.facilities.*;
import org.opengis.feature.simple.SimpleFeature;

import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FacilitiesForAllTimes {

    public static void main(String[] args) {

        Config config = ConfigUtils.createConfig();
        Scenario scenario = ScenarioUtils.createScenario(config);

        Scenario scenario1 = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        ActivityFacilities activityFacilities = scenario1.getActivityFacilities();
        ActivityFacilitiesFactory activityFacilitiesFactory = new ActivityFacilitiesFactoryImpl();

        MatsimFacilitiesReader matsimFacilitiesReader = new MatsimFacilitiesReader(scenario);
        matsimFacilitiesReader.readFile("D://Arbeit/Berlin/ReLocation/combinedFacilities_BB_BE.xml.gz");
//        matsimFacilitiesReader.readFile("./combinedFacilities_BB_BE.xml.gz");

        long idforFacilities = 0;

        Collection<SimpleFeature> features = ShapeFileReader.getAllFeatures("D://Arbeit/Berlin/ReLocation/debug frozen tastes/shape/shapeTeilBerlin.shp");
        Map<String, Geometry> districts = new HashMap<>();

        for (SimpleFeature feature : features) {
            String id = feature.getID();
            Geometry geometry = (Geometry) feature.getDefaultGeometry();
            districts.put(id, geometry);
        }

        for (ActivityFacility facility : scenario.getActivityFacilities().getFacilities().values()) {
            Coord coord = facility.getCoord();
            Map<String, ActivityOption> activityOptions = facility.getActivityOptions();
            if (inDistrict(districts, coord)) {
                for (ActivityOption x : activityOptions.values()) {
                    if (x.getType().equals("shop")) {
                        ActivityFacility newFacility = activityFacilitiesFactory.createActivityFacility(Id.create(idforFacilities++, ActivityFacility.class), coord);
//                    for (long ii = 600; ii <= 97200; ii += 600) {
//                        String type = "shopping_" + ii + ".0";
//                        newFacility.addActivityOption(activityFacilitiesFactory.createActivityOption(type));
//                    }
                        newFacility.addActivityOption(activityFacilitiesFactory.createActivityOption("shopping"));
                        activityFacilities.addActivityFacility(newFacility);
                    }
                    if (x.getType().equals("leisure")) {
                        ActivityFacility newFacility = activityFacilitiesFactory.createActivityFacility(Id.create(idforFacilities++, ActivityFacility.class), coord);
//                    for (long ii = 600; ii <= 97200; ii += 600) {
//                        String type = "leisure_" + ii + ".0";
//                        newFacility.addActivityOption(activityFacilitiesFactory.createActivityOption(type));
//                    }
                        newFacility.addActivityOption(activityFacilitiesFactory.createActivityOption("leisure"));
                        activityFacilities.addActivityFacility(newFacility);
                    }
                }
            }
        }
        new FacilitiesWriter(activityFacilities).write("D://Arbeit/Berlin/ReLocation/facilitiesOpenBerlinDebug.xml.gz");
        System.out.println("Done");
    }

    public static boolean inDistrict(Map<String, Geometry> allZones, Coord coord) {
        Point point = MGC.coord2Point(coord);
        for (String nameZone : allZones.keySet()) {
            Geometry geo = allZones.get(nameZone);
            if (geo.contains(point)) {
                return true;
            }
        }
        return false;
    }
}
