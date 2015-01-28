package simulator.extensions.location;
import java.util.Set;

import org.cpntools.simulator.extensions.NamedRPCHandler;
import org.geotools.data.DataUtilities;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Graphable;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import simulator.extensions.location.model.Graph;
import simulator.extensions.location.model.LocationPool;
import simulator.extensions.location.ui.LogWindow;
import simulator.extensions.location.utils.GraphUtils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;


public class RPCHandler implements NamedRPCHandler {
	
	private final static String SEPARATOR = "#";
	
	private final LogWindow logWindow;
	private final LocationPool locationPool;
	
	public RPCHandler() {
		this(new LocationPool(), null);
	}

	public RPCHandler(LogWindow logWindow) {
		this(new LocationPool(), logWindow);
	}
	
	public RPCHandler(LocationPool locationPool) {
		this(locationPool, null);
	}
	
	public RPCHandler(LocationPool locationPool, LogWindow logWindow) {
		this.logWindow = logWindow;
		this.locationPool = locationPool;
	}

	public LocationPool getLocationPool() {
		return locationPool;
	}

	@Override
	public String structureName() {
		return "LA";
	}
	
	public String[] untangle(String s) {
		return s.split(SEPARATOR);
	}
	
	public String tangle(String[] s) {
		if (s.length == 0) return null;
		StringBuilder out = new StringBuilder();
		out.append(s[0]);
		for (int x = 1; x < s.length; ++x) {
			out.append(SEPARATOR).append(s[x]);
		}
		return out.toString();
	}

	public void log(String data) {
		if (logWindow != null)
			logWindow.showInfo(data);
	}
	
	public String sep() throws Exception {
		return SEPARATOR;
    }
	
	// Define the RPC endpoints here
	
	private String lastCustomer = "";
	public String af(String fname, String transition) throws Exception {
		log("assignFeature"+" "+fname+" "+transition);
		assignFeature(fname, transition, null);
		return fname;
	}
	
	public String assignFeature(String fname, String transition, String customer) throws Exception {
		log("assignFeature"+" "+fname+" "+transition+" "+customer);
		SimpleFeature feature = locationPool.getFeature(fname);
		if (transition.equals("RCC")) {
			lastCustomer = fname;
		}
		if (transition.equals("CRT")) {
			feature.setAttribute("status", "onroute");
			feature.setAttribute("destination", customer == null ? lastCustomer : customer);
		}
		if (transition.equals("RRT")) {
			feature.setAttribute("status", "available");
			feature.setAttribute("destination", "");
		}
		if (transition.equals("DIS")) {
			feature.setAttribute("status", "onroute");
			feature.setAttribute("destination", customer == null ? lastCustomer : customer);
		}
		if (transition.equals("RIN")) {
			feature.setAttribute("status", "onroute");
			feature.setAttribute("destination", feature.getAttribute("callcenter").toString());
		}
		return fname;
	}
	
	public String getFeatureSet(String featureType) throws Exception {
		log("getFeatureSet"+" "+featureType);
		Set<String> features = locationPool.getFeatures(featureType);
		String[] array = features.toArray(new String[]{});
		return tangle(array);
	}
	
	public Boolean typeEquals(String feature, String featureType) throws Exception {
		log("typeEquals"+" "+feature+" "+featureType);
		return locationPool.getFeatureType(feature).equals(featureType);
	}
	
	public Boolean attributeEquals(String feature, String propertyName, String propertyValue) throws Exception {
		String av = getAttribute(feature, propertyName);
		log("hasProperty"+" "+feature+" "+propertyName+" "+propertyValue+" "+av);
		return av.equals(propertyValue);
	}
	
	public String getAttribute(String feature, String propertyName) throws Exception {
		log("getAttribute"+" "+feature+" "+propertyName);
		SimpleFeature location = locationPool.getFeature(feature);
		Object att = location.getAttribute(propertyName);
		return att.toString();
	}
	
	public Boolean equals(String f1, String f2) throws Exception {
		log("equals"+" "+f1+" "+f2);
		SimpleFeature bigLocation = locationPool.getFeature(f1);
		SimpleFeature smallLocation = locationPool.getFeature(f2);
		Geometry bigGeometry = (Geometry) bigLocation.getDefaultGeometry();
		Geometry smallGeometry = (Geometry) smallLocation.getDefaultGeometry();
		return bigGeometry.equals(smallGeometry);
	}
	
	public Boolean disjoint(String f1, String f2) throws Exception {
		log("disjoint"+" "+f1+" "+f2);
		SimpleFeature bigLocation = locationPool.getFeature(f1);
		SimpleFeature smallLocation = locationPool.getFeature(f2);
		Geometry bigGeometry = (Geometry) bigLocation.getDefaultGeometry();
		Geometry smallGeometry = (Geometry) smallLocation.getDefaultGeometry();
		return bigGeometry.disjoint(smallGeometry);
	}

