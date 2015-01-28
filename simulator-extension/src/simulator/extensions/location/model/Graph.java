package simulator.extensions.location.model;

import java.util.HashMap;
import java.util.Map;

import org.geotools.graph.build.feature.FeatureGraphGenerator;
import org.geotools.graph.build.line.LineStringGraphGenerator;
import org.geotools.graph.structure.Node;

public class Graph {
	private final FeatureGraphGenerator generator;
	private final Map<String, Node> featureNodeMap = new HashMap<String, Node>();
	
	public Graph() {
		LineStringGraphGenerator lineStringGenerator = new LineStringGraphGenerator();
		generator = new FeatureGraphGenerator(lineStringGenerator);
	}

	public FeatureGraphGenerator getGenerator() {
		return generator;
	}
	
	public void put(String name, Node node) {
		featureNodeMap.put(name, node);
	}
	
	public Node get(String name) {
		return featureNodeMap.get(name);
	}
	
	public void remove(String name) {
		featureNodeMap.remove(name);
	}
}
