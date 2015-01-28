package simulator.extensions.location.utils;

import java.awt.Color;

import org.geotools.data.FeatureSource;
import org.geotools.factory.CommonFactoryFinder;
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
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

public class StyleUtils {
	private static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
	private static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);
	
	public static Style createStyle(FeatureSource<?, ?> featureSource) {
		SimpleFeatureType schema = (SimpleFeatureType) featureSource.getSchema();
		Class<?> geomType = schema.getGeometryDescriptor().getType().getBinding();

		if (Polygon.class.isAssignableFrom(geomType) || MultiPolygon.class.isAssignableFrom(geomType)) {
			return createPolygonStyle(Color.RED, 3, 1, new float[]{10, 10}, null, 0.1);
		} else if (LineString.class.isAssignableFrom(geomType) || MultiLineString.class.isAssignableFrom(geomType)) {
			return createLineStyle(Color.BLACK, 3);
		} else {
			return createPointStyle(Color.GREEN, Color.GREEN, 10, null);
		}
	}

	public static Style createPolygonStyle(Color strokeColor, int size1, double salpha, float[] dashArray,
			Color fillColor, double falpha) {
		Stroke stroke = styleFactory.createStroke(
				filterFactory.literal(strokeColor), 
				filterFactory.literal(size1),
				filterFactory.literal(salpha));
		stroke.setDashArray(dashArray);
		PolygonSymbolizer sym;
		if (fillColor != null) {
			Fill fill = styleFactory.createFill(
					filterFactory.literal(fillColor),
					filterFactory.literal(falpha));
			sym = styleFactory.createPolygonSymbolizer(stroke, fill, null);
		} else { 
			sym = styleFactory.createPolygonSymbolizer(stroke, null, null);
		}
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

	public static Style createPointStyle(Color stroke, Color fill, int size, String attributeLabel) {
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
		
		if (attributeLabel != null) {
			Font[] f = new Font[] { styleFactory.createFont(
					filterFactory.literal("Arial"),
					filterFactory.literal(Font.Style.NORMAL),
					filterFactory.literal(Font.Weight.NORMAL),
					filterFactory.literal(24)) };
			StyleBuilder sb = new StyleBuilder();
			TextSymbolizer tsym = styleFactory.createTextSymbolizer(
					styleFactory.createFill(filterFactory.literal(Color.BLACK)),
					f,
					sb.createHalo(Color.WHITE, 1d),
					sb.attributeExpression(attributeLabel),
					null,
					null);
			rule.symbolizers().add(tsym);
		}
		
		FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[] { rule });
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);
		return style;
	}
}
