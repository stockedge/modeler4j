package src.com.stormcolor;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.Timer;
import javax.vecmath.Vector2f;

public class Machine {
	private Modeler modeler;

	// 1. Proteus: Asegurarse de que las lineas del esquemático tengan el mismo grosor que nuestro rotulador.
	
	// 2. Proteus: Seleccionar Output > Set Output Origin y marcamos el punto XY más Back/Left(arriba/izquierda en esquemático)
	
	// 3. Proteus: Exportamos a Gerber. Output > Generate Gerber
	
	// 4. StormModeler: Situar el rotulador en la parte más Back/Left(arriba/izquierda en esquemático) de la plataforma (dentro del recuadro)
	// Este será el punto 0,0 indicado a través de "Set Output Origin" de Proteus.	
	// Luego bajar el lapiz hasta que toque la superficie a pintar
	
	// 5. StormModeler: Indicamos en Initial position el punto XY en el que se encuentra el rotulador, que sería 0,0
	//	y fijamos 20 para el eje z (bajara a 0 cuando esciba y subirá a 40 cuando no escriba)
	
	// 6. StormModeler: File > Open y seleccionamos el fichero Gerber.
	
	// 7. StormModeler: Pulsar Run. Este se dirigirá automáticamente hacia el origen del primer trazo. (subirá 20 al comienzo al ser el origen de un trazo)
	// Una vez llegue bajará 40 pasos comenzando así la serie de trazados.
	
	
	// - Para calibrar la maquina se dispone antes en los pic XY el máximo MIN_DELAY que se pueda conseguir.
	// - Fijar en cada pic un "un step" como una secuencia de multiples pasos
	// - Luego en este codigo se fija las variables stepsForX,stepsForY,distanceCMwith_stepsForX,distanceCMwith_stepsForY con los resultados obtenidos
	// tras hacer un recorrido largo con ambos ejes.
	// - Luego para ajustar la velocidad de los ejes mutuamente (para poder dibujar una linea con angulo de 45 ambos a la misma velocidad)
	// se abre el archivo linea45grados de proteus (asegurarse que ocupe la minima diagonal de ambos ejes) y se ejecuta
	// deberemos de ajustar para que terminen el recorrido de 45 grados ambos a la vez. Para ello ajustamos de nuevo el MIN_DELAY del eje
	// que llega antes, aumentando su valor y que viendo vaya terminando cada vez más tarde.
	
	// El firmware utilizado en este caso personal son los siguientes:
	// MINEBEA-17PM-K-041-P1F-XC8 PARA EL EJE Y
	// MITSUMI_M49SP-1-XC8 PARA EL EJE X
	// BIPOLAR-STORM-MODELER-EJEZ-XC8 PARA EL EJE Z
	// Solo hay que fijar las variables para X e Y. El Eje Z va directamente desde el paso 0 (parte más baja) al paso 40 (parte más alta)
	
	// El algoritmo implementado actualmente en este programa es para la realización de trazados con un rotulador a partir de un fichero gerber
	
	// public variables
	private final float stepsForX = 600f;
	private final float stepsForY = 26000f;
	private final float distanceCMwith_stepsForX = 10.09f;
	private final float distanceCMwith_stepsForY = 11.95f;
	
	// private variables
	private final float stepDistanceMMUnitX = (distanceCMwith_stepsForX*10f)/stepsForX;
	private final float stepDistanceMMUnitY = (distanceCMwith_stepsForY*10f)/stepsForY;
	
	public final float mm2th = 0.0254f;
	public final float stepDistanceTHUnitX = stepDistanceMMUnitX/mm2th;
	public final float stepDistanceTHUnitY = stepDistanceMMUnitY/mm2th;
	
	public float penDiameterTH = 20f; 
		
	public Vector2f currentPos = new Vector2f(0f, 0f);		
	public final Float upPosZ = 40f;
	public final Float downPosZ = 0f;
	public Float currentPosZ = upPosZ-20f;
		
