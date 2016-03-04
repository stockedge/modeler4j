package src.com.stormcolor;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.ScrollPaneConstants;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;

public class MainUI extends JFrame {

	private Modeler modeler;
	
	public JPanel contentPane;
	public JTextField numSteps;
	public JTextField initialTHposX;
	public JTextField initialTHposY;
	public JTextField currentPosX;
	public JTextField currentPosY;
	public JTextField TargetPosX;
	public JTextField TargetPosY;
	public JButton btnLeft;
	public JButton btnFront;
	public JButton btnRight;
	public JButton btnBack;
	public JButton btnUp;
	public JButton btnDown;
	public JTextField zStatus;
	private JButton BTNminorZoom;
	private JButton BTNmoreZoom;
	private JTextField initialTHposZ;
	public JTextField penDiameter;
	public JTextField penDiameterValue;
	private JLabel lblPenDiameter;
	
	
	public CanvasView canvas;
	public JPanel canvasMovementUIList;
	public JButton btnSignalFB;
	public JButton btnSignalLR;
	public JLabel canvasMovementCountUI;
	public JTextField execStartId;
	public JButton btnExec;
	public JScrollPane scrollPane;
	
	
	public Movements stackCanvasMovements;	
	public int selectedTrace = -1;
	public float canvasScale = 0.275f;
	
	public Boolean fileLoaded = false;
	
