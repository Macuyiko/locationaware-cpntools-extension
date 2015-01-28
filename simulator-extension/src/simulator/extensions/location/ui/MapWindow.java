package simulator.extensions.location.ui;

import org.geotools.map.MapContent;
import org.geotools.swing.JMapFrame;

public class MapWindow {
	
	private final JMapFrame mapFrame;
	private final MapContent content;

	public MapWindow() {
		this(new MapContent());
		content.setTitle("Map Overview");
	}
	
	public MapWindow(MapContent content) {
		this.content = content;
		mapFrame = new JMapFrame();
		mapFrame.setMapContent(content);
		mapFrame.enableToolBar(true);
		mapFrame.pack();
		mapFrame.setSize(800, 600);
		mapFrame.setVisible(true);
	}
	
	public JMapFrame getMapFrame() {
		return mapFrame;
	}
	
	public MapContent getContent() {
		return content;
	}

	
}
