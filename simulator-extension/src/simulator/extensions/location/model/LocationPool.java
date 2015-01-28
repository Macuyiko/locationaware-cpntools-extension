package simulator.extensions.location.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opengis.feature.simple.SimpleFeature;

public class LocationPool {
	private final Set<String> featureTypes = new HashSet<String>();
	private final Map<String, SimpleFeature> features = new HashMap<String, SimpleFeature>();
	private final Map<String, String> featureTypeMap = new HashMap<String, String>();
	
	public boolean isFeatureType(String typeName) {
		return featureTypes.contains(typeName);
	}
	
	public boolean isFeature(String name) {
		return features.containsKey(name);
	}
	
	public void addFeature(String name, String typeName, SimpleFeature feature) {
		featureTypes.add(typeName);
		features.put(name, feature);
		featureTypeMap.put(name, typeName);
	}
	
	public void removeFeature(String name) {
		features.remove(name);
		featureTypeMap.remove(name);
	}
	
	public void addFeatureType(String typeName) {
		featureTypes.add(typeName);
	}
	
	public void removeFeatureType(String typeName) {
		featureTypes.remove(typeName);
		Set<Entry<String, String>> entrySet = featureTypeMap.entrySet();
		for (Entry<String, String> e : entrySet) {
			if (e.getValue().equals(typeName)) {
				features.remove(e.getKey());
				featureTypeMap.remove(e.getKey());
			}
		}
	}
	
	public Set<String> getFeatureTypes() {
		return new HashSet<String>(featureTypes);
	}
	
	public Set<String> getFeatures() {
		return new HashSet<String>(features.keySet());
	}
	
	public Set<String> getFeatures(String typeName) {
		Set<String> features = new HashSet<String>();
		for (Entry<String, String> e : featureTypeMap.entrySet()) {
			if (e.getValue().equals(typeName)) {
				features.add(e.getKey());
			}
		}
		return features;
	}
	
	public SimpleFeature getFeature(String name) {
		return features.get(name);
	}

	public String getFeatureType(String name) {
		return featureTypeMap.get(name);
	}
}