	private Vector2f targetPos = new Vector2f(0f, 0f);
	public boolean viewInMM = false;
	private Timer timer;
	private Timer timerTMPX;
	private Timer timerTMPY;
	private int xSteps;
	private int ySteps;
	public int STEP_ACUM_MAX = 32750;
	private int acumNumberX = STEP_ACUM_MAX;
	private int acumNumberY = STEP_ACUM_MAX;
	private float lastDifferenceX = 0f;
	private float lastDifferenceY = 0f;
	
	public Movements stackMovements;
	public boolean executingStackCanvasMovements = false;
	public int currentStackId = 0;
	private int receivedOK = 0;
	
	private Runnable movementXY_callback;
	private Runnable movementZ_callback;
	private Float rad = (3.1416f*2f)/12f;
	
	public Machine(Modeler modeler) {		
		this.modeler = modeler;
		stackMovements = new Movements(this.modeler);
	}
	
	public void setCurrentPosX(Float x) {
		currentPos.x = x;
		modeler.updateCurrentPos_GUI();
	}
	
	public Float getCurrentPosX() {
		return currentPos.x;
	}

	public void setCurrentPosY(Float y) {
		currentPos.y = y;
		modeler.updateCurrentPos_GUI();
	}
	
	public Float getCurrentPosY() {
		return currentPos.y;
	}

	public void setCurrentPosZ(Float z) {
		currentPosZ = z;
		modeler.updateCurrentPos_GUI();
	}
	
	public Float getCurrentPosZ() {
		return currentPosZ;
	}
	
	public void setPenDiameter(Float d) {
		penDiameterTH = d;		
		modeler.updateCurrentPos_GUI();
	}
	
	public Float getPenDiameter() {
		return currentPosZ;
	}
	
