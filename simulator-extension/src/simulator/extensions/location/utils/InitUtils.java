package simulator.extensions.location.utils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import simulator.extensions.location.model.LocationPool;
import simulator.extensions.location.ui.LogWindow;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public class InitUtils {
	
	public enum FeatureTypes {Road, Region, CallCenter, Customer, RepairStation, RepairTeam, Inspector};
	
	private static File lineFile = new File("data/callcenter1/line/line_Shapefile.shp");
	private static File polyFile = new File("data/callcenter1/polygon/polygon_Shapefile.shp");
	private static File pointFile = new File("data/callcenter1/point/point2_Shapefile.shp");
	
	public static void initialize(LocationPool pool, LogWindow logWindow) throws IOException {
		logWindow.showInfo(lineFile.toURI().toURL().toString());
		logWindow.showInfo(polyFile.toURI().toURL().toString());
		logWindow.showInfo(pointFile.toURI().toURL().toString());
		if (!lineFile.exists() || !polyFile.exists() || !pointFile.exists())
			logWindow.showInfo("\n!!! SOME FILES DO NOT EXIST !!!\n");
		initializeRoads(pool, logWindow);
		initializeRegions(pool, logWindow);
		initializeRepairStations(pool, logWindow);
		initializeCustomers(pool, logWindow);
		initializeCallCenters(pool, logWindow);
		initializeRepairTeams(pool, logWindow);
		initializeInspectors(pool, logWindow);
	}

	private static void initializeRoads(LocationPool pool, LogWindow logWindow) throws IOException {
		logWindow.showInfo(" (init roads)");
		SimpleFeatureSource featureSource = LoadingUtils.getFeatureSourceFromShapefile(lineFile);
		SimpleFeatureCollection features = featureSource.getFeatures();
		FeatureIterator<SimpleFeature> iterator = features.features();
		int i = 0;
		while (iterator.hasNext()) {
			SimpleFeature feature = iterator.next();
			pool.addFeature("Road" + ++i, FeatureTypes.Road.name(), feature);
		}
		logWindow.showInfo(pool.getFeatures(FeatureTypes.Road.name()).size() + " roads parsed");
	}
	
	private static void initializeRegions(LocationPool pool, LogWindow logWindow) throws IOException {
		logWindow.showInfo(" (init regions)");
		SimpleFeatureSource featureSource = LoadingUtils.getFeatureSourceFromShapefile(polyFile);
		SimpleFeatureCollection features = featureSource.getFeatures();
		FeatureIterator<SimpleFeature> iterator = features.features();
		int i = 0;
		while (iterator.hasNext()) {
			SimpleFeature feature = iterator.next();
			String name = "Region" + ++i;
			pool.addFeature(name, FeatureTypes.Region.name(), feature);
		}
		logWindow.showInfo(pool.getFeatures(FeatureTypes.Region.name()).size() + " regions parsed");
	}
	
	private static void initializeCustomers(LocationPool pool, LogWindow logWindow) throws IOException {
		logWindow.showInfo(" (init customers)");
		Map<String, Class<?>> atts = new HashMap<String, Class<?>>(){
			private static final long serialVersionUID = 1L;
			{
				this.put("region", String.class);
			}};
		SimpleFeatureSource featureSource = LoadingUtils.getFeatureSourceFromShapefile(pointFile);
		SimpleFeatureCollection features = featureSource.getFeatures();
		FeatureIterator<SimpleFeature> iterator = features.features();
		SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(
				LoadingUtils.getExtendedFeatureTypeFromFeatureSource("Customer", atts, featureSource));
		int i = 0;
		while (iterator.hasNext()) {
			SimpleFeature feature = (SimpleFeature) iterator.next();
			if (!feature.getAttribute("Name").toString().startsWith("客户")) continue;
			String name = "Customer" + ++i;
			sfb.addAll(feature.getAttributes());
			sfb.add(getRegion(feature, pool));
			SimpleFeature extendedFeature = sfb.buildFeature(name);
			extendedFeature.setAttribute("Name", name);
			pool.addFeature(
					name, 
					FeatureTypes.Customer.name(), 
					extendedFeature);
		}
		logWindow.showInfo(pool.getFeatures(FeatureTypes.Customer.name()).size() + " customers parsed");
	}
	
	private static void initializeCallCenters(LocationPool pool, LogWindow logWindow) throws IOException {
		int csx[] = new int[]{-98, -129, 0, 0, 88, 74};
		int csy[] = new int[]{125, 25, 100, -40, 132, 53};
		
		logWindow.showInfo(" (init centers)");
		SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
		sftb.setName("Call Center");
		sftb.setCRS(DefaultGeographicCRS.WGS84);
		sftb.add("Name", String.class);
		sftb.add("the_geom", Point.class);
		sftb.add("region", String.class);
		SimpleFeatureType callCenterType = sftb.buildFeatureType();
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
		for (int x = 0; x < csx.length; x++) {
			String name = "CallCenter" + (x + 1);
			Point point = geometryFactory.createPoint(new Coordinate(csx[x], csy[x]));
			SimpleFeatureBuilder builder = new SimpleFeatureBuilder(callCenterType);
			builder.add(name);
			builder.add(point);
			builder.add("");
			SimpleFeature feature = builder.buildFeature(name);
			feature.setAttribute("region", getRegion(feature, pool));
			pool.addFeature(
					name, 
					FeatureTypes.CallCenter.name(), 
					feature);
		}
		logWindow.showInfo(pool.getFeatures(FeatureTypes.CallCenter.name()).size() + " call centers parsed");
	}
	
	private static void initializeRepairTeams(LocationPool pool, LogWindow logWindow) throws IOException {
		int numberOfTeams = 5;
		
		logWindow.showInfo(" (init teams)");
		SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
		sftb.setName("Repair Team");
		sftb.setCRS(DefaultGeographicCRS.WGS84);
		sftb.add("Name", String.class);
		sftb.add("the_geom", Point.class);
		sftb.add("status", String.class);
		sftb.add("destination", String.class);
		SimpleFeatureType repairTeamType = sftb.buildFeatureType();
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
		List<String> repairStations = new ArrayList<String>(pool.getFeatures(FeatureTypes.RepairStation.name()));
		Random random = new Random();
		for (int x = 0; x < numberOfTeams; x++) {
			String name = "RepairTeam" + (x + 1);
			Point station = (Point) pool.getFeature(
					repairStations.get(random.nextInt(repairStations.size()))).getDefaultGeometry();
			Point point = geometryFactory.createPoint(new Coordinate(station.getX(), station.getY()));
			SimpleFeatureBuilder builder = new SimpleFeatureBuilder(repairTeamType);
			builder.add(name);
			builder.add(point);
			builder.add("available");
			builder.add("");
			SimpleFeature feature = builder.buildFeature(name);
			pool.addFeature(
					name, 
					FeatureTypes.RepairTeam.name(), 
					feature);
		}
		logWindow.showInfo(pool.getFeatures(FeatureTypes.RepairTeam.name()).size() + " repair teams parsed");
	}
	
	private static void initializeRepairStations(LocationPool pool, LogWindow logWindow) throws IOException {
		logWindow.showInfo(" (init stations)");
		Map<String, Class<?>> atts = new HashMap<String, Class<?>>(){
			private static final long serialVersionUID = 1L;
			{
				this.put("region", String.class);
			}};
		SimpleFeatureSource featureSource = LoadingUtils.getFeatureSourceFromShapefile(pointFile);
		SimpleFeatureCollection features = featureSource.getFeatures();
		FeatureIterator<SimpleFeature> iterator = features.features();
		SimpleFeatureBuilder sfb = new SimpleFeatureBuilder(
				LoadingUtils.getExtendedFeatureTypeFromFeatureSource("RepairStation", atts, featureSource));
		int i = 0;
		while (iterator.hasNext()) {
			SimpleFeature feature = iterator.next();
			if (!feature.getAttribute("Name").toString().startsWith("维修站")) continue;
			String name = "RepairStation" + ++i;
			sfb.addAll(feature.getAttributes());
			sfb.add(getRegion(feature, pool));
			SimpleFeature extendedFeature = sfb.buildFeature(name);
			extendedFeature.setAttribute("Name", name);
			pool.addFeature(
					name, 
					FeatureTypes.RepairStation.name(), 
					extendedFeature);
		}
		logWindow.showInfo(pool.getFeatures(FeatureTypes.RepairStation.name()).size() + " repair stations parsed");
	}

	private static void initializeInspectors(LocationPool pool, LogWindow logWindow) {
		int numberOfInspectorsPerCenter = 1;
		
		logWindow.showInfo(" (init inspectors)");
		SimpleFeatureTypeBuilder sftb = new SimpleFeatureTypeBuilder();
		sftb.setName("Call Center");
		sftb.setCRS(DefaultGeographicCRS.WGS84);
		sftb.add("Name", String.class);
		sftb.add("the_geom", Point.class);
		sftb.add("callcenter", String.class);
		sftb.add("status", String.class);
		sftb.add("destination", String.class);
		SimpleFeatureType inspectorType = sftb.buildFeatureType();
		Set<String> callCenters = pool.getFeatures(FeatureTypes.CallCenter.name());
		
		int i = 0;
		for (String center : callCenters) {
			Point pointGeom = (Point) pool.getFeature(center).getDefaultGeometry();
			for (int j = 0; j < numberOfInspectorsPerCenter; j++) {
				String name = "Inspector" + ++i;
				SimpleFeatureBuilder builder = new SimpleFeatureBuilder(inspectorType);
				builder.add(name);
				builder.add(pointGeom);
				builder.add(center);
				builder.add("available");
				builder.add("");
				SimpleFeature feature = builder.buildFeature(name);
				pool.addFeature(
						name,
						FeatureTypes.Inspector.name(), 
						feature);
			}
		}
		logWindow.showInfo(pool.getFeatures(FeatureTypes.Inspector.name()).size() + " inspectors parsed");
	}

	private static String getRegion(SimpleFeature feature, LocationPool pool) {
		Set<String> regions = pool.getFeatures(FeatureTypes.Region.name());
		Point pointGeom = (Point) feature.getDefaultGeometry();
		
		for (String region : regions) {
			SimpleFeature regionFeature = pool.getFeature(region);
			MultiPolygon regionGeom = (MultiPolygon) regionFeature.getDefaultGeometry();
			if (regionGeom.contains(pointGeom)) return region;
		}
		
		return null;
    }
}
