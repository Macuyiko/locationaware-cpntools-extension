package simulator.extensions.location.utils;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Set;

import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.geotools.graph.traverse.standard.DijkstraIterator;
import org.geotools.graph.traverse.standard.DijkstraIterator.EdgeWeighter;
import org.geotools.resources.geometry.ShapeUtilities;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.Point;

import simulator.extensions.location.model.Graph;
import simulator.extensions.location.model.LocationPool;
import simulator.extensions.location.utils.InitUtils.FeatureTypes;

public class GraphUtils {
	public static Graph createGraphFromLocationPool(LocationPool pool) {
		Graph graph = new Graph();
		addLines(pool.getFeatures(FeatureTypes.Road.name()), graph, pool);
		@SuppressWarnings("unchecked")
		Collection<Edge> edges = graph.getGenerator().getGraph().getEdges();
		addPoints(pool.getFeatures(FeatureTypes.CallCenter.name()), edges, graph, pool);
		addPoints(pool.getFeatures(FeatureTypes.RepairStation.name()), edges, graph, pool);
		addPoints(pool.getFeatures(FeatureTypes.Customer.name()), edges, graph, pool);
		
		@SuppressWarnings("unchecked")
		Collection<Node> nodes = graph.getGenerator().getGraph().getNodes();
		for (String fname : pool.getFeatures(FeatureTypes.RepairTeam.name())) {
			Node nearest = getNearestGraphNode(nodes, (Point) pool.getFeature(fname).getDefaultGeometry());
			graph.put(fname, nearest);
		}
		for (String fname : pool.getFeatures(FeatureTypes.Inspector.name())) {
			Node nearest = getNearestGraphNode(nodes, (Point) pool.getFeature(fname).getDefaultGeometry());
			graph.put(fname, nearest);
		}
		
		return graph;
	}
	
	private static void addLines(Set<String> multilines, Graph graph, LocationPool pool) {
		FeatureGraphGenerator graphGenerator = graph.getGenerator();
		GeometryFactory gf = new GeometryFactory();
		SimpleFeatureType lineType;
		try {
			lineType = DataUtilities.createType("lseg", "geom:LineString");
		} catch (SchemaException e) {
			return;
		}
		
		for (String fname : multilines) {
			SimpleFeature feature = pool.getFeature(fname);
			MultiLineString mls = (MultiLineString) feature.getDefaultGeometry();
			LineString ls = (LineString) mls.getGeometryN(0);
			for (int i = 0; i < ls.getNumPoints() - 1; i++) {
				LineSegment lseg = new LineSegment(ls.getCoordinateN(i), ls.getCoordinateN(i + 1));
				SimpleFeature lineTypeFeature = SimpleFeatureBuilder.build(lineType, new Object[] { lseg.toGeometry(gf) }, null);
				graphGenerator.add(lineTypeFeature);
			}
		}
	}
	
	private static void addPoints(Set<String> points, Collection<Edge> edges, Graph graph, LocationPool pool) {
		FeatureGraphGenerator graphGenerator = graph.getGenerator();
		GeometryFactory gf = new GeometryFactory();
		SimpleFeatureType lineType;
		try {
			lineType = DataUtilities.createType("lseg", "geom:LineString");
		} catch (SchemaException e) {
			return;
		}
		
		for (String fname : points) {
			SimpleFeature feature = pool.getFeature(fname);
			Point point = (Point) feature.getDefaultGeometry();
			Edge nearestEdge = getNearestGraphEdge(edges, point);
			Point2D pointOnEdge = getColinearPoint(nearestEdge, point);
			Coordinate pointOnEdgeCoords = new Coordinate(pointOnEdge.getX(), pointOnEdge.getY());

			graphGenerator.remove(nearestEdge.getObject());
			LineSegment lsegX = new LineSegment(pointOnEdgeCoords, point.getCoordinate());
			LineSegment lsegA = new LineSegment(((Point) nearestEdge.getNodeA().getObject()).getCoordinate(), pointOnEdgeCoords);
			LineSegment lsegB = new LineSegment(pointOnEdgeCoords, ((Point) nearestEdge.getNodeB().getObject()).getCoordinate());
			
			SimpleFeature lineTypeFeatureX = SimpleFeatureBuilder.build(lineType, new Object[] { lsegX.toGeometry(gf) }, null);
			SimpleFeature lineTypeFeatureA = SimpleFeatureBuilder.build(lineType, new Object[] { lsegA.toGeometry(gf) }, null);
			SimpleFeature lineTypeFeatureB = SimpleFeatureBuilder.build(lineType, new Object[] { lsegB.toGeometry(gf) }, null);
			graphGenerator.add(lineTypeFeatureX);
			graphGenerator.add(lineTypeFeatureA);
			graphGenerator.add(lineTypeFeatureB);

			Node createdNode = ((Edge) graphGenerator.get(lineTypeFeatureX)).getNodeB();
			graph.put(fname, createdNode);
		}
	}
	
	public static Path getShortestRoute(Node start, Node dest, org.geotools.graph.structure.Graph graph) {
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
		return path;
	}
	
	public static Point2D getColinearPoint(Edge e, Point p) {
		SimpleFeature f = (SimpleFeature) (((Graphable) e).getObject());
		LineString ls = (LineString) f.getDefaultGeometry();
		Point2D p2d = ShapeUtilities.nearestColinearPoint(
				ls.getCoordinateN(0).x, ls.getCoordinateN(0).y,
				ls.getCoordinateN(1).x, ls.getCoordinateN(1).y,
				p.getCoordinate().x, p.getCoordinate().y);
		return p2d;
	}
	
	public static Edge getNearestGraphEdge(Collection<Edge> edges, Point pointy) {
		Edge nearestEdge = null;
		double dist = -1;
		for (Edge e : edges) {
			Point2D p = getColinearPoint(e, pointy);
			double newdist = calculateEuclideanDistance(
					pointy.getX(), pointy.getY(), 
					p.getX(), p.getY());
			if (dist < 0 || newdist < dist) {
				dist = newdist;
				nearestEdge = e;
			}
		}
		return nearestEdge;
	}
	
	public static Node getNearestGraphNode(Collection<Node> nodes, Point pointy) {
		Node nearestNode = null;
		double dist = -1;
		for (Node n : nodes) {
			Point p = ((Point) ((Graphable) n).getObject());
			double newdist = calculateEuclideanDistance(
					pointy.getX(), pointy.getY(), 
					p.getCoordinate().x, p.getCoordinate().y);
			if (dist < 0 || newdist < dist) {
				dist = newdist;
				nearestNode = n;
			}
		}
		return nearestNode;
	}

	public static double calculateEuclideanDistance(double xOrig, double yOrig, double xDest, double yDest) {
		double distance = Math.sqrt((xDest - xOrig) * (xDest - xOrig) + (yDest - yOrig) * (yDest - yOrig));
		return distance;
	}

}