	/**
	 * Create the frame.
	 */
	public MainUI(final Modeler modeler) {
		this.modeler = modeler;
		
		setTitle("MODELER4J");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		Toolkit tk = Toolkit.getDefaultToolkit();  
		int xSize = ((int) tk.getScreenSize().getWidth());  
		int ySize = ((int) tk.getScreenSize().getHeight());
		setBounds(100, 100, xSize, ySize);
		
		this.setExtendedState(this.getExtendedState()|JFrame.MAXIMIZED_BOTH);
		
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);
		
		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);
		
		JMenuItem mntmOpen = new JMenuItem("Open Gerber File...");
		mntmOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				// FILE CHOOSER
				JFileChooser chooser = new JFileChooser();
				FileNameExtensionFilter filter = new FileNameExtensionFilter(
				    "TXT files", "txt");
				chooser.setFileFilter(filter);
				//chooser.setCurrentDirectory("<YOUR DIR COMES HERE>");
				int returnVal = chooser.showOpenDialog(MainUI.this);
				if(returnVal == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					modeler.loadGerberFile(file);
				}
				
			}
		});
		mnFile.add(mntmOpen);
		
		final JMenu mnRecentFiles = new JMenu("Recent Files");
		mnRecentFiles.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent arg0) {
				mnRecentFiles.removeAll();
				String[] files = modeler.recentFiles.getRecentFiles().split(",");
				for(int n=files.length-1; n >= 0; n--) { 
					final String fileName = files[n];
					JMenuItem mntmRFile = new JMenuItem(fileName);
					mntmRFile.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							
							modeler.loadGerberFile(new File(fileName));
							
						}
					});
					mnRecentFiles.add(mntmRFile);
				}
			}
		});
		mnFile.add(mnRecentFiles);
		
		
		
		
		// TEXT FIELD NUM STEPS
		numSteps = new JTextField();
		numSteps.setText("1");
		numSteps.setBounds(10, 30, 80, 20);
		contentPane.add(numSteps);
		numSteps.setColumns(10);
				
				
		
		
				
		// BUTTON STEPS LEFT
		btnLeft = new JButton("left");		
		btnLeft.setContentAreaFilled(false);
		btnLeft.setOpaque(true);
		btnLeft.setBackground(Color.CYAN);
		btnLeft.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnLeft.setBackground(Color.ORANGE);
				SwingUtilities.invokeLater(new Runnable(){  
		            public void run(){
		            	modeler.machine.stepsLeftCommand(Integer.parseInt(numSteps.getText()), modeler.machine.STEP_ACUM_MAX);
		            } 
				});	 
			}
		});
		btnLeft.setBounds(10, 91, 80, 80);
		contentPane.add(btnLeft);
		
		// BUTTON STEPS RIGHT
		btnRight = new JButton("right");
		btnRight.setContentAreaFilled(false);
		btnRight.setOpaque(true);
		btnRight.setContentAreaFilled(false);
		btnRight.setBackground(Color.CYAN);
		btnRight.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnRight.setBackground(Color.ORANGE);
				SwingUtilities.invokeLater(new Runnable(){  
		            public void run(){
		            	modeler.machine.stepsRightCommand(Integer.parseInt(numSteps.getText()), modeler.machine.STEP_ACUM_MAX);
		            } 
				});	 
			}
		});
		btnRight.setBounds(190, 91, 80, 80);
		contentPane.add(btnRight);
				
		// BUTTON STEPS FRONT
		btnFront = new JButton("front");
		btnFront.setContentAreaFilled(false);
		btnFront.setOpaque(true);
		btnFront.setBackground(Color.CYAN);
		btnFront.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnFront.setBackground(Color.ORANGE);
				SwingUtilities.invokeLater(new Runnable(){  
		            public void run(){
		            	modeler.machine.stepsFrontCommand(Integer.parseInt(numSteps.getText()), modeler.machine.STEP_ACUM_MAX);
		            } 
				});	 
			}
		});		
		btnFront.setBounds(100, 182, 80, 80);
		contentPane.add(btnFront);
				
		// BUTTON STEPS BACK
		btnBack = new JButton("back");
		btnBack.setContentAreaFilled(false);
		btnBack.setOpaque(true);
		btnBack.setBackground(Color.CYAN);
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnBack.setBackground(Color.ORANGE);
				SwingUtilities.invokeLater(new Runnable(){  
		            public void run(){
		            	modeler.machine.stepsBackCommand(Integer.parseInt(numSteps.getText()), modeler.machine.STEP_ACUM_MAX);
		            } 
				});	 
			}
		});
		btnBack.setBounds(100, 0, 80, 80);
		contentPane.add(btnBack);
		
		// BUTTON STEPS UP
		btnUp = new JButton("up");
		btnUp.setBackground(Color.LIGHT_GRAY);
		btnUp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnUp.setBackground(Color.ORANGE);
				SwingUtilities.invokeLater(new Runnable(){  
		            public void run(){
		            	modeler.machine.stepsUpCommand(Integer.parseInt(numSteps.getText()), modeler.machine.STEP_ACUM_MAX);
		            } 
				});	 
			}
		});
		btnUp.setBounds(100, 91, 80, 40);
		contentPane.add(btnUp);
		
		// BUTTON STEPS DOWN
		btnDown = new JButton("down");
		btnDown.setBackground(Color.LIGHT_GRAY);
		btnDown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnDown.setBackground(Color.ORANGE);
				SwingUtilities.invokeLater(new Runnable(){  
		            public void run(){
		            	modeler.machine.stepsDownCommand(Integer.parseInt(numSteps.getText()), modeler.machine.STEP_ACUM_MAX);
		            } 
				});	 
			}
		});
		btnDown.setBounds(100, 131, 80, 40);
		contentPane.add(btnDown);
		
		// BUTTON STOP
		JButton btnStop = new JButton("STOP");
		btnStop.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				modeler.machine.stop();
			}
		});
		btnStop.setContentAreaFilled(false);
		btnStop.setOpaque(true);
		btnStop.setBackground(Color.RED);
		btnStop.setBounds(190, 182, 80, 80);
		contentPane.add(btnStop);
		
		
		
		
		
		// CURRENT POS LABEL
		JLabel lblCurrentPos = new JLabel("Current position");
		lblCurrentPos.setBounds(284, 0, 177, 14);
		contentPane.add(lblCurrentPos);
		
		// CURRENT Z LABEL
		JLabel lblCurrentZ = new JLabel("Current Z");
		lblCurrentZ.setBounds(472, 0, 83, 14);
		contentPane.add(lblCurrentZ);
				
		// TEXT FIELD INITIAL POS X
		initialTHposX = new JTextField();
		initialTHposX.setBounds(284, 15, 86, 20);
		initialTHposX.setColumns(10);
		initialTHposX.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
			    warn();
			  }
			  public void removeUpdate(DocumentEvent e) {
			    warn();
			  }
			  public void insertUpdate(DocumentEvent e) {
			    warn();
			  }

			  public void warn() {
				  if(!initialTHposX.getText().equals("")) {
					  modeler.machine.setCurrentPosX((modeler.machine.viewInMM)?Float.parseFloat(initialTHposX.getText())/modeler.machine.mm2th:Float.parseFloat(initialTHposX.getText()));
				  }
			}
		});
		contentPane.add(initialTHposX);
		
		// TEXT FIELD INITIAL POS Y
		initialTHposY = new JTextField();
		initialTHposY.setBounds(375, 15, 86, 20);
		initialTHposY.setColumns(10);
		initialTHposY.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
			    warn();
			  }
			  public void removeUpdate(DocumentEvent e) {
			    warn();
			  }
			  public void insertUpdate(DocumentEvent e) {
			    warn();
			  }

			  public void warn() {
				  if(!initialTHposY.getText().equals("")) {
					  modeler.machine.setCurrentPosY((modeler.machine.viewInMM)?Float.parseFloat(initialTHposY.getText())/modeler.machine.mm2th:Float.parseFloat(initialTHposY.getText()));
				  }
			}
		});
		contentPane.add(initialTHposY);
		
		initialTHposZ = new JTextField();
		initialTHposZ.setBounds(469, 15, 86, 20);
		initialTHposZ.setColumns(10);
		initialTHposZ.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
			    warn();
			  }
			  public void removeUpdate(DocumentEvent e) {
			    warn();
			  }
			  public void insertUpdate(DocumentEvent e) {
			    warn();
			  }

			  public void warn() {
				  if(!initialTHposZ.getText().equals("")) {
					  modeler.machine.setCurrentPosZ(Float.parseFloat(initialTHposZ.getText()));
				  }
			}
		});
		contentPane.add(initialTHposZ);
		
		// TEXT FIELD INITIAL POS X (READ-ONLY)
		currentPosX = new JTextField();
		currentPosX.setBackground(Color.LIGHT_GRAY);
		currentPosX.setEditable(false);
		currentPosX.setBounds(284, 33, 86, 20);
		contentPane.add(currentPosX);
		currentPosX.setColumns(10);
		
		// TEXT FIELD INITIAL POS Y (READ-ONLY)
		currentPosY = new JTextField();
		currentPosY.setBackground(Color.LIGHT_GRAY);
		currentPosY.setEditable(false);
		currentPosY.setBounds(375, 33, 86, 20);
		contentPane.add(currentPosY);
		currentPosY.setColumns(10);
		
		// TEXT FIELD INITIAL POS Z (READ-ONLY)
		zStatus = new JTextField();
		zStatus.setBackground(Color.LIGHT_GRAY);
		zStatus.setEditable(false);
		zStatus.setBounds(469, 33, 86, 20);
		contentPane.add(zStatus);
		zStatus.setColumns(10);
		
		
		
		
		
		// TARGET POS LABEL
		JLabel lblTargetPos = new JLabel("Target position");
		lblTargetPos.setBounds(284, 56, 177, 14);
		contentPane.add(lblTargetPos);
		
		TargetPosX = new JTextField();
		TargetPosX.setBackground(Color.LIGHT_GRAY);
		TargetPosX.setEditable(false);
		TargetPosX.setBounds(284, 71, 86, 20);
		contentPane.add(TargetPosX);
		TargetPosX.setColumns(10);
		
		TargetPosY = new JTextField();
		TargetPosY.setEditable(false);
		TargetPosY.setBackground(Color.LIGHT_GRAY);
		TargetPosY.setBounds(375, 71, 86, 20);
		contentPane.add(TargetPosY);
		TargetPosY.setColumns(10); 
		
		JButton btnMmth = new JButton("mm/th");
		btnMmth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				modeler.machine.viewInMM = (modeler.machine.viewInMM) ? false : true;
				updateCurrentPos_GUI();
				updateUIMovementList();
			}
		});
		btnMmth.setBounds(479, 70, 76, 23);
		contentPane.add(btnMmth);
		
		
		
		
		
		
		// PEN DIAMETER LABEL
		lblPenDiameter = new JLabel("Pen Diameter");
		lblPenDiameter.setBounds(284, 102, 86, 14);
		contentPane.add(lblPenDiameter);
		
		penDiameter = new JTextField();
		penDiameter.setBounds(284, 121, 86, 20);
		penDiameter.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
			    warn();
			  }
			  public void removeUpdate(DocumentEvent e) {
			    warn();
			  }
			  public void insertUpdate(DocumentEvent e) {
			    warn();
			  }

			  public void warn() {
				  if(!penDiameter.getText().equals("")) {
					  modeler.machine.setPenDiameter((modeler.machine.viewInMM)?Float.parseFloat(penDiameter.getText())/modeler.machine.mm2th:Float.parseFloat(penDiameter.getText()));
				  }
			}
		});
		contentPane.add(penDiameter);
		penDiameter.setColumns(10);
		
		penDiameterValue = new JTextField();
		penDiameterValue.setBackground(Color.LIGHT_GRAY);
		penDiameterValue.setEditable(false);
		penDiameterValue.setBounds(284, 139, 86, 20);
		contentPane.add(penDiameterValue);
		penDiameterValue.setColumns(10);
		
		
		
		
		
		
		
		
		BTNminorZoom = new JButton("-");
		BTNminorZoom.setBackground(Color.LIGHT_GRAY);
		BTNminorZoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				canvasScale *= 0.9f;
				updateCanvas();
			}
		});		
		BTNminorZoom.setBounds(274, 222, 50, 40);
		contentPane.add(BTNminorZoom);
		
		BTNmoreZoom = new JButton("+");
		BTNmoreZoom.setBackground(Color.LIGHT_GRAY);
		BTNmoreZoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				canvasScale *= 1.1f;
				updateCanvas();
			}
		});
		BTNmoreZoom.setBounds(323, 222, 50, 40);
		contentPane.add(BTNmoreZoom);
		
		
		
		JButton btnOptimize = new JButton("Optimize");
		btnOptimize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				stackCanvasMovements.optimizeStack();
				updateCanvas();
				updateUIMovementList();
				System.out.println("Optimization OK");
			}
		});
		btnOptimize.setBounds(372, 231, 89, 23);
		contentPane.add(btnOptimize);
		
		
		
		
		
		
		btnSignalFB = new JButton("FB_OK SIGNAL");		
		btnSignalFB.setBounds(this.getWidth()-300, 0, 100, 20);
		btnSignalFB.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnSignalFB.setForeground(Color.BLACK);
		btnSignalFB.setBackground(Color.BLUE);
		btnSignalFB.setContentAreaFilled(false);
		btnSignalFB.setOpaque(true);
		btnSignalFB.setVisible(false);
		btnSignalFB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				modeler.machine.onSerialReceived("Y");
			}
		});
		contentPane.add(btnSignalFB);
		
		btnSignalLR = new JButton("LR_OK SIGNAL");		
		btnSignalLR.setBounds(this.getWidth()-300, 20, 100, 20);
		btnSignalLR.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnSignalLR.setForeground(Color.BLACK);
		btnSignalLR.setBackground(Color.BLUE);
		btnSignalLR.setContentAreaFilled(false);
		btnSignalLR.setOpaque(true);
		btnSignalLR.setVisible(false);
		btnSignalLR.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				modeler.machine.onSerialReceived("H");
			}
		});
		contentPane.add(btnSignalLR);
		
		canvasMovementCountUI = new JLabel("0");
		canvasMovementCountUI.setBounds(this.getWidth()-185, 0, 80, 20);
		contentPane.add(canvasMovementCountUI);
		
		execStartId = new JTextField();
		execStartId.setText("0");
		execStartId.setBounds(this.getWidth()-140, 0, 30, 20);
		execStartId.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
			    warn();
			  }
			  public void removeUpdate(DocumentEvent e) {
			    warn();
			  }
			  public void insertUpdate(DocumentEvent e) {
			    warn();
			  }

			  public void warn() {
				  if(!execStartId.getText().equals("")) {
					  modeler.machine.setCurrentStackId(Integer.parseInt(execStartId.getText()));
					  updateCurrentPos_GUI();
					  updateUIMovementList();
				  }
			}
		});
		contentPane.add(execStartId);		
		execStartId.setColumns(10);
		
		btnExec = new JButton("EXEC");		
		btnExec.setBounds(this.getWidth()-90, 0, 50, 20);
		btnExec.setFont(new Font("Tahoma", Font.BOLD, 16));
		btnExec.setForeground(Color.BLACK);
		btnExec.setBackground(Color.ORANGE);
		btnExec.setContentAreaFilled(false);
		btnExec.setOpaque(true);
		btnExec.setVisible(false);
		btnExec.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				modeler.machine.execStackMovements();
			}
		});
		contentPane.add(btnExec);
		
		
		canvasMovementUIList = new JPanel();
		FlowLayout flowLayout1 = new FlowLayout(FlowLayout.LEADING, 0, 0);
        canvasMovementUIList.setLayout(flowLayout1);
        canvasMovementUIList.setPreferredSize(new Dimension(170, this.getHeight()-60));
        canvasMovementUIList.setAlignmentX(JPanel.LEFT_ALIGNMENT);
        
		scrollPane = new JScrollPane(canvasMovementUIList);
		scrollPane.setBounds(this.getWidth()-185, 20, 170, this.getHeight()-60);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);		
		contentPane.add(scrollPane);
		
		
		canvas = new CanvasView(modeler);
		canvas.setBounds(10, 268, 572, 331);
		contentPane.add(canvas);
		
		
		
		stackCanvasMovements = new Movements();
	}
	
	public void ui_resize() {
		btnSignalFB.setBounds(this.getWidth()-300, 0, 100, 20);
		btnSignalLR.setBounds(this.getWidth()-300, 20, 100, 20);
		canvasMovementCountUI.setBounds(this.getWidth()-185, 0, 80, 20);
		execStartId.setBounds(this.getWidth()-140, 0, 30, 20);
		btnExec.setBounds(this.getWidth()-90, 0, 50, 20);
    	scrollPane.setBounds(this.getWidth()-185, 20, 170, this.getHeight()-60);
    	canvas.setBounds(10, 268, this.getWidth()-200 , this.getHeight()-330);
    	
    	scrollPane.repaint();
		scrollPane.revalidate();
	}
	
	public void resetMovementStack() {
		canvasMovementCountUI.setBounds(this.getWidth()-185, 0, 80, 20);
    	btnExec.setBounds(this.getWidth()-120, 0, 80, 20);
    	scrollPane.setBounds(this.getWidth()-185, 20, 170, this.getHeight()-60);
    	canvas.setBounds(10, 268, this.getWidth()-200 , this.getHeight()-330);
    	
    	scrollPane.repaint();
		scrollPane.revalidate();
	}
	
	public void addMovementToStack(Float xVal, Float yVal, String isStartTraceValues) { 
		stackCanvasMovements.addMovementToStack(xVal, yVal, isStartTraceValues);
	}
	
	public void updateCanvas() {		
		drawCanvas((Graphics2D)canvas.getGraphics());
	}
	
	public void updateCurrentPos_GUI() {
		currentPosX.setText((modeler.machine.viewInMM)?Float.toString(modeler.machine.currentPos.x*modeler.machine.mm2th)+"mm":Float.toString(modeler.machine.currentPos.x)+"th");
		currentPosY.setText((modeler.machine.viewInMM)?Float.toString(modeler.machine.currentPos.y*modeler.machine.mm2th)+"mm":Float.toString(modeler.machine.currentPos.y)+"th");
		zStatus.setText(Float.toString(modeler.machine.currentPosZ)); 
		penDiameterValue.setText((modeler.machine.viewInMM)?Float.toString(modeler.machine.penDiameterTH*modeler.machine.mm2th)+"mm":Float.toString(modeler.machine.penDiameterTH)+"th");
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
			if(modeler.machine.currentStackId > n) {
				btnA.setBackground(Color.GRAY);
			} else if(modeler.machine.currentStackId == n) {
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
				if((modeler.machine.viewInMM)) btnFront.setText(stackCanvasMovements.xValues.get(n+1)*modeler.machine.mm2th+","+stackCanvasMovements.yValues.get(n+1)*modeler.machine.mm2th);			
				else btnFront.setText(stackCanvasMovements.xValues.get(n+1)+","+stackCanvasMovements.yValues.get(n+1));
				btnFront.setBackground(Color.WHITE);
			} else if(stackCanvasMovements.isStartTraceValues.get(n+1).equals("2")) {
				if((modeler.machine.viewInMM)) btnFront.setText(stackCanvasMovements.xValues.get(n)*modeler.machine.mm2th+","+stackCanvasMovements.yValues.get(n)*modeler.machine.mm2th+" -> "+stackCanvasMovements.xValues.get(n+1)*modeler.machine.mm2th+","+stackCanvasMovements.yValues.get(n+1)*modeler.machine.mm2th);			
				else btnFront.setText(stackCanvasMovements.xValues.get(n)+","+stackCanvasMovements.yValues.get(n)+" -> "+stackCanvasMovements.xValues.get(n+1)+","+stackCanvasMovements.yValues.get(n+1));
				btnFront.setBackground(Color.WHITE);	 			
			} else if(stackCanvasMovements.isStartTraceValues.get(n+1).equals("1")) {
				if((modeler.machine.viewInMM)) btnFront.setText(stackCanvasMovements.xValues.get(n)*modeler.machine.mm2th+","+stackCanvasMovements.yValues.get(n)*modeler.machine.mm2th+" -> "+stackCanvasMovements.xValues.get(n+1)*modeler.machine.mm2th+","+stackCanvasMovements.yValues.get(n+1)*modeler.machine.mm2th);			
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
		canvasMovementCountUI.setText(modeler.machine.currentStackId+"/"+stackCanvasMovements.xValues.size());
		
		scrollPane.repaint();
		scrollPane.revalidate();
	}
	
	protected void drawCanvas(Graphics2D g2D) {
		if(fileLoaded == true) {
			g2D.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			Line2D line1;
			final float dash1[] = {4.0f};
			BasicStroke strokeDashed = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, dash1, 0.0f);
			BasicStroke strokeDashedHiglighted = new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f, dash1, 0.0f);
			BasicStroke strokeSolid = new BasicStroke(modeler.machine.penDiameterTH*canvasScale, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			BasicStroke strokeSolidHiglighted = new BasicStroke((modeler.machine.penDiameterTH*canvasScale)*1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
			
			// draw down traces
			for(int n=0; n < stackCanvasMovements.getStackMovementSize(); n++) {
			    if(stackCanvasMovements.xValues.size() > n+1) {
			    	if(stackCanvasMovements.isStartTraceValues.get(n+1).equals("1")) {
			    		// next is normal
			    		g2D.setStroke(strokeSolid);
			    		if(modeler.machine.executingStackCanvasMovements == true) {
			    			if(modeler.machine.currentStackId > n) {
			    				g2D.setColor(Color.BLUE);
			    			} else if(modeler.machine.currentStackId == n) {
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
			    		if(modeler.machine.executingStackCanvasMovements == true) {
			    			if(modeler.machine.currentStackId > n) {
			    				g2D.setColor(Color.BLUE);
			    			} else if(modeler.machine.currentStackId == n) {
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
			    		if(modeler.machine.executingStackCanvasMovements == true) {
			    			if(modeler.machine.currentStackId > n) {
			    				g2D.setColor(new Color(0f,0f,0f,0f));
			    			} else if(modeler.machine.currentStackId == n) {
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
			if(modeler.machine.currentPosZ.equals(0)) {
				g2D.setColor(Color.RED);
			} else {
				g2D.setColor(Color.CYAN);
			}
			line1 = new Line2D.Float((modeler.machine.currentPos.x-35f)*canvasScale, modeler.machine.currentPos.y*-canvasScale,
									(modeler.machine.currentPos.x+35f)*canvasScale, modeler.machine.currentPos.y*-canvasScale);
			g2D.draw(line1);
			
			line1 = new Line2D.Float(modeler.machine.currentPos.x*canvasScale, (modeler.machine.currentPos.y-35f)*-canvasScale,
									modeler.machine.currentPos.x*canvasScale, (modeler.machine.currentPos.y+35f)*-canvasScale);
			g2D.draw(line1);
			
			
			// dispose
			g2D.dispose();
		}
	}
}
