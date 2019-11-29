package org.matsim.prepare.population.reLocation;

import org.junit.*;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.matsim.core.utils.geometry.geotools.MGC;
import org.matsim.core.utils.gis.PolygonFeatureFactory;
import org.matsim.core.utils.gis.ShapeFileWriter;
import org.matsim.testcases.MatsimTestUtils;
import org.opengis.feature.simple.SimpleFeature;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RunRelocationTest {

    @Rule
    public final MatsimTestUtils utils = new MatsimTestUtils() ;

    private static final String shapeFile = "testShapeFile.";

    @BeforeClass
    public static void SetupContext() {
        createShapeFile();
    }

    @AfterClass
    public static void cleanUp() {
        System.out.println("Done");
//		File file = new File(shapeFile + "shp");
//		file.delete();
//		file = new File(shapeFile + "shx");
//		file.delete();
//		file = new File(shapeFile + "cpg");
//		file.delete();
//		file = new File(shapeFile + "dbf");
//		file.delete();
//		file = new File(shapeFile + "prj");
//		file.delete();
//		file = new File(shapeFile + "fix");
//		file.delete();
    }

    @Test
    public void test() {
        Assert.assertTrue(true);
    }

    @Test
    public void readeShapeFileTest() {
//        Map<String, Geometry> zones = RunReLocation.readShapeFile(utils.getOutputDirectory() + shapeFile + "shp");
//        String[] nameOfZones = {"testShapeFile.1", "testShapeFile.2", "testShapeFile.3", "testShapeFile.4" };
//        Assert.assertEquals(4, zones.size());
//        for (String str : nameOfZones) {
//            Assert.assertTrue(zones.containsKey(str));
//        }
    }

    public void facilitiesToZoneTest() {

    }

    private static void createShapeFile() {

        Collection<SimpleFeature> features = new ArrayList<>();
        PolygonFeatureFactory polygonFeatureFactory = new PolygonFeatureFactory.Builder()
                .setCrs(MGC.getCRS("DHDN_GK4")).setName("Zone A")
                .addAttribute("Id", String.class)
                .create();

        Coordinate[] coordZone1 = new Coordinate[4];
        coordZone1[0] = new Coordinate(0, 0);
        coordZone1[1] = new Coordinate(0, 50);
        coordZone1[2] = new Coordinate(50, 50);
        coordZone1[3] = new Coordinate(50, 0);

        Coordinate[] coordZone2 = new Coordinate[4];
        coordZone2[0] = new Coordinate(0, 0);
        coordZone2[1] = new Coordinate(0, 50);
        coordZone2[2] = new Coordinate(-50, 50);
        coordZone2[3] = new Coordinate(-50, 0);

        Coordinate[] coordZone3 = new Coordinate[4];
        coordZone3[0] = new Coordinate(0, 0);
        coordZone3[1] = new Coordinate(0, -50);
        coordZone3[2] = new Coordinate(50, -50);
        coordZone3[3] = new Coordinate(50, 0);

        Coordinate[] coordZone4 = new Coordinate[4];
        coordZone4[0] = new Coordinate(0, 0);
        coordZone4[1] = new Coordinate(0, -50);
        coordZone4[2] = new Coordinate(-50, -50);
        coordZone4[3] = new Coordinate(-50, 0);

        features.add(polygonFeatureFactory.createPolygon(coordZone1, new HashMap<String, Object>() {{put("Id", "zone1");}}, null));
        features.add(polygonFeatureFactory.createPolygon(coordZone2, new HashMap<String, Object>() {{put("Id", "zone2");}}, null));
        features.add(polygonFeatureFactory.createPolygon(coordZone3, new HashMap<String, Object>() {{put("Id", "zone3");}}, null));
        features.add(polygonFeatureFactory.createPolygon(coordZone4, new HashMap<String, Object>() {{put("Id", "zone4");}}, null));

//        ShapeFileWriter.writeGeometries(features, utils.getOutputDirectory() + shapeFile + "shp");

    }

    private void WriteShapeFile() {

    }


}
