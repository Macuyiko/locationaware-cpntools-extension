package geotools.testing;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.geotools.resources.geometry.ShapeUtilities;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Font;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.TextSymbolizer;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


public class GeoUtils {
	private static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
	private static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);
	
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
	
	public static SimpleFeatureSource getFeatureSourceFromShapefile(File file) {
		try {
			Map<String, Serializable> params = new HashMap<String, Serializable>();
			params.put(ShapefileDataStoreFactory.DBFCHARSET.key, "GB18030");
			params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
			params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, true);
			params.put(ShapefileDataStoreFactory.ENABLE_SPATIAL_INDEX.key, true);
			params.put(ShapefileDataStoreFactory.MEMORY_MAPPED.key, false);
			DataStore store = DataStoreFinder.getDataStore(params);
	        SimpleFeatureSource featureSource = store.getFeatureSource(store.getTypeNames()[0]);
	        return featureSource;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static Style createStyle(FeatureSource<?, ?> featureSource) {
		SimpleFeatureType schema = (SimpleFeatureType) featureSource.getSchema();
		Class<?> geomType = schema.getGeometryDescriptor().getType().getBinding();

		if (Polygon.class.isAssignableFrom(geomType) || MultiPolygon.class.isAssignableFrom(geomType)) {
			return createPolygonStyle(Color.RED, 3, 1, new float[]{10, 10}, Color.RED, 0.1);
		} else if (LineString.class.isAssignableFrom(geomType) || MultiLineString.class.isAssignableFrom(geomType)) {
			return createLineStyle(Color.BLACK, 3);
		} else {
			return createPointStyle(Color.GREEN, Color.GREEN, 10);
		}
	}

	public static Style createPolygonStyle(Color strokeColor, int size1, double salpha, float[] dashArray,
			Color fillColor, double falpha) {
		Stroke stroke = styleFactory.createStroke(
				filterFactory.literal(strokeColor), 
				filterFactory.literal(size1),
				filterFactory.literal(salpha));
		stroke.setDashArray(dashArray);
		Fill fill = styleFactory.createFill(
				filterFactory.literal(fillColor),
				filterFactory.literal(falpha));
		PolygonSymbolizer sym = styleFactory.createPolygonSymbolizer(stroke,
				fill, null);
		Rule rule = styleFactory.createRule();
		rule.symbolizers().add(sym);
		FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[] { rule });
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);
		return style;
	}

	public static Style createLineStyle(Color color, int size) {
		Stroke stroke = styleFactory.createStroke(filterFactory.literal(color), filterFactory.literal(size));
		LineSymbolizer sym = styleFactory.createLineSymbolizer(stroke, null);
		Rule rule = styleFactory.createRule();
		rule.symbolizers().add(sym);
		FeatureTypeStyle fts = styleFactory .createFeatureTypeStyle(new Rule[] { rule });
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);
		return style;
	}

	public static Style createPointStyle(Color stroke, Color fill, int size) {
		Graphic gr = styleFactory.createDefaultGraphic();
		Mark mark = styleFactory.getCircleMark();
		mark.setStroke(styleFactory.createStroke(filterFactory.literal(stroke), filterFactory.literal(size)));
		mark.setFill(styleFactory.createFill(filterFactory.literal(fill)));
		gr.graphicalSymbols().clear();
		gr.graphicalSymbols().add(mark);
		gr.setSize(filterFactory.literal(10));
       
		Rule rule = styleFactory.createRule();
		
		PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);
		rule.symbolizers().add(sym);
		
		Font[] f = new Font[] { styleFactory.createFont(
				filterFactory.literal("DejaVu Sans"),
				filterFactory.literal(Font.Style.NORMAL),
				filterFactory.literal(Font.Weight.NORMAL),
				filterFactory.literal(24)) };
		StyleBuilder sb = new StyleBuilder();
		TextSymbolizer tsym = styleFactory.createTextSymbolizer(
				styleFactory.createFill(filterFactory.literal(Color.BLACK)),
				f,
				null,
				sb.attributeExpression("Name"),
				null,
				null);
		rule.symbolizers().add(tsym);
		
		FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[] { rule });
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);
		return style;
	}
}
