package org.matsim.prepare.ptRouteTrim;

import org.apache.log4j.Logger;
import org.geotools.feature.SchemaException;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.TransitScheduleFactoryImpl;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.pt.utils.TransitScheduleValidator;
import org.matsim.utils.gis.shp2matsim.ShpGeometryUtils;
import playground.vsp.andreas.utils.pt.TransitScheduleCleaner;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class TransitRouteSplitter {
    private static boolean removeEmptyLines = true;
    private static boolean allowOneStopWithinZone = true ;
    private static int minimumRouteLength = 3 ;
    private static final Logger log = Logger.getLogger(TransitRouteTrimmer.class);

    public static void main(String[] args) throws IOException, SchemaException {
        final String inScheduleFile = "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.5-10pct/input/berlin-v5.5-transit-schedule.xml.gz";//"../../shared-svn/projects/avoev/matsim-input-files/vulkaneifel/v0/optimizedSchedule.xml.gz";
        final String inNetworkFile = "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.5-10pct/input/berlin-v5.5-network.xml.gz";//"../../shared-svn/projects/avoev/matsim-input-files/vulkaneifel/v0/optimizedNetwork.xml.gz";
        final String outScheduleFile = "C:\\Users\\jakob\\projects\\matsim-berlin\\src\\main\\java\\org\\matsim\\prepare\\ptRouteTrim\\output\\output-transit-schedule.xml.gz";//"../../shared-svn/projects/avoev/matsim-input-files/vulkaneifel/v1/optimizedScheduleWoBusTouchingZone.xml.gz";
        final String zoneShpFile = "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/projects/avoev/shp-files/shp-berlin-hundekopf-areas/berlin_hundekopf.shp";
        final String outputRouteShapeRoot = "C:\\Users\\jakob\\projects\\matsim-berlin\\src\\main\\java\\org\\matsim\\prepare\\ptRouteTrim\\output\\routes";

        // Prepare Scenario
        Config config = ConfigUtils.createConfig();
        config.transit().setTransitScheduleFile(inScheduleFile);
        config.network().setInputFile(inNetworkFile);

        Scenario scenario =  ScenarioUtils.loadScenario(config);
        TransitSchedule inTransitSchedule = scenario.getTransitSchedule();

        // Get Stops within Area
        List<PreparedGeometry> geometries = ShpGeometryUtils.loadPreparedGeometries(new URL(zoneShpFile));

        Set<Id<TransitStopFacility>> stopsInArea = new HashSet<>();
        for (TransitStopFacility stop : inTransitSchedule.getFacilities().values()) {
            if (ShpGeometryUtils.isCoordInPreparedGeometries(stop.getCoord(), geometries)) {
                stopsInArea.add(stop.getId());
            }
        }


        Set<Id<TransitLine>> linesToModify = inTransitSchedule.getTransitLines().keySet(); // all lines will be examined

        //Modify Routes: Split Routes into shorter sections (first stop not included)
        minimumRouteLength = 1 ;
        allowOneStopWithinZone = false ;
        TransitSchedule outTransitSchedule = modifyTransitLinesFromTransitSchedule(inTransitSchedule, linesToModify, stopsInArea, scenario);
        System.out.println("\n Modify Routes: SplitOldRouteIntoMultiplePiecesWithoutFirstStop");
        TransitRouteTrimmer.countLinesInOut(outTransitSchedule, stopsInArea);
        TransitSchedule2Shape.createShpFile(outTransitSchedule, outputRouteShapeRoot + "SplitOldRouteIntoMultiplePiecesWithoutFirstStop.shp");

        //Modify Routes: Split Routes into shorter sections (first stop included)
        minimumRouteLength=1;
        allowOneStopWithinZone = true ;
        TransitSchedule outTransitSchedule2 = modifyTransitLinesFromTransitSchedule(inTransitSchedule, linesToModify, stopsInArea, scenario);
        System.out.println("\n Modify Routes: SplitOldRouteIntoMultiplePiecesWithFirstStop");
        TransitRouteTrimmer.countLinesInOut(outTransitSchedule2, stopsInArea);
        TransitSchedule2Shape.createShpFile(outTransitSchedule2, outputRouteShapeRoot + "SplitOldRouteIntoMultiplePiecesWithFirstStop.shp");


        // Schedule Cleaner and Writer
        TransitSchedule outTransitScheduleCleaned = TransitScheduleCleaner.removeStopsNotUsed(outTransitSchedule);
        TransitScheduleValidator.ValidationResult validationResult = TransitScheduleValidator.validateAll(outTransitScheduleCleaned, scenario.getNetwork());
        log.warn(validationResult.getErrors());
        new TransitScheduleWriter(outTransitScheduleCleaned).writeFile(outScheduleFile);

    }


    private static TransitSchedule modifyTransitLinesFromTransitSchedule(TransitSchedule transitSchedule, Set<Id<TransitLine>> linesToModify, Set<Id<TransitStopFacility>> stopsInArea, Scenario scenario) {
        TransitSchedule tS = (new TransitScheduleFactoryImpl()).createTransitSchedule();
        Iterator var3 = transitSchedule.getFacilities().values().iterator();

        while (var3.hasNext()) {
            TransitStopFacility stop = (TransitStopFacility) var3.next();
            tS.addStopFacility(stop);
        }

        var3 = transitSchedule.getTransitLines().values().iterator();

        while (var3.hasNext()) {
            TransitLine line = (TransitLine) var3.next();
            if (!linesToModify.contains(line.getId())) {
                tS.addTransitLine(line);
                continue;
            }


            TransitLine lineNew = transitSchedule.getFactory().createTransitLine(line.getId());
            for (TransitRoute route : line.getRoutes().values()) {
                ArrayList<TransitRoute> routesNew = modifyRouteCutIntoShorterRoutes(route, stopsInArea, scenario);
                for (TransitRoute rt : routesNew) {
                    lineNew.addRoute(rt);
                }
            }

            if (lineNew.getRoutes().size() == 0 && removeEmptyLines) {
                log.info(lineNew.getId() + " does not contain routes. It will NOT be added to the schedule");
                continue;
            }

            tS.addTransitLine(lineNew);

        }

        log.info("Old schedule contained " + transitSchedule.getTransitLines().values().size() + " lines.");
        log.info("New schedule contains " + tS.getTransitLines().values().size() + " lines.");
        return tS;
    }

    private static ArrayList<TransitRoute> modifyRouteCutIntoShorterRoutes(TransitRoute routeOld, Set<Id<TransitStopFacility>> stopsInArea, Scenario scenario) {
        // Find which stops of route are within zone
        List<Boolean> inOutList = new ArrayList<>();
        ArrayList<TransitRoute> resultRoutes = new ArrayList<>();
        for (TransitRouteStop stop : routeOld.getStops()) {
            Id<TransitStopFacility> id = stop.getStopFacility().getId();
            inOutList.add(stopsInArea.contains(id));
        }

        List<TransitRouteStop> stopsOld = new ArrayList<>(routeOld.getStops());

        List<Id<Link>> linksOld = new ArrayList<>();
        linksOld.add(routeOld.getRoute().getStartLinkId());
        linksOld.addAll(routeOld.getRoute().getLinkIds());
        linksOld.add(routeOld.getRoute().getEndLinkId());


        // Make new stops and links lists
        List<TransitRouteStop> stopsNew = new ArrayList<>();
        List<Id<Link>> linksNew = new ArrayList<>();

        int newRouteCnt = 0;
        for (int i = 0; i < inOutList.size(); i++) {
            if (!inOutList.get(i)) {

                // adds first stop that's within zone
                if (stopsNew.size() == 0 && i > 0 && allowOneStopWithinZone) {
                    stopsNew.add(stopsOld.get(i-1));
                    linksNew.add(linksOld.get(i-1));
                }

                stopsNew.add(stopsOld.get(i));
                linksNew.add(linksOld.get(i));
            } else if (inOutList.get(i)) {

                // The following is only done, if stop i is the first stop to enter the zone.
                if (stopsNew.size() > 0) {
                    //adds first stop in zone
                    if (allowOneStopWithinZone) {
                        stopsNew.add(stopsOld.get(i));
                        linksNew.add(linksOld.get(i));
                    }

                    // creates route and clears stopsNew and linksNew
                    if (stopsNew.size() >= minimumRouteLength) {
                        TransitRoute routeNew = modifyRoute(routeOld, scenario, stopsNew, linksNew, newRouteCnt);
                        resultRoutes.add(routeNew);
                        newRouteCnt++;
                    }
                    stopsNew.clear();
                    linksNew.clear();
                }
            }
        }

        if (stopsNew.size() >= minimumRouteLength && stopsNew.size() > 0) {
            TransitRoute routeNew = modifyRoute(routeOld, scenario, stopsNew, linksNew, newRouteCnt);
            resultRoutes.add(routeNew);
        }

        return resultRoutes;
    }


    private static TransitRoute modifyRoute(TransitRoute routeOld, Scenario scenario, List<TransitRouteStop> stopsNew, List<Id<Link>> linksNew, int modNumber) {

        NetworkRoute networkRouteNew = RouteUtils.createNetworkRoute(linksNew, scenario.getNetwork());
        TransitScheduleFactory tsf = scenario.getTransitSchedule().getFactory();
        String routeIdOld = routeOld.getId().toString();
        Id<TransitRoute> routeIdNew = Id.create( routeIdOld + "_mod" + modNumber, TransitRoute.class);
        TransitRoute routeNew = tsf.createTransitRoute(routeIdNew, networkRouteNew, stopsNew, routeOld.getTransportMode());
        routeNew.setDescription(routeOld.getDescription());

        for (Departure departure : routeOld.getDepartures().values()) {
            String vehIdOld = departure.getVehicleId().toString();
            String depIdOld = departure.getId().toString();
            Departure departureNew = tsf.createDeparture(Id.create(depIdOld + "_mod" + modNumber, Departure.class), departure.getDepartureTime());
            departureNew.setVehicleId(Id.createVehicleId(vehIdOld + "_mod" + modNumber));
            routeNew.addDeparture(departureNew);
        }
        return routeNew;
    }
}
