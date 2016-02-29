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
	
	// CANVAS DRAW
	public CanvasView canvas;
	public JPanel canvasMovementUIList;
	public JLabel canvasMovementCountUI;
	public JButton btnExec;
	public JScrollPane scrollPane;
	public Movements stackCanvasMovements;	
	public int selectedTrace = -1;
	public float canvasScale = 0.275f;
	
	public Serial serial;
	Timer timerTMP;
	
	public RecentFiles recentFiles;
	
	public Modeler() {		
		System.out.println("Initializing StormModeler");
		
		mainUI = new MainUI(this);
		
		machine = new Machine(this);	
		mainUI.setVisible(true);
		
		stackCanvasMovements = new Movements(this);
		
		serial = new Serial(this);        
		try {
			serial.connect("COM1");	         
        } catch ( Exception e ) {
            e.printStackTrace();
        }
		
		recentFiles = new RecentFiles(this);
		
		
		// UI
		canvasMovementCountUI = new JLabel("0");
		canvasMovementCountUI.setBounds(mainUI.getWidth()-185, 0, 80, 20);
		mainUI.contentPane.add(canvasMovementCountUI);
		
		btnExec = new JButton("EXEC");		
		btnExec.setBounds(mainUI.getWidth()-120, 0, 80, 20);
		btnExec.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnExec.setForeground(Color.BLACK);
		btnExec.setBackground(Color.ORANGE);
		btnExec.setContentAreaFilled(false);
		btnExec.setOpaque(true);
		btnExec.setVisible(false);
		btnExec.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				machine.execStackMovements();
			}
		});
		mainUI.contentPane.add(btnExec);
		
		
		canvasMovementUIList = new JPanel();
		FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEADING, 0, 0);
        canvasMovementUIList.setLayout(flowLayout1);
        canvasMovementUIList.setPreferredSize(new Dimension(170, mainUI.getHeight()-60));
        canvasMovementUIList.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
		scrollPane = new JScrollPane(canvasMovementUIList);
		scrollPane.setBounds(mainUI.getWidth()-185, 20, 170, mainUI.getHeight()-60);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);		
		mainUI.contentPane.add(scrollPane);
		
		
		canvas = new CanvasView(this);
		canvas.setBounds(10, 268, 572, 331);
		mainUI.contentPane.add(canvas);
		
		mainUI.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
            	canvasMovementCountUI.setBounds(mainUI.getWidth()-185, 0, 80, 20);
            	btnExec.setBounds(mainUI.getWidth()-120, 0, 80, 20);
            	scrollPane.setBounds(mainUI.getWidth()-185, 20, 170, mainUI.getHeight()-60);
            	canvas.setBounds(10, 268, mainUI.getWidth()-200 , mainUI.getHeight()-330);
            	
            	scrollPane.repaint();
        		scrollPane.revalidate();
            }
        });
				
		mainUI.initialTHposX.setText(Float.toString(machine.currentPos.x));
		mainUI.initialTHposY.setText(Float.toString(machine.currentPos.y));
		machine.setPenDiameter(machine.penDiameterTH);
		//machine.updateCurrentPos_GUI();
    }
	
	public void loadGerberFile(File file) {		
		System.out.println("Loading "+file.getAbsolutePath());
		
		//String[] files = recentFiles.getRecentFiles();
		recentFiles.addRecentFile(file);
		
		
		stackCanvasMovements.resetMovementStack();
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
					
					stackCanvasMovements.addMovementToStack(Float.parseFloat(xVal), Float.parseFloat(yVal), mode);
				}
			   
			   // next line
			   line = reader.readLine();
			}
			
			updateCanvas();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		btnExec.setVisible(true);
		//stackCanvasMovements.optimizeStack();
		updateUIMovementList();
	}
	
	public void updateCurrentPos_GUI() {
		mainUI.currentPosX.setText((machine.viewInMM)?Float.toString(machine.currentPos.x*machine.mm2th)+"mm":Float.toString(machine.currentPos.x)+"th");
		mainUI.currentPosY.setText((machine.viewInMM)?Float.toString(machine.currentPos.y*machine.mm2th)+"mm":Float.toString(machine.currentPos.y)+"th");
		mainUI.zStatus.setText(Float.toString(machine.currentPosZ)); 
		mainUI.penDiameterValue.setText((machine.viewInMM)?Float.toString(machine.penDiameterTH*machine.mm2th)+"mm":Float.toString(machine.penDiameterTH)+"th");
		updateCanvas();
	}
	
	public void updateUIMovementList() {		
		canvasMovementUIList.removeAll();		
		canvasMovementUIList.setPreferredSize(new Dimension(170, stackCanvasMovements.xValues.size()*10));
		
		for(int n=0; n < stackCanvasMovements.xValues.size()-1; n++) {
			JLabel btnA = new JLabel();
			btnA.setHorizontalAlignment(JLabel.LEFT);
			btnA.setOpaque(true);
			btnA.setFont(btnA.getFont().deriveFont(8.0f));
			btnA.setPreferredSize(new Dimension(20, 10));
			btnA.setText(n+"");
			if(machine.currentStackId > n) {
				btnA.setBackground(Color.GRAY);
			} else if(machine.currentStackId == n) {
				btnA.setBackground(Color.YELLOW);
			} else {
				btnA.setBackground(Color.WHITE);
			}
			canvasMovementUIList.add(btnA);
			
			JLabel lbMode = new JLabel();
			lbMode.setHorizontalAlignment(JLabel.LEFT);
			lbMode.setOpaque(true);
			lbMode.setFont(lbMode.getFont().deriveFont(8.0f));
			lbMode.setPreferredSize(new Dimension(10, 10));			
			if(stackCanvasMovements.isStartTraceValues.get(n+1).equals("3")) {
				// next is drill hole
				lbMode.setBackground(Color.GRAY);
			} else if(stackCanvasMovements.isStartTraceValues.get(n+1).equals("2")) {
				// next is start
				lbMode.setBackground(Color.MAGENTA);
			} else if(stackCanvasMovements.isStartTraceValues.get(n+1).equals("1")) {
				// next is normal
				lbMode.setBackground(Color.LIGHT_GRAY);
			}
			canvasMovementUIList.add(lbMode);
			
			JLabel btnFront = new JLabel();
			btnFront.setHorizontalAlignment(JLabel.LEFT);
			//btnFront.setContentAreaFilled(false);
			btnFront.setOpaque(true);
			if(stackCanvasMovements.isStartTraceValues.get(n+1).equals("3")) {
				if((machine.viewInMM)) btnFront.setText(stackCanvasMovements.xValues.get(n+1)*machine.mm2th+","+stackCanvasMovements.yValues.get(n+1)*machine.mm2th);			
				else btnFront.setText(stackCanvasMovements.xValues.get(n+1)+","+stackCanvasMovements.yValues.get(n+1));
				btnFront.setBackground(Color.WHITE);
			} else if(stackCanvasMovements.isStartTraceValues.get(n+1).equals("2")) {
				if((machine.viewInMM)) btnFront.setText(stackCanvasMovements.xValues.get(n)*machine.mm2th+","+stackCanvasMovements.yValues.get(n)*machine.mm2th+" -> "+stackCanvasMovements.xValues.get(n+1)*machine.mm2th+","+stackCanvasMovements.yValues.get(n+1)*machine.mm2th);			
				else btnFront.setText(stackCanvasMovements.xValues.get(n)+","+stackCanvasMovements.yValues.get(n)+" -> "+stackCanvasMovements.xValues.get(n+1)+","+stackCanvasMovements.yValues.get(n+1));
				btnFront.setBackground(Color.WHITE);	 			
			} else if(stackCanvasMovements.isStartTraceValues.get(n+1).equals("1")) {
				if((machine.viewInMM)) btnFront.setText(stackCanvasMovements.xValues.get(n)*machine.mm2th+","+stackCanvasMovements.yValues.get(n)*machine.mm2th+" -> "+stackCanvasMovements.xValues.get(n+1)*machine.mm2th+","+stackCanvasMovements.yValues.get(n+1)*machine.mm2th);			
				else btnFront.setText(stackCanvasMovements.xValues.get(n)+","+stackCanvasMovements.yValues.get(n)+" -> "+stackCanvasMovements.xValues.get(n+1)+","+stackCanvasMovements.yValues.get(n+1));
				btnFront.setBackground(Color.WHITE);
			}
			btnFront.setFont(btnFront.getFont().deriveFont(8.0f));
			//btnFront.setBounds(0, 0, 120, 30);
			btnFront.setPreferredSize(new Dimension(140, 10));
			
			final int currentBtn = n;
			btnFront.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent arg0) {
					selectedTrace = currentBtn;
					updateCanvas();
				}
				@Override
				public void mouseExited(MouseEvent arg0) {
					selectedTrace = -1;
					updateCanvas();
				}
			});
			canvasMovementUIList.add(btnFront);
		}
		canvasMovementCountUI.setText(machine.currentStackId+"/"+stackCanvasMovements.xValues.size());
		
		scrollPane.repaint();
		scrollPane.revalidate();
	}
	
	public void updateCanvas() {		
		drawCanvas((Graphics2D)canvas.getGraphics());
	}
	
	protected void drawCanvas(Graphics2D g2D) {
		g2D.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
		g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		Line2D line1;
		final float dash1[] = {4.0f};
		BasicStroke strokeDashed = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, dash1, 0.0f);
		BasicStroke strokeDashedHiglighted = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, dash1, 0.0f);
		BasicStroke strokeSolid = new BasicStroke(machine.penDiameterTH*canvasScale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		BasicStroke strokeSolidHiglighted = new BasicStroke((machine.penDiameterTH*canvasScale)*1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		
		// draw down traces
		for(int n=0; n < stackCanvasMovements.getStackMovementSize(); n++) {
		    if(stackCanvasMovements.xValues.size() > n+1) {
		    	if(stackCanvasMovements.isStartTraceValues.get(n+1).equals("1")) {
		    		// next is normal
		    		g2D.setStroke(strokeSolid);
		    		if(machine.executingStackCanvasMovements == true) {
		    			if(machine.currentStackId > n) {
		    				g2D.setColor(Color.BLUE);
		    			} else if(machine.currentStackId == n) {
		    				g2D.setStroke(strokeSolidHiglighted);
		    				g2D.setColor(Color.ORANGE);
		    			} else {
		    				g2D.setColor(Color.LIGHT_GRAY);
		    			}
		    		} else {
		    			if(selectedTrace > n) {
		    				g2D.setColor(Color.BLUE);
		    			} else if(selectedTrace == n) {
		    				g2D.setStroke(strokeSolidHiglighted);
		    				g2D.setColor(Color.ORANGE);
		    			}  else {
		    				g2D.setColor(Color.LIGHT_GRAY);
		    			}
		    		}
				    line1 = new Line2D.Float(stackCanvasMovements.xValues.get(n)*canvasScale, stackCanvasMovements.yValues.get(n)*-canvasScale,
				    								stackCanvasMovements.xValues.get(n+1)*canvasScale, stackCanvasMovements.yValues.get(n+1)*-canvasScale);
				    g2D.draw(line1);
		    	} else if(stackCanvasMovements.isStartTraceValues.get(n+1).equals("3")) {
		    		// next is drill hole
			    	g2D.setStroke(strokeSolid);
		    		if(machine.executingStackCanvasMovements == true) {
		    			if(machine.currentStackId > n) {
		    				g2D.setColor(Color.BLUE);
		    			} else if(machine.currentStackId == n) {
		    				g2D.setStroke(strokeSolidHiglighted);
		    				g2D.setColor(Color.ORANGE);
		    			} else {
		    				g2D.setColor(Color.LIGHT_GRAY);
		    			}
		    		} else {
		    			if(selectedTrace > n) {
		    				g2D.setColor(Color.BLUE);
		    			} else if(selectedTrace == n) {
		    				g2D.setStroke(strokeSolidHiglighted);
		    				g2D.setColor(Color.ORANGE);
		    			}  else {
		    				g2D.setColor(Color.LIGHT_GRAY);
		    			}
		    		}
		    		Float nbf = 0f;
		    		for(int nb=0; nb < 12; nb++) {
		    			Float rad = (3.1416f*2f)/12f;	
		    			Float rAx = (float) Math.cos(rad*nbf)*20f;
		    			Float rAy = (float) Math.sin(rad*nbf)*20f;
		    			Float rBx = (float) Math.cos(rad*(nbf+1f))*20f;
		    			Float rBy = (float) Math.sin(rad*(nbf+1f))*20f;
					    line1 = new Line2D.Float(	(stackCanvasMovements.xValues.get(n)+rAx)*canvasScale, (stackCanvasMovements.yValues.get(n)+rAy)*-canvasScale,
					    							(stackCanvasMovements.xValues.get(n)+rBx)*canvasScale, (stackCanvasMovements.yValues.get(n)+rBy)*-canvasScale );
					    nbf += 1f;
					    g2D.draw(line1);
		    		}
			    }
		    }
		}
		// draw up traces
		for(int n=0; n < stackCanvasMovements.getStackMovementSize(); n++) {
		    if(stackCanvasMovements.xValues.size() > n+1) {
		    	if(stackCanvasMovements.isStartTraceValues.get(n+1).equals("2")) {
		    		// next is start
		    		g2D.setStroke(strokeDashed);
		    		if(machine.executingStackCanvasMovements == true) {
		    			if(machine.currentStackId > n) {
		    				g2D.setColor(new Color(0f,0f,0f,0f));
		    			} else if(machine.currentStackId == n) {
		    				g2D.setStroke(strokeDashedHiglighted);
		    				g2D.setColor(Color.RED);
		    			} else {
		    				g2D.setColor(Color.MAGENTA);
		    			}
		    		} else {
		    			if(selectedTrace > n) {
		    				g2D.setColor(new Color(0f,0f,0f,0f));
		    			} else if(selectedTrace == n) {
		    				g2D.setStroke(strokeDashedHiglighted);
		    				g2D.setColor(Color.RED);
		    			}  else {
		    				g2D.setColor(Color.MAGENTA);
		    			}
		    		}
				    line1 = new Line2D.Float(stackCanvasMovements.xValues.get(n)*canvasScale, stackCanvasMovements.yValues.get(n)*-canvasScale,
				    								stackCanvasMovements.xValues.get(n+1)*canvasScale, stackCanvasMovements.yValues.get(n+1)*-canvasScale);
				    g2D.draw(line1);
			    }
		    }
		}
		
		// CROSSHAIR
		if(machine.currentPosZ.equals(0)) {
			g2D.setColor(Color.RED);
		} else {
			g2D.setColor(Color.CYAN);
		}
		line1 = new Line2D.Float((machine.currentPos.x-35f)*canvasScale, machine.currentPos.y*-canvasScale,
								(machine.currentPos.x+35f)*canvasScale, machine.currentPos.y*-canvasScale);
		g2D.draw(line1);
		
		line1 = new Line2D.Float(machine.currentPos.x*canvasScale, (machine.currentPos.y-35f)*-canvasScale,
								machine.currentPos.x*canvasScale, (machine.currentPos.y+35f)*-canvasScale);
		g2D.draw(line1);
		
		
		// dispose
		g2D.dispose();
	}
	
}
