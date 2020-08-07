package org.matsim.prepare.population;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.population.Activity;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.util.*;

/**
 * @author dziemke
 */
public class PopulationModifier {

    public static void main(String[] args) {
        // Input and output files
        String plansInputFile = "../../public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.5-1pct/input/berlin-v5.5-1pct.plans.xml.gz";
        String plansOutputFile = "scenarios/berlin-v5.5-1pct/input/plans-modified-carInternal.xml.gz";
        String areaShapeFile = "../../shared-svn/studies/countries/de/open_berlin_scenario/input/shapefiles/berlin_hundekopf/berlin_hundekopf.shp";

        // Store relevant area of city as geometry
        Collection<SimpleFeature> features = (new ShapeFileReader()).readFileAndInitialize(areaShapeFile);
        Map<String, Geometry> zoneGeometries = new HashMap<>();
        for (SimpleFeature feature : features) {
            zoneGeometries.put((String) feature.getAttribute("SCHLUESSEL"), (Geometry) feature.getDefaultGeometry());
        }
        Geometry areaGeometry = zoneGeometries.get("Hundekopf");

        // Get population
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        PopulationReader populationReader = new PopulationReader(scenario);
        populationReader.readFile(plansInputFile);

        // Substitute car mode by carInternal mode for people inside relevant area
        for (Person person : scenario.getPopulation().getPersons().values()) {
            Activity homeActivity = (Activity) person.getPlans().get(0).getPlanElements().get(0);
            Point homeActAsPoint = MGC.xy2Point(homeActivity.getCoord().getX(), homeActivity.getCoord().getY());
            if (areaGeometry.contains(homeActAsPoint)) {
                person.getAttributes().putAttribute("subpopulation", "personInternal");
                for (PlanElement pe : person.getPlans().get(0).getPlanElements()) {
                    if (pe instanceof Leg) {
                        Leg leg = (Leg) pe;
                        if (leg.getMode().equals(TransportMode.car)) {
                            leg.setMode("carInternal");
                            leg.getAttributes().putAttribute("routingMode", "carInternal");
                        }
                        if (leg.getMode().equals(TransportMode.walk) && leg.getAttributes().getAttribute("routingMode").equals(TransportMode.car)) {
                            leg.getAttributes().putAttribute("routingMode", "carInternal");
                        }
                    } else if (pe instanceof Activity) {
                        Activity activity = (Activity) pe;
                        if (activity.getType().equals("car interaction")) {
                            activity.setType("carInternal interaction");
                        }
                    }
                }
            }
        }

        // Write modified population to file
        PopulationWriter populationWriter = new PopulationWriter(scenario.getPopulation());
        populationWriter.write(plansOutputFile);
    }
}