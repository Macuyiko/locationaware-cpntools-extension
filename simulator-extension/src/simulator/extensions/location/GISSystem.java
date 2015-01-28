package simulator.extensions.location;

import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graphable;
import org.geotools.graph.structure.Node;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.map.event.MapLayerListEvent;
import org.opengis.feature.simple.SimpleFeature;

import simulator.extensions.location.model.Graph;
import simulator.extensions.location.ui.LogWindow;
import simulator.extensions.location.ui.MapWindow;
import simulator.extensions.location.utils.GraphUtils;
import simulator.extensions.location.utils.InitUtils.FeatureTypes;
import simulator.extensions.location.utils.MappingUtils;

import com.vividsolutions.jts.geom.Point;

public class GISSystem {
	private RPCHandler rpcHandler;
	private LogWindow logWindow;
	private MapWindow mapWindow;
	private Graph graph;

	public GISSystem(RPCHandler rpcHandler, LogWindow logWindow, MapWindow mapWindow, Graph graph) {
		this.rpcHandler = rpcHandler;
		this.logWindow = logWindow;
		this.mapWindow = mapWindow;
		this.graph = graph;
	}
	
	public synchronized void notifyLayerChange(int... indexes) {
		for (int index : indexes) {
			try {
				MapLayerEvent mple = new MapLayerEvent(MappingUtils.LAYERS[index], MapLayerEvent.DATA_CHANGED);
				MapLayerListEvent mplle = new MapLayerListEvent(mapWindow.getContent(), 
						MappingUtils.LAYERS[index], 
						mapWindow.getContent().layers().indexOf(MappingUtils.LAYERS[index]), mple);
				mapWindow.getMapFrame().getMapPane().layerChanged(mplle);
			} catch (Exception i){}
		}
	}
	
	public void updateRepairTeams(DefaultFeatureCollection routes) {
		// To add later: some activities still assume atomic, instant movement of repair teams
		// namely: shop floor repair and perform final check.
		// Later, this should be modified so that movement also consumes time
		for (String fname : rpcHandler.getLocationPool().getFeatures(FeatureTypes.RepairTeam.name())) {
			SimpleFeature feature = rpcHandler.getLocationPool().getFeature(fname);
			if (feature.getAttribute("status").equals("available")) {
				Node current = graph.get(fname);
				@SuppressWarnings("unchecked")
				List<Edge> outs = current.getEdges();
				Random r = new Random();
				Node newnode = outs.get(r.nextInt(outs.size())).getOtherNode(current);
				graph.put(fname, newnode);
				Point p = ((Point) ((Graphable) newnode).getObject());
				rpcHandler.getLocationPool().getFeature(fname).setDefaultGeometry(p);
			}
			
			if (feature.getAttribute("status").equals("onroute")) {
				Node current = graph.get(fname);
				Node dest = graph.get(feature.getAttribute("destination").toString());
				if (current.equals(dest)) {
					feature.setAttribute("status", "onsite");
					logWindow.showInfo("Repair team "+fname+" is onsite");
				} else {
					logWindow.showInfo("Repair team "+fname+" is onroute to "+dest);
					moveStepCloser(routes, fname, current, dest);
				}
			}
		}
	}
	
	public void updateInspectors(DefaultFeatureCollection routes) {
		for (String fname : rpcHandler.getLocationPool().getFeatures(FeatureTypes.Inspector.name())) {
			// To add later: some activities still assume atomic, instant movement of inspectors
			// namely: write report. Later, this should be modified so that movement also consumes time
			SimpleFeature feature = rpcHandler.getLocationPool().getFeature(fname);
			
			if (feature.getAttribute("status").equals("onroute")) {
				Node current = graph.get(fname);
				String destination = feature.getAttribute("destination").toString();
				Node dest = graph.get(destination);
				if (current.equals(dest)) {
					if (rpcHandler.getLocationPool().getFeatureType(destination).equals(FeatureTypes.Customer.name())) {
						feature.setAttribute("status", "onsite");
						logWindow.showInfo("Inspector "+fname+" is onsite");
					} else {
						feature.setAttribute("status", "available");
						logWindow.showInfo("Inspector "+fname+" is back to call center");
					}
				} else {
					logWindow.showInfo("Inspector "+fname+" is onroute to "+dest);
					moveStepCloser(routes, fname, current, dest);
				}
			}
		}
	}
	
	public void moveStepCloser(DefaultFeatureCollection routes, String fname, Node current, Node dest) {
		Path shortest = GraphUtils.getShortestRoute(current, dest, graph.getGenerator().getGraph());
		List<?> edges = shortest.getEdges();
		for (int i = 0; i < edges.size() - 1; i++) {
			SimpleFeature edgeFeature = (SimpleFeature) ((Graphable) edges.get(i)).getObject();
			routes.add(edgeFeature);
		}
		Node newnode = (Node) shortest.get(shortest.size()-2);
		graph.put(fname, newnode);
		Point p = ((Point) ((Graphable) newnode).getObject());
		rpcHandler.getLocationPool().getFeature(fname).setDefaultGeometry(p);
	}
	
	public void startPoller() {
		final DefaultFeatureCollection routes = new DefaultFeatureCollection(null, null);
		
		Timer moveTeamsTimer = new Timer(true);
		moveTeamsTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				routes.clear();
				updateRepairTeams(routes);
				updateInspectors(routes);
			}
		}, 0, 1 * 1000);
		
		Timer redrawTimer = new Timer(true);
		redrawTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				try {
			//	mapWindow.getContent().removeLayer(MappingUtils.LAYERS[MappingUtils.LAYER_ROUTE]);
			//	MappingUtils.LAYERS[MappingUtils.LAYER_ROUTE] = new FeatureLayer(routes, style);
			//	mapWindow.getContent().addLayer(MappingUtils.LAYERS[MappingUtils.LAYER_ROUTE]);
					notifyLayerChange(MappingUtils.LAYER_INSPECTORS, MappingUtils.LAYER_REPAIRTEAMS, MappingUtils.LAYER_ROUTE);
					try { Thread.sleep(100); } catch (InterruptedException ex) {}
					mapWindow.getMapFrame().getMapPane().repaint();
					try { Thread.sleep(100); } catch (InterruptedException ex) {}
				} catch (Exception e) {
					logWindow.showInfo(e.getMessage());
					logWindow.showInfo("*** Restarting map screen ***");
					MappingUtils.clearMapContent(mapWindow.getContent());
					MappingUtils.initializeMapContentFromLocationPool(rpcHandler.getLocationPool(), mapWindow.getContent());
				}
			}
		}, 0, 1 * 1000);
		
	}
}
