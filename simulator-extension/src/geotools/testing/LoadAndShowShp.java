package geotools.testing;

import java.io.File;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.map.FeatureLayer;
import org.geotools.map.MapContent;
import org.geotools.styling.Style;
import org.geotools.swing.JMapFrame;

public class LoadAndShowShp {

	private static String[] shapeFiles = new String[] {
			"data/callcenter1/line/line_Shapefile.shp",
			"data/callcenter1/point/point2_Shapefile.shp",
			"data/callcenter1/polygon/polygon_Shapefile.shp" };
	
	public static void main(String[] args) throws Exception {
		LoadAndShowShp me = new LoadAndShowShp();
		JMapFrame.showMap(me.getMap());
	}

	public MapContent getMap() throws Exception {
		
		MapContent map = new MapContent();
		map.setTitle("Example Map Window");
		
		for (String shapeFile : shapeFiles) {
			System.out.println("* PARSING: "+shapeFile);

			File file = new File(shapeFile);
	        SimpleFeatureSource featureSource = GeoUtils.getFeatureSourceFromShapefile(file);
			
			Style style = GeoUtils.createStyle(featureSource);
			FeatureLayer layer = new FeatureLayer(featureSource, style);
			map.addLayer(layer);
			
			SimpleFeatureCollection features = featureSource.getFeatures();
			FeatureIterator<?> iterator = features.features();
			while (iterator.hasNext()) {
				SimpleFeatureImpl feature = (SimpleFeatureImpl) iterator.next();
				System.out.println("*** FEATURE: "+feature.getAttribute("Name"));
				System.out.println("             "+feature);	
			}
			
		}
		
		return map;
	}

	

}

