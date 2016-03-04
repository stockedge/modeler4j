package src.com.stormcolor;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.Timer;


public class Modeler extends JFrame {
	public MainUI mainUI;	
	public Machine machine;	
	public Serial serial;
	Timer timerTMP;
	
	public RecentFiles recentFiles;
	
	public Modeler() {		
		System.out.println("Initializing StormModeler");
		
		mainUI = new MainUI(this);
		
		machine = new Machine(this);
		
		mainUI.initialTHposX.setText(Float.toString(machine.currentPos.x));
		mainUI.initialTHposY.setText(Float.toString(machine.currentPos.y));
		machine.setPenDiameter(machine.penDiameterTH);
		//machine.updateCurrentPos_GUI();
		
		mainUI.setVisible(true);
		mainUI.ui_resize();
		
		
		serial = new Serial(this);        
		try {
			serial.connect("COM1");	         
        } catch ( Exception e ) {
            e.printStackTrace();
        }
		
		recentFiles = new RecentFiles(this);
		
		
		
		
		mainUI.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
            	mainUI.ui_resize();
            }
        });
    }
	
	public void loadGerberFile(File file) {		
		System.out.println("Loading "+file.getAbsolutePath());
		
		//String[] files = recentFiles.getRecentFiles();
		recentFiles.addRecentFile(file);
		
		
		mainUI.resetMovementStack();
		machine.currentStackId = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine();
			while (line != null) {
				if(line.startsWith("X")) { // X-47650Y+38850D02*
					String[] explA = line.split("Y");// X-47650 +38850D02*
					String xVal = explA[0].substring(1, explA[0].length()-1); // -4765
					String yVal = explA[1]; // +38850D02*
					
					String[] explB = yVal.split("D"); // +38850 02*
					yVal = explB[0].substring(0, explB[0].length()-1); // +3885
					String mode = explB[1].substring(1, 2); // 2
					
					mainUI.addMovementToStack(Float.parseFloat(xVal), Float.parseFloat(yVal), mode);
				}
			   
			   // next line
			   line = reader.readLine();
			}
			
			mainUI.updateCanvas();
			mainUI.fileLoaded = true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		mainUI.btnExec.setVisible(true);
		mainUI.btnSignalFB.setVisible(true);
		mainUI.btnSignalLR.setVisible(true);
		//stackCanvasMovements.optimizeStack();
		mainUI.updateUIMovementList();
	}
}