	public Boolean intersects(String f1, String f2) throws Exception {
		log("intersects"+" "+f1+" "+f2);
		SimpleFeature bigLocation = locationPool.getFeature(f1);
		SimpleFeature smallLocation = locationPool.getFeature(f2);
		Geometry bigGeometry = (Geometry) bigLocation.getDefaultGeometry();
		Geometry smallGeometry = (Geometry) smallLocation.getDefaultGeometry();
		return bigGeometry.intersects(smallGeometry);
	}
	
	public Boolean touches(String f1, String f2) throws Exception {
		log("touches"+" "+f1+" "+f2);
		SimpleFeature bigLocation = locationPool.getFeature(f1);
		SimpleFeature smallLocation = locationPool.getFeature(f2);
		Geometry bigGeometry = (Geometry) bigLocation.getDefaultGeometry();
		Geometry smallGeometry = (Geometry) smallLocation.getDefaultGeometry();
		return bigGeometry.touches(smallGeometry);
	}
	
	public Boolean crosses(String f1, String f2) throws Exception {
		log("crosses"+" "+f1+" "+f2);
		SimpleFeature bigLocation = locationPool.getFeature(f1);
		SimpleFeature smallLocation = locationPool.getFeature(f2);
		Geometry bigGeometry = (Geometry) bigLocation.getDefaultGeometry();
		Geometry smallGeometry = (Geometry) smallLocation.getDefaultGeometry();
		return bigGeometry.crosses(smallGeometry);
	}
	
	public Boolean within(String f1, String f2) throws Exception {
		log("within"+" "+f1+" "+f2);
		SimpleFeature bigLocation = locationPool.getFeature(f1);
		SimpleFeature smallLocation = locationPool.getFeature(f2);
		Geometry bigGeometry = (Geometry) bigLocation.getDefaultGeometry();
		Geometry smallGeometry = (Geometry) smallLocation.getDefaultGeometry();
		return bigGeometry.within(smallGeometry);
	}

	public Boolean contains(String f1, String f2) throws Exception {
		log("contains"+" "+f1+" "+f2);
		SimpleFeature bigLocation = locationPool.getFeature(f1);
		SimpleFeature smallLocation = locationPool.getFeature(f2);
		Geometry bigGeometry = (Geometry) bigLocation.getDefaultGeometry();
		Geometry smallGeometry = (Geometry) smallLocation.getDefaultGeometry();
		return bigGeometry.contains(smallGeometry);
	}
	
	public Boolean overlaps(String f1, String f2) throws Exception {
		log("overlaps"+" "+f1+" "+f2);
		SimpleFeature bigLocation = locationPool.getFeature(f1);
		SimpleFeature smallLocation = locationPool.getFeature(f2);
		Geometry bigGeometry = (Geometry) bigLocation.getDefaultGeometry();
		Geometry smallGeometry = (Geometry) smallLocation.getDefaultGeometry();
		return bigGeometry.overlaps(smallGeometry);
	}
	
	public String buffer(String feature, Integer distance) throws Exception {
		log("buffer"+" "+feature+" "+distance);
		SimpleFeature location = locationPool.getFeature(feature);
		Geometry geometry = (Geometry) location.getDefaultGeometry();
		Polygon b = (Polygon) geometry.buffer(distance);
		SimpleFeatureType bufferType = DataUtilities.createType("buffer", "geom:Polygon");
		SimpleFeature buffer = SimpleFeatureBuilder.build(bufferType, new Object[] { b }, null);
		locationPool.addFeature(feature+"_buffer", "__temp", buffer);
		return feature+"_buffer";
	}
	
	public Boolean shortestRoute(String startFeature, String endFeature) throws Exception {
		log("shortestRoute"+" "+startFeature+" "+endFeature);
		String endFeatureType = locationPool.getFeatureType(endFeature);
		Graph graph = GraphUtils.createGraphFromLocationPool(locationPool);
		Set<String> endFeatureNames = locationPool.getFeatures(endFeatureType);
		double bl = -1d;
		String bf = null;
		for (String candidateEndFeature : endFeatureNames) {
			Path path = GraphUtils.getShortestRoute(
					graph.get(startFeature), 
					graph.get(candidateEndFeature), 
					graph.getGenerator().getGraph());
			double l = 0d;
			for (Object ed : path.getEdges()) {
				SimpleFeature feature = (SimpleFeature) ((Graphable) ed).getObject();
				Geometry geometry = (Geometry) feature.getDefaultGeometry();
				l += geometry.getLength();
			}
			if (bf == null || l < bl) {
				bl = l;
				bf = candidateEndFeature;
			}
			
		}
		return endFeature.equals(bf);
	}
		
}
