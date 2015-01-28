package geotools.testing;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.build.line.LineStringGraphGenerator;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.traverse.standard.DijkstraIterator;
import org.geotools.graph.traverse.standard.DijkstraIterator.EdgeWeighter;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

public class ConstructGraphFromShp {

	private static String shapeFileGraph 			= "data/callcenter1/line/line_Shapefile.shp";
	private static String shapeFilePointsToAdd 	= "data/callcenter1/point/point2_Shapefile.shp";
	
	private static Map<String, Node> nameToNode = new HashMap<String, Node>();

	public static void main(String[] args) throws Exception {
		LoadAndShowShp mapper = new LoadAndShowShp();
		MapContent map = mapper.getMap();
		
		ConstructGraphFromShp me = new ConstructGraphFromShp();
		Style bluePointStyle = GeoUtils.createPointStyle(Color.blue, Color.blue, 1);
		Style redPointStyle = GeoUtils.createPointStyle(Color.red, Color.red, 2);
		
		// Construct the graph
		FeatureGraphGenerator graphGenerator = me.buildGraph();
		Graph graph = graphGenerator.getGraph();
		SimpleFeatureType graphPointType = DataUtilities.createType("gnode","geom:Point");
		
		// Display the graph (add a new layer)
		DefaultFeatureCollection collection = new DefaultFeatureCollection();
		for (Object n : graph.getNodes()) {
            Node node = (Node) n;
            collection.add(SimpleFeatureBuilder.build(
            		graphPointType, 
            		new Object[]{node.getObject()}, 
            		null));
        }
		
		FeatureLayer layer = new FeatureLayer(collection, bluePointStyle);
		map.addLayer(layer);
		
		Random rand = new Random();
		String[] nodes = nameToNode.keySet().toArray(new String[]{});
		String startName = nodes[rand.nextInt(nodes.length)];
		String destName = nodes[rand.nextInt(nodes.length)];
		Node start = nameToNode.get(startName);
		Node dest = nameToNode.get(destName);
		
		// Select by name
		start = nameToNode.get("客户4");
		dest = nameToNode.get("维修站3");
		
		System.out.println("START: "+startName+" "+start);
		System.out.println("DEST: "+destName+" "+dest);
		
		EdgeWeighter weighter = new DijkstraIterator.EdgeWeighter() {
		   public double getWeight(Edge e) {
		      SimpleFeature feature = (SimpleFeature) e.getObject();
		      Geometry geometry = (Geometry) feature.getDefaultGeometry();
		      return geometry.getLength();
		   }
		};

		DijkstraShortestPathFinder pf = new DijkstraShortestPathFinder(graph, start, weighter);
		pf.calculate();

		Path path = pf.getPath(dest);
		
		System.out.println("PATH: "+path);
		
		DefaultFeatureCollection collectionPath = new DefaultFeatureCollection();
		for (Object n : path) {
            Node node = (Node) n;
            collectionPath.add(SimpleFeatureBuilder.build(graphPointType, 
            		new Object[]{node.getObject()}, null));
        }
		
		FeatureLayer layerPath = new FeatureLayer(collectionPath, redPointStyle);
		map.addLayer(layerPath);
		
		JMapFrame.showMap(map);
	}

	private FeatureGraphGenerator buildGraph() throws Exception {
		nameToNode.clear();
		
		LineStringGraphGenerator lineStringGenerator = new LineStringGraphGenerator();
		FeatureGraphGenerator graphGenerator = new FeatureGraphGenerator(lineStringGenerator);
		GeometryFactory gf = new GeometryFactory();
		SimpleFeatureType lineType = DataUtilities.createType(
				"lseg","geom:LineString, name:String");
		
		// Add lines (roads)
		File graphFile = new File(shapeFileGraph);
		SimpleFeatureSource graphFeatureSource = GeoUtils.getFeatureSourceFromShapefile(graphFile);
		SimpleFeatureCollection features = graphFeatureSource.getFeatures();
		FeatureIterator<?> iterator = features.features();
		while (iterator.hasNext()) {
			SimpleFeatureImpl feature = (SimpleFeatureImpl) iterator.next();
			Geometry geometry = (Geometry) feature.getAttribute("the_geom");
			LineString ls = (LineString) ((MultiLineString)geometry).getGeometryN(0);
			for (int i = 0; i < ls.getNumPoints()-1; i++) {
				LineSegment lseg = new LineSegment(ls.getCoordinateN(i), ls.getCoordinateN(i+1));
				String name = feature.getID() + "_" + i;
				SimpleFeature lineTypeFeature = SimpleFeatureBuilder.build(
						lineType, 
		            	new Object[]{lseg.toGeometry(gf), name}, null);
				graphGenerator.add(lineTypeFeature);		
			}
		}
		
		// Add points (centers)
		File pointFile = new File(shapeFilePointsToAdd);
		SimpleFeatureSource pointFeatureSource = GeoUtils.getFeatureSourceFromShapefile(pointFile);
		@SuppressWarnings("unchecked")
		Collection<Edge> edges = graphGenerator.getGraph().getEdges();
		features = pointFeatureSource.getFeatures();
		iterator = features.features();
		while (iterator.hasNext()) {
			SimpleFeatureImpl feature = (SimpleFeatureImpl) iterator.next();
			Geometry geometry = (Geometry) feature.getAttribute("the_geom");
			System.out.println("*** FEATURE : " + feature);
			System.out.println("    GEOMETRY: " + geometry);
			System.out.println("    BASENAME: " + feature.getID());
			System.out.println("    NAME    : " + feature.getAttribute("Name"));
			
			Point p = (Point) geometry;
			Edge nearestEdge = GeoUtils.getNearestGraphEdge(edges, p);
			Point2D pointOnEdge = GeoUtils.getColinearPoint(nearestEdge, p);
			Coordinate pointOnEdgeCoords = new Coordinate(pointOnEdge.getX(), pointOnEdge.getY());
			
			// Remove this edge
			graphGenerator.remove(nearestEdge.getObject());
			// Add three new edges
			LineSegment lseg = new LineSegment(pointOnEdgeCoords, p.getCoordinate());
			LineSegment lsegA = new LineSegment(((Point)nearestEdge.getNodeA().getObject()).getCoordinate(), pointOnEdgeCoords);
			LineSegment lsegB = new LineSegment(pointOnEdgeCoords, ((Point)nearestEdge.getNodeB().getObject()).getCoordinate());
			String name = feature.getID();
			SimpleFeature lineTypeFeature = SimpleFeatureBuilder.build(lineType, 
	            	new Object[]{lseg.toGeometry(gf), name + "_connecting"}, null);
			SimpleFeature lineTypeFeatureA = SimpleFeatureBuilder.build(lineType, 
	            	new Object[]{lsegA.toGeometry(gf), name + "_a"}, null);
			SimpleFeature lineTypeFeatureB = SimpleFeatureBuilder.build(lineType, 
	            	new Object[]{lsegB.toGeometry(gf), name + "_b"}, null);
			graphGenerator.add(lineTypeFeature);
			graphGenerator.add(lineTypeFeatureA);
			graphGenerator.add(lineTypeFeatureB);
			
			Node createdNode = ((Edge) graphGenerator.get(lineTypeFeature)).getNodeB();
			nameToNode.put(feature.getAttribute("Name").toString(), createdNode);
			System.out.println(feature.getAttribute("Name").toString() + " --> " + createdNode);
			
		}
		return graphGenerator;
	}

}
