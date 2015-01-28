package simulator.extensions.location;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import org.cpntools.simulator.extensions.AbstractExtension;
import org.cpntools.simulator.extensions.Channel;
import org.cpntools.simulator.extensions.Extension;
import org.geotools.map.MapContent;
import simulator.extensions.location.model.Graph;
import simulator.extensions.location.ui.LogWindow;
import simulator.extensions.location.ui.MapWindow;
import simulator.extensions.location.utils.GraphUtils;
import simulator.extensions.location.utils.InitUtils;
import simulator.extensions.location.utils.MappingUtils;

public class LocationAwareExtension extends AbstractExtension {
	
	private static LogWindow LOG_WINDOW ;
	private static RPCHandler RPC_HANDLER;
	private static MapWindow MAP_WINDOW;
	private static Graph GRAPH;
	private static GISSystem GIS_SYSTEM;
	private static boolean isInitialized = false;
	
	public LocationAwareExtension() {	
		super();
		
		if (!isInitialized) {
			LOG_WINDOW = new LogWindow();
		}
		
		System.setErr(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				LOG_WINDOW.showByte((char) b);
			}
		}));
		
		if (!isInitialized) {
			LOG_WINDOW.showInfo("Location Aware Extension initializing ...");
			LOG_WINDOW.showInfo(" - Log window");
			LOG_WINDOW.showInfo(" - RPC handler");
			RPC_HANDLER = new RPCHandler(LOG_WINDOW);
			LOG_WINDOW.showInfo(" - Location pool");
			try {
				InitUtils.initialize(RPC_HANDLER.getLocationPool(), LOG_WINDOW);
			} catch (IOException e) {
				LOG_WINDOW.showInfo("Fatal error: " + e.getMessage());
			}
			LOG_WINDOW.showInfo(" - Graph");
			GRAPH = GraphUtils.createGraphFromLocationPool(RPC_HANDLER.getLocationPool());
			LOG_WINDOW.showInfo(" - Map content");
			MapContent content = new MapContent();
			MappingUtils.initializeMapContentFromLocationPool(RPC_HANDLER.getLocationPool(), content);
			LOG_WINDOW.showInfo(" - Map window");
			MAP_WINDOW = new MapWindow(content);
			LOG_WINDOW.showInfo("- GIS system");
			GIS_SYSTEM = new GISSystem(RPC_HANDLER, LOG_WINDOW, MAP_WINDOW, GRAPH);
			
			Timer gisTimer = new Timer(true);
			gisTimer.schedule(new TimerTask() {
				public void run() {
					GIS_SYSTEM.startPoller();
				}
			}, 10 * 1000);
			
			isInitialized = true;
		}
	}

	@Override
	public Extension start(Channel c) {
		LOG_WINDOW.showInfo("Location Aware Extension starting");
		return super.start(c);
	}

	@Override
	public int getIdentifier() {
		return Extension.TESTING;
	}

	@Override
	public String getName() {
		return "Location Aware Extension";
	}
	
	@Override
	public Object getRPCHandler() {
		LOG_WINDOW.showInfo("getRPCHandler() invoked");
		return RPC_HANDLER;
	}
	
	public static void main(String[] args) {
		LocationAwareExtension lae = new LocationAwareExtension();
		try {
			((RPCHandler)lae.getRPCHandler()).assignFeature("RepairTeam1", "CRT", "Customer4");
		} catch (Exception e) {
			e.printStackTrace();
		}
	//	System.exit(0);
	}
	
	

}