	public void stop() {
		if(timer != null) timer.stop();
		xSteps = 0;
		ySteps = 0;
		lastDifferenceX = 0f;
		lastDifferenceY = 0f;
		executingStackCanvasMovements = false;
	}
	
	
	public void stepsLeftCommand(int steps, int acumStepNumber) {
		String cmd = "$"+steps+"L"+acumStepNumber+"M$";
		System.out.println(cmd);
		modeler.mainUI.btnLeft.setBackground(Color.ORANGE);
		try {
			modeler.serial.sendSerial(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stepsRightCommand(int steps, int acumStepNumber) {
		String cmd = "$"+steps+"R"+acumStepNumber+"M$";
		System.out.println(cmd);
		modeler.mainUI.btnRight.setBackground(Color.ORANGE);
		try {
			modeler.serial.sendSerial(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stepsFrontCommand(int steps, int acumStepNumber) {
		String cmd = "$"+steps+"F"+acumStepNumber+"M$";
		System.out.println(cmd);
		modeler.mainUI.btnFront.setBackground(Color.ORANGE);
		try {
			modeler.serial.sendSerial(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stepsBackCommand(int steps, int acumStepNumber) {
		String cmd = "$"+steps+"B"+acumStepNumber+"M$";
		System.out.println(cmd);
		modeler.mainUI.btnBack.setBackground(Color.ORANGE);
		try {
			modeler.serial.sendSerial(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stepsUpCommand(int steps, int acumStepNumber) {
		String cmd = "$"+steps+"U"+acumStepNumber+"M$";
		System.out.println(cmd);
		modeler.mainUI.btnUp.setBackground(Color.ORANGE);
		try {
			modeler.serial.sendSerial(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stepsDownCommand(int steps, int acumStepNumber) {
		String cmd = "$"+steps+"D"+acumStepNumber+"M$";
		System.out.println(cmd);
		modeler.mainUI.btnDown.setBackground(Color.ORANGE);
		try {
			modeler.serial.sendSerial(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public void makeMovementZ(final int upOrDown, Runnable c) {
		movementZ_callback = c;
			
		timer = new Timer (0, new ActionListener () { 
			public void actionPerformed(ActionEvent e) { 
				timer.stop();
				    	
		    	if(upOrDown == 0) {
		    		// set down
					if(!currentPosZ.equals(downPosZ)) {	
						int s;
		            	if(currentPosZ>downPosZ) {
				    		// down
		            		s = Math.round(currentPosZ-downPosZ);
		            		System.out.println("DOWN "+s+"...");
		            		stepsDownCommand(s, STEP_ACUM_MAX);
		            		setCurrentPosZ(currentPosZ-s);
		            	} else {
				    		// up
		            		s = Math.round(downPosZ-currentPosZ);
		            		System.out.println("UP "+s+"...");
				    		stepsUpCommand(s, STEP_ACUM_MAX);
				    		setCurrentPosZ(currentPosZ+s);
		            	}
		            	
		            	timer = new Timer (s*20, new ActionListener () { 
						    public void actionPerformed(ActionEvent e) { 
						    	timer.stop();
	
						    	movementZ_callback.run();
						    	//movementZ_callback = null;
						    } 
						});
						timer.start();
					} else {
						movementZ_callback.run();
				    	//movementZ_callback = null;
					}
				} else {
					// set up
					if(!currentPosZ.equals(upPosZ)) {	
						int s;
				    	if(currentPosZ>upPosZ) {				    		
				    		// down
		            		s = Math.round(currentPosZ-upPosZ);
		            		System.out.println("DOWN "+s+"...");
				    		stepsDownCommand(s, STEP_ACUM_MAX);
				    		setCurrentPosZ(currentPosZ-s);
				    	} else {
				    		// up
				    		s = Math.round(upPosZ-currentPosZ);
		            		System.out.println("UP "+s+"...");
				    		stepsUpCommand(s, STEP_ACUM_MAX);
				    		setCurrentPosZ(currentPosZ+s);
				    	}
				    	
				    	timer = new Timer (s*20, new ActionListener () { 
						    public void actionPerformed(ActionEvent e) { 
						    	timer.stop();

								movementZ_callback.run();
						    	//movementZ_callback = null;
						    } 
						});
						timer.start();
					} else {
						movementZ_callback.run();
				    	//movementZ_callback = null;
					}
				}
				 	
		    }
		});
		timer.start();
	}
	
	public void makeMovementXY(float x, float y, Runnable c) {
		movementXY_callback = c;
		
		targetPos = new Vector2f(x, y);
		modeler.mainUI.TargetPosX.setText((viewInMM)?Float.toString(targetPos.x*mm2th)+"mm":Float.toString(targetPos.x)+"th");
		modeler.mainUI.TargetPosY.setText((viewInMM)?Float.toString(targetPos.y*mm2th)+"mm":Float.toString(targetPos.y)+"th"); 

		/*// X
		Vector2f vvx = new Vector2f(currentPos);
		Vector2f vvx2 = new Vector2f(targetPos);
		vvx2.sub(vvx);
		Vector2f vxf = new Vector2f(vvx2.x,0f);
		
		Vector2f vvy = new Vector2f(currentPos);
		Vector2f vvy2 = new Vector2f(targetPos);
		vvy2.sub(vvy);
		Vector2f vyf = new Vector2f(0f,vvx2.y);
		
		float xLength = vxf.length();		
		System.out.println("- xLength "+xLength);
		float numStepsForDisplaceX = xLength/stepDistanceTHUnitX;
		xSteps = Math.round(numStepsForDisplaceX+lastDifferenceX);
		// Y
		float yLength = vyf.length();	
		System.out.println("- yLength "+yLength);
		float numStepsForDisplaceY = yLength/stepDistanceTHUnitY;
		ySteps = Math.round(numStepsForDisplaceY+lastDifferenceY);*/
		// X
		float xLength = Math.abs(targetPos.x-currentPos.x);		
		System.out.println("- xLength "+xLength);
		float numStepsForDisplaceX = xLength/stepDistanceTHUnitX;
		xSteps = Math.round(numStepsForDisplaceX+lastDifferenceX);
		// Y
		float yLength = Math.abs(targetPos.y-currentPos.y);		
		System.out.println("- yLength "+yLength);
		float numStepsForDisplaceY = yLength/stepDistanceTHUnitY;
		ySteps = Math.round(numStepsForDisplaceY+lastDifferenceY);
		
		acumNumberX = STEP_ACUM_MAX;
		acumNumberY = STEP_ACUM_MAX;
		if(xLength > yLength) {
			float dd = yLength/xLength; 
			acumNumberY = Math.round(dd*STEP_ACUM_MAX);
		} else if(yLength > xLength) {
			float dd = xLength/yLength;
			acumNumberX = Math.round(dd*STEP_ACUM_MAX);
		}
		
		// X		
		lastDifferenceX = numStepsForDisplaceX-xSteps;
		
		timerTMPX = new Timer (200, new ActionListener () { 
		    public void actionPerformed(ActionEvent e) { 
		    	timerTMPX.stop();
		    	if(Math.abs(xSteps) > 1) {
					if(targetPos.x < currentPos.x) {
						stepsLeftCommand(Math.abs(xSteps), acumNumberX);
						setCurrentPosX(currentPos.x-(xSteps*stepDistanceTHUnitX));
					} else {
						stepsRightCommand(Math.abs(xSteps), acumNumberX);
						setCurrentPosX(currentPos.x+(xSteps*stepDistanceTHUnitX));
					} 	
					
		    	} else onSerialReceived("Y");	
			} 
		});	
		timerTMPX.start();
		
		// Y			
		lastDifferenceY = numStepsForDisplaceY-ySteps;
		
		timerTMPY = new Timer (400, new ActionListener () { 
		    public void actionPerformed(ActionEvent e) { 
		    	timerTMPY.stop();
		    	if(Math.abs(ySteps) > 1) {
			    	if(targetPos.y < currentPos.y) {
			    		stepsFrontCommand(Math.abs(ySteps), acumNumberY);
			    		setCurrentPosY(currentPos.y-(ySteps*stepDistanceTHUnitY));
			    	} else {
			    		stepsBackCommand(Math.abs(ySteps), acumNumberY);	
			    		setCurrentPosY(currentPos.y+(ySteps*stepDistanceTHUnitY));
			    	}	
			    	
		    	} else onSerialReceived("Y");
            } 
		});	
		timerTMPY.start(); 
	}
	
	public void onSerialReceived(String str) {
		if(str.equals("Y")) {
			modeler.mainUI.btnLeft.setBackground(Color.CYAN);
			modeler.mainUI.btnRight.setBackground(Color.CYAN);
			modeler.mainUI.btnFront.setBackground(Color.CYAN);
			modeler.mainUI.btnBack.setBackground(Color.CYAN);
			
			if(receivedOK == 0) {
				System.out.println("OK 1 received");
				
				receivedOK++;
			} else if(receivedOK == 1) {
				System.out.println("OK 2 received. Go next...");
								
				receivedOK = 0;
				movementXY_callback.run();
				//movementXY_callback = null;				
			}
		}
	} 
	
	
	
	
	
	
	// STACK OF MOVEMENTS
	public void execStackMovements() {
		executingStackCanvasMovements = true;	
		stackMovements.resetMovementStack();
		
		stackMovements.xValues = modeler.stackCanvasMovements.xValues;
		stackMovements.yValues = modeler.stackCanvasMovements.yValues;
		stackMovements.isStartTraceValues = modeler.stackCanvasMovements.isStartTraceValues;				
		currentStackId = 0;		
		receivedOK = 0;
		
		if(stackMovements.isStartTraceValues.get(0).equals("1")) {
			// target position is normal (1). Must be down now	
			makeMovementZ(0, new Thread(new Runnable() {
			    @Override
			    public void run() {
			    	makeMovementXY(stackMovements.xValues.get(0), stackMovements.yValues.get(0), new Thread(new Runnable() {
						@Override
					    public void run() {	
							nextStackMovement();	
						}
					}));	
			    }
			}));
		} else if(stackMovements.isStartTraceValues.get(0).equals("2") ||
				stackMovements.isStartTraceValues.get(0).equals("3")) {
			// target position is start (2) or drill hole (3). Must be up now	
			makeMovementZ(1, new Thread(new Runnable() {
			    @Override
			    public void run() {
			    	makeMovementXY(stackMovements.xValues.get(0), stackMovements.yValues.get(0), new Thread(new Runnable() {
						@Override
					    public void run() {	
							nextStackMovement();	
						}
					}));	
			    }
			}));
		}
	}
	
	public void nextStackMovement() {
		if(executingStackCanvasMovements) {
			if(stackMovements.getStackMovementSize() > 0) {
				
				if(stackMovements.isStartTraceValues.get(0).equals("2") ||
					stackMovements.isStartTraceValues.get(0).equals("1")) {	
					// current position is normal (1) or start (2)
					System.out.println("Start of target "+currentStackId);
										
					if(stackMovements.isStartTraceValues.get(0+1).equals("1")) {
						// target position is normal (1). Must be down now	
						makeMovementZ(0, new Thread(new Runnable() {
						    @Override
						    public void run() {
						    	makeMovementXY(stackMovements.xValues.get(0+1), stackMovements.yValues.get(0+1), new Thread(new Runnable() {
									@Override
								    public void run() {	
										shiftMovementFromStack();
										nextStackMovement(); // get new movement from stack
									}
								}));	 
						    }
						}));
					} else if(stackMovements.isStartTraceValues.get(0+1).equals("2") ||
							stackMovements.isStartTraceValues.get(0+1).equals("3")) {
						// target position is start (2) or drill hole (3). Must be up now	
						makeMovementZ(1, new Thread(new Runnable() {
						    @Override
						    public void run() {
						    	makeMovementXY(stackMovements.xValues.get(0+1), stackMovements.yValues.get(0+1), new Thread(new Runnable() {
									@Override
								    public void run() {	
										shiftMovementFromStack();
										nextStackMovement(); // get new movement from stack
									}
								}));	
						    }
						}));
					}				
				} else if(stackMovements.isStartTraceValues.get(0).equals("3")) {
					// current position is drill hole (3)
					System.out.println("Start of target "+currentStackId);
					
					Float currentDrill = 0f;
					Float rAx = (float) Math.cos(rad*currentDrill)*20f;
	    			Float rAy = (float) Math.sin(rad*currentDrill)*20f;
					makeMovementXY(	rAx, rAy, new Thread(new Runnable() {
						private Float currentDrill = 0f;
						
						@Override
					    public void run() {		
							makeMovementZ(0, new Thread(new Runnable() {
							    @Override
							    public void run() {
							    	nextDrillTarget();
							    }
							}));
						}
						
						private void nextDrillTarget() {
							if(currentDrill >= 12f) {
								makeMovementZ(1, new Thread(new Runnable() {
								    @Override
								    public void run() {
										shiftMovementFromStack();
										nextStackMovement(); // get new movement from stack
								    }
								}));
							} else {
								Float rBx = (float) Math.cos(rad*(currentDrill+1f))*20f;
					    		Float rBy = (float) Math.sin(rad*(currentDrill+1f))*20f;
								makeMovementXY(	(stackMovements.xValues.get(0)+rBx), (stackMovements.yValues.get(0)+rBy), new Thread(new Runnable() {
									@Override
								    public void run() {	
										currentDrill += 1f;
										nextDrillTarget();																		
									}
								}));
							}
						}
					}));
				} else {
					shiftMovementFromStack();
					nextStackMovement();
				}
				
			} else { // no more movements
				executingStackCanvasMovements = false;
				makeMovementZ(1, new Thread(new Runnable() {
				    @Override
				    public void run() {
				    	
				    }
				}));
			}
			
		}
	}
	
	public void shiftMovementFromStack() {
		ArrayList<Float> xValuesTMP = new ArrayList<Float>();
		ArrayList<Float> yValuesTMP = new ArrayList<Float>();
		ArrayList<String> isStartTraceValuesTMP = new ArrayList<String>();
		for(int n=1; n < stackMovements.getStackMovementSize(); n++) {
			xValuesTMP.add(stackMovements.xValues.get(n));
			yValuesTMP.add(stackMovements.yValues.get(n));
			isStartTraceValuesTMP.add(stackMovements.isStartTraceValues.get(n));
		}		
		stackMovements.xValues = xValuesTMP;
		stackMovements.yValues = yValuesTMP;
		stackMovements.isStartTraceValues = isStartTraceValuesTMP;
		currentStackId++;
		modeler.updateUIMovementList();
	}
}
