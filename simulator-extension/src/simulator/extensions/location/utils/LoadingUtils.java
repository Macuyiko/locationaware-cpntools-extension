package simulator.extensions.location.utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeatureType;

import simulator.extensions.location.model.LocationPool;

public class LoadingUtils {
	public static SimpleFeatureType getExtendedFeatureTypeFromFeatureSource(
			String name, Map<String, Class<?>> attributes,
			SimpleFeatureSource featureSource) {
		SimpleFeatureType sft = featureSource.getSchema();
		SimpleFeatureTypeBuilder stb = new SimpleFeatureTypeBuilder();
		stb.init(sft);
		stb.setName(name);
		for (Entry<String, Class<?>> e : attributes.entrySet()) {
			stb.add(e.getKey(), e.getValue());
		}
		SimpleFeatureType newFeatureType = stb.buildFeatureType();
		return newFeatureType;
	}

	public static SimpleFeatureSource getFeatureSourceFromShapefile(File file) {
		try {
			Map<String, Serializable> params = new HashMap<String, Serializable>();
			params.put(ShapefileDataStoreFactory.DBFCHARSET.key, "GB18030");
			params.put(ShapefileDataStoreFactory.URLP.key, file.toURI().toURL());
			params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, true);
			params.put(ShapefileDataStoreFactory.ENABLE_SPATIAL_INDEX.key, true);
			params.put(ShapefileDataStoreFactory.MEMORY_MAPPED.key, false);
			//DataStore store = DataStoreFinder.getDataStore(params);
			ShapefileDataStore store = new ShapefileDataStore(file.toURI().toURL());
			store.setCharset(Charset.forName("GB18030"));
			SimpleFeatureSource featureSource = store.getFeatureSource(store.getTypeNames()[0]);
	        return featureSource;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}
	
	public static DefaultFeatureCollection featureCollectionFromLocationPool(String typeName, LocationPool pool) {
		DefaultFeatureCollection features = new DefaultFeatureCollection(null, null);
		for (String name : pool.getFeatures(typeName)) {
			features.add(pool.getFeature(name));
		}
		return features;
	}
}
