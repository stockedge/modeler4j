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
import java.io.File;

import javax.swing.JTextField;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainUI extends JFrame {

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
	
	/**
	 * Create the frame.
	 */
	public MainUI(final Modeler modeler) {
		setTitle("MODELER4J");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 777, 664);
		
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
		btnFront.setBounds(100, 182, 80, 80);
		contentPane.add(btnFront);
		
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
		
		// TEXT FIELD NUM STEPS
		numSteps = new JTextField();
		numSteps.setText("1");
		numSteps.setBounds(10, 30, 80, 20);
		contentPane.add(numSteps);
		numSteps.setColumns(10);
		
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
		
		// CURRENT POS
		JLabel lblCurrentPos = new JLabel("Current position");
		lblCurrentPos.setBounds(284, 0, 177, 14);
		contentPane.add(lblCurrentPos);
		
		currentPosX = new JTextField();
		currentPosX.setBackground(Color.LIGHT_GRAY);
		currentPosX.setEditable(false);
		currentPosX.setBounds(284, 33, 86, 20);
		contentPane.add(currentPosX);
		currentPosX.setColumns(10);
		
		currentPosY = new JTextField();
		currentPosY.setBackground(Color.LIGHT_GRAY);
		currentPosY.setEditable(false);
		currentPosY.setBounds(375, 33, 86, 20);
		contentPane.add(currentPosY);
		currentPosY.setColumns(10);
		
		zStatus = new JTextField();
		zStatus.setBackground(Color.LIGHT_GRAY);
		zStatus.setEditable(false);
		zStatus.setBounds(469, 33, 86, 20);
		contentPane.add(zStatus);
		zStatus.setColumns(10);
		
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
		
		BTNminorZoom = new JButton("-");
		BTNminorZoom.setBackground(Color.LIGHT_GRAY);
		BTNminorZoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				modeler.canvasScale *= 0.9f;
				modeler.updateCanvas();
			}
		});
		
		JButton btnMmth = new JButton("mm/th");
		btnMmth.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				modeler.machine.viewInMM = (modeler.machine.viewInMM) ? false : true;
				modeler.updateCurrentPos_GUI();
				modeler.updateUIMovementList();
			}
		});
		btnMmth.setBounds(479, 70, 76, 23);
		contentPane.add(btnMmth);
		BTNminorZoom.setBounds(274, 222, 50, 40);
		contentPane.add(BTNminorZoom);
		
		BTNmoreZoom = new JButton("+");
		BTNmoreZoom.setBackground(Color.LIGHT_GRAY);
		BTNmoreZoom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				modeler.canvasScale *= 1.1f;
				modeler.updateCanvas();
			}
		});
		BTNmoreZoom.setBounds(323, 222, 50, 40);
		contentPane.add(BTNmoreZoom);
		
		JLabel lblCurrentZ = new JLabel("Current Z");
		lblCurrentZ.setBounds(472, 0, 83, 14);
		contentPane.add(lblCurrentZ);
		
		JButton btnOptimize = new JButton("Optimize");
		btnOptimize.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				modeler.stackCanvasMovements.optimizeStack();
			}
		});
		btnOptimize.setBounds(372, 231, 89, 23);
		contentPane.add(btnOptimize);
		
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
		
		lblPenDiameter = new JLabel("Pen Diameter");
		lblPenDiameter.setBounds(284, 102, 86, 14);
		contentPane.add(lblPenDiameter);
		
		
	}
}
