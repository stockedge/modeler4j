package src.com.stormcolor;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class CanvasView extends Canvas {
	private Modeler modeler;
	
	public CanvasView(Modeler modeler) {		
		this.modeler = modeler;
	}
	
	@Override
	public void paint(Graphics g) {
		setBackground(Color.BLACK);
				
		Graphics2D g2D = (Graphics2D)g;
		modeler.drawCanvas(g2D);
	}
}
