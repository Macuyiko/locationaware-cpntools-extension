package simulator.extensions.location.utils;

import java.awt.Color;
import java.util.List;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.styling.Style;

import simulator.extensions.location.model.LocationPool;
import simulator.extensions.location.utils.InitUtils.FeatureTypes;

public class MappingUtils {
	
	public static final int LAYER_REGIONS = 0;
	public static final int LAYER_CALLCENTERS = 1;
	public static final int LAYER_CUSTOMERS = 2;
	public static final int LAYER_REPAIRSTATIONS = 3;
	public static final int LAYER_REPAIRTEAMS = 4;
	public static final int LAYER_INSPECTORS = 5;
	public static final int LAYER_ROADS = 6;
	public static final int LAYER_ROUTE = 7;
	public static final FeatureLayer[] LAYERS = new FeatureLayer[8];
	
	public static void clearMapContent(MapContent content) {
		List<Layer> layers = content.layers();
		for (Layer layer : layers) content.removeLayer(layer);
	}
	
	public static void initializeMapContentFromLocationPool(LocationPool pool, MapContent content) {
		clearMapContent(content);
		
		Style style;

		style = StyleUtils.createPolygonStyle(Color.RED, 3, 1, new float[]{10, 10}, null, 0.1);
		LAYERS[LAYER_REGIONS] = new FeatureLayer(LoadingUtils.featureCollectionFromLocationPool(FeatureTypes.Region.name(), pool), style);
		content.addLayer(LAYERS[LAYER_REGIONS]);
		
		style = StyleUtils.createLineStyle(Color.ORANGE, 8);
		LAYERS[LAYER_ROADS] = new FeatureLayer(LoadingUtils.featureCollectionFromLocationPool(FeatureTypes.Road.name(), pool), style);
		content.addLayer(LAYERS[LAYER_ROADS]);
		
		style = StyleUtils.createPointStyle(Color.GREEN, Color.GREEN, 5, "Name");
		LAYERS[LAYER_CALLCENTERS] = new FeatureLayer(LoadingUtils.featureCollectionFromLocationPool(FeatureTypes.CallCenter.name(), pool), style);
		content.addLayer(LAYERS[LAYER_CALLCENTERS]);
		
		style = StyleUtils.createPointStyle(Color.BLUE, Color.BLUE, 5, "Name");
		LAYERS[LAYER_CUSTOMERS] = new FeatureLayer(LoadingUtils.featureCollectionFromLocationPool(FeatureTypes.Customer.name(), pool), style);
		content.addLayer(LAYERS[LAYER_CUSTOMERS]);
		
		style = StyleUtils.createPointStyle(Color.MAGENTA, Color.MAGENTA, 5, "Name");
		LAYERS[LAYER_REPAIRSTATIONS] = new FeatureLayer(LoadingUtils.featureCollectionFromLocationPool(FeatureTypes.RepairStation.name(), pool), style);
		content.addLayer(LAYERS[LAYER_REPAIRSTATIONS]);
		
		style = StyleUtils.createPointStyle(Color.GRAY, Color.GRAY, 4, null);
		LAYERS[LAYER_REPAIRTEAMS] = new FeatureLayer(LoadingUtils.featureCollectionFromLocationPool(FeatureTypes.RepairTeam.name(), pool), style);
		content.addLayer(LAYERS[LAYER_REPAIRTEAMS]);
		
		style = StyleUtils.createPointStyle(Color.BLACK, Color.BLACK, 4, null);
		LAYERS[LAYER_INSPECTORS] = new FeatureLayer(LoadingUtils.featureCollectionFromLocationPool(FeatureTypes.Inspector.name(), pool), style);
		content.addLayer(LAYERS[LAYER_INSPECTORS]);
		
		style = StyleUtils.createLineStyle(Color.RED, 4);
		LAYERS[LAYER_ROUTE] = new FeatureLayer(new DefaultFeatureCollection(null, null), style);
		content.addLayer(LAYERS[LAYER_ROUTE]);
	}
}
