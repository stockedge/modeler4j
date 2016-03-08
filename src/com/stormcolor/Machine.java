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
	//	y fijamos 0 para el eje z (bajara a 0 cuando esciba y subirá a 80 cuando no escriba)
	
	// 6. StormModeler: File > Open y seleccionamos el fichero Gerber.
	
	// 7. StormModeler: Pulsar Run. Este se dirigirá automáticamente hacia el origen del primer trazo. (subirá 80 al comienzo al dirigirse hacia el origen de un trazo)
	// Una vez llegue bajará 80 pasos comenzando así la serie de trazados.
	
	
	// - Para calibrar la maquina se dispone en los PICs XY el MAX_DELAY/MIN_DELAY (cuando solo mueve un eje) y el CON_DELAY (cuando se mueven los 2 ejes a 45º)
	// - Luego en este codigo se fija las variables stepsForX,stepsForY,distanceCMwith_stepsForX,distanceCMwith_stepsForY con los resultados obtenidos
	// tras hacer un recorrido largo con ambos ejes.
	// - Luego para ajustar la velocidad de los ejes mutuamente (para poder dibujar una linea con angulo de 45 ambos a la misma velocidad)
	// se abre el archivo linea45grados de proteus (asegurarse que ocupe la minima diagonal de ambos ejes) y se ejecuta.
	// Debemos de ajustar CON_DELAY para que terminen el recorrido de 45 grados ambos a la vez. Para ello ajustamos de nuevo el CON_DELAY del eje
	// que llega antes, aumentando su valor y que viendo vaya terminando cada vez más tarde.
	
	// El firmware utilizado en este caso son los siguientes:
	// MINEBEA-17PM-K-041-P1F-XC8 PARA EL EJE Y
	// MITSUMI_M49SP-1-XC8 PARA EL EJE X
	// BIPOLAR-STORM-MODELER-EJEZ-XC8 PARA EL EJE Z
	// Solo hay que fijar las variables para X e Y. El Eje Z va directamente desde el paso 0 (parte más baja) al paso 80 (parte más alta)
	
	// El algoritmo implementado actualmente en este programa es para la realización de trazados con un rotulador a partir de un fichero gerber
	
	// public variables
	private final float stepsForX = 800f; // MITSUMI L-R
	private final float stepsForY = 525000f; // MINEBEA F-B
	private final float distanceCMwith_stepsForX = 13.55f;
	private final float distanceCMwith_stepsForY = 12.0f;
	
	private final int loosenessStepsByDirChangeX = 0;
	private final int loosenessStepsByDirChangeY = 3000;
	private String oldDirX = "LEFT";
	private String oldDirY = "FRONT";
	
	// private variables
	private final float stepDistanceMMUnitX = (distanceCMwith_stepsForX*10f)/stepsForX;
	private final float stepDistanceMMUnitY = (distanceCMwith_stepsForY*10f)/stepsForY;
	
	public final float mm2th = 0.0254f;
	public final float stepDistanceTHUnitX = stepDistanceMMUnitX/mm2th;
	public final float stepDistanceTHUnitY = stepDistanceMMUnitY/mm2th;
	
	public float penDiameterTH = 20f; 
		
	public Vector2f currentPos = new Vector2f(0f, 0f);
	public Vector2f currentPosTMP = new Vector2f(0f, 0f);
	public final Float upPosZ = 80f;
	public final Float downPosZ = 0f;
	public Float currentPosZ = downPosZ;
		
	private Vector2f targetPos = new Vector2f(0f, 0f);
	public boolean viewInMM = false;
	private Timer timer;
	private Timer timerTMPX;
	private Timer timerTMPY;
	private Timer timerTM;
	private float xLength;
	private float yLength;
	private int xSteps;
	private int ySteps;
	public int STEP_ACUM_MAX = 65535;
	private int acumNumberX = STEP_ACUM_MAX;
	private int acumNumberY = STEP_ACUM_MAX;
	private float offsetX = 0f;
	private float offsetY = 0f;
	
	public Movements stackMovements;
	public boolean executingStackCanvasMovements = false;
	public Integer currentStackId = 0;
	private int receivedFB = 0;
	private int receivedLR = 0;
	
	Runnable[] arrRunnables = new Runnable[30];
	private int arrRunnablesLength = 0;
	private Float rad = (3.1416f*2f)/12f;
	
	public Machine(Modeler modeler) {		
		this.modeler = modeler;
		stackMovements = new Movements();
	}
	
	public void setCurrentPosX(Float x) {
		currentPos.x = x;
		modeler.mainUI.updateCurrentPos_GUI();
	}
	
	public Float getCurrentPosX() {
		return currentPos.x;
	}

	public void setCurrentPosY(Float y) {
		currentPos.y = y;
		modeler.mainUI.updateCurrentPos_GUI();
	}
	
	public Float getCurrentPosY() {
		return currentPos.y;
	}

	public void setCurrentPosZ(Float z) {
		currentPosZ = z;
		modeler.mainUI.updateCurrentPos_GUI();
	}
	
	public Float getCurrentPosZ() {
		return currentPosZ;
	}
	
	public void setPenDiameter(Float d) {
		penDiameterTH = d;		
		modeler.mainUI.updateCurrentPos_GUI();
	}
	
	public Float getPenDiameter() {
		return currentPosZ;
	}
	
	public void stop() {
		if(timer != null) timer.stop();
		xSteps = 0;
		ySteps = 0;
		offsetX = 0f;
		offsetY = 0f;
		executingStackCanvasMovements = false;
	}
	
	
	private void sendSerial(String cmd) {
		try {
			modeler.serial.sendSerial(cmd);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void stepsLeftCommand(int steps, int acumStepNumber) {
		modeler.mainUI.btnLeft.setBackground(Color.ORANGE);
		
		if(oldDirX.equals("RIGHT"))
			steps += loosenessStepsByDirChangeX;
		oldDirX = "LEFT";
				
		String cmd = "^"+steps+"L"+acumStepNumber+"M$";
		System.out.println("SEND LEFT... "+cmd);		
		sendSerial(cmd);
	}
	
	public void stepsRightCommand(int steps, int acumStepNumber) {
		modeler.mainUI.btnRight.setBackground(Color.ORANGE);
		
		if(oldDirX.equals("LEFT"))
			steps += loosenessStepsByDirChangeX;
		oldDirX = "RIGHT";
		
		String cmd = "^"+steps+"R"+acumStepNumber+"M$";
		System.out.println("SEND RIGHT... "+cmd);		
		sendSerial(cmd);
	}
	
	public void stepsFrontCommand(int steps, int acumStepNumber) {
		modeler.mainUI.btnFront.setBackground(Color.ORANGE);
		
		if(oldDirY.equals("BACK"))
			steps += loosenessStepsByDirChangeY;
		oldDirY = "FRONT";
		
		String cmd = "^"+steps+"F"+acumStepNumber+"M$";
		System.out.println("SEND FRONT... "+cmd);		
		sendSerial(cmd);
	}
	
	public void stepsBackCommand(int steps, int acumStepNumber) {
		modeler.mainUI.btnBack.setBackground(Color.ORANGE);
		
		if(oldDirY.equals("FRONT"))
			steps += loosenessStepsByDirChangeY;
		oldDirY = "BACK";
		
		String cmd = "^"+steps+"B"+acumStepNumber+"M$";
		System.out.println("SEND BACK... "+cmd);		
		sendSerial(cmd);
	}
	
	public void stepsUpCommand(int steps, int acumStepNumber) {
		modeler.mainUI.btnUp.setBackground(Color.ORANGE);
		
		String cmd = "^"+steps+"U"+acumStepNumber+"M$";
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►");
		System.out.println("►►►►►►►►►►►►►►►►►►►►►►►► UP ►►►►►►►►►►►►►►►►►►►►►►► "+cmd);
		System.out.println("►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►");
		System.out.println("");
		sendSerial(cmd);
	}
	
	public void stepsDownCommand(int steps, int acumStepNumber) {
		modeler.mainUI.btnDown.setBackground(Color.ORANGE);
		
		String cmd = "^"+steps+"D"+acumStepNumber+"M$";
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►");
		System.out.println("►►►►►►►►►►►►►►►►►►►►►►►► DOWN ►►►►►►►►►►►►►►►►►►►►►►► "+cmd);
		System.out.println("►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►►");
		System.out.println("");
		sendSerial(cmd);
	}
		
	public void makeMovementZ(final int upOrDown, Runnable runnable) {
		addRunnable(runnable);
			
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
		            		stepsDownCommand(s, STEP_ACUM_MAX-1);
		            		setCurrentPosZ(currentPosZ-s);
		            	} else {
				    		// up
		            		s = Math.round(downPosZ-currentPosZ);
				    		stepsUpCommand(s, STEP_ACUM_MAX-1);
				    		setCurrentPosZ(currentPosZ+s);
		            	}
		            	
		            	timer = new Timer (s*60, new ActionListener () { 
						    public void actionPerformed(ActionEvent e) { 
						    	timer.stop();
	
						    	runRunnables();
						    } 
						});
						timer.start();
					} else {
						runRunnables();
					}
				} else {
					// set up
					if(!currentPosZ.equals(upPosZ)) {	
						int s;
				    	if(currentPosZ>upPosZ) {				    		
				    		// down
		            		s = Math.round(currentPosZ-upPosZ);
				    		stepsDownCommand(s, STEP_ACUM_MAX-1);
				    		setCurrentPosZ(currentPosZ-s);
				    	} else {
				    		// up
				    		s = Math.round(upPosZ-currentPosZ);
				    		stepsUpCommand(s, STEP_ACUM_MAX-1);
				    		setCurrentPosZ(currentPosZ+s);
				    	}
				    	
				    	timer = new Timer (s*60, new ActionListener () { 
						    public void actionPerformed(ActionEvent e) { 
						    	timer.stop();

						    	runRunnables();
						    } 
						});
						timer.start();
					} else {
						runRunnables();
					}
				}
				 	
		    }
		});
		timer.start();
	}
	
	public void makeMovementXY(float x, float y, Runnable runnable) {
		addRunnable(runnable);
		
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
		xSteps = Math.round(numStepsForDisplaceX+offsetX);
		// Y
		float yLength = vyf.length();	
		System.out.println("- yLength "+yLength);
		float numStepsForDisplaceY = yLength/stepDistanceTHUnitY;
		ySteps = Math.round(numStepsForDisplaceY+offsetY);*/
		
		
		// X
		Vector2f vX = new Vector2f(targetPos.x, 0f);
		vX.sub(new Vector2f(currentPos.x, 0f));
		xLength = vX.length();
		float numStepsForDisplaceX = xLength/stepDistanceTHUnitX;
		float numStepsForDisplaceX_Offset = numStepsForDisplaceX;
		
		// Y
		Vector2f vY = new Vector2f(0f, targetPos.y);
		vY.sub(new Vector2f(0f, currentPos.y));
		yLength = vY.length();	
		float numStepsForDisplaceY = yLength/stepDistanceTHUnitY;		
		float numStepsForDisplaceY_Offset = numStepsForDisplaceY;
				
		
		System.out.println("/////////////////////////////////////// Displacing XY "+currentStackId+" /////////////////////////////////////");
		System.out.println(currentPos.x+", "+currentPos.y+" -> "+targetPos.x+", "+targetPos.y);
		System.out.println("Distance: "+xLength+" th, "+yLength+" th");
		System.out.println("Dist Steps: "+numStepsForDisplaceX+", "+numStepsForDisplaceY);		
		System.out.println("Offset: "+offsetX+", "+offsetY);
		
		
		// X
		String dirXstr = "";
		if(xLength > 0.0f) {			
			if(targetPos.x < currentPos.x) {							
				dirXstr = "Left";
				numStepsForDisplaceX_Offset -= offsetX;	
			} else {
				dirXstr = "Right";
				numStepsForDisplaceX_Offset += offsetX;	
			} 				
			System.out.println("+Off Steps X: "+numStepsForDisplaceX_Offset);
			
			xSteps = Math.abs(Math.round(numStepsForDisplaceX_Offset));
			offsetX = (numStepsForDisplaceX_Offset-xSteps);					
		} else {			
			xSteps = 0;	
		}
		
		
		
		// Y
		String dirYstr = "";
		if(yLength > 0.0f) {			
			if(targetPos.y < currentPos.y) {				
				dirYstr = "Front";
				numStepsForDisplaceY_Offset -= offsetY;
	    	} else {	    		
	    		dirYstr = "Back";
	    		numStepsForDisplaceY_Offset += offsetY;
	    	}				
			System.out.println("+Off Steps Y: "+numStepsForDisplaceY_Offset);
			
			ySteps = Math.abs(Math.round(numStepsForDisplaceY_Offset));
			offsetY = (numStepsForDisplaceY_Offset-ySteps);
		} else {			
			ySteps = 0;		
		}
		
		
		System.out.println("Real Steps: "+xSteps+" "+dirXstr+", "+ySteps+" "+dirYstr);	
		System.out.println("New Offset: "+offsetX+", "+offsetY);
		System.out.println("///////////////////////////////////////");
		
		
		// ACUMS
		acumNumberX = STEP_ACUM_MAX;
		acumNumberY = STEP_ACUM_MAX;
		if(xLength > yLength) {
			float dd = yLength/xLength; 
			acumNumberY = Math.round(dd*STEP_ACUM_MAX);
		} else if(yLength > xLength) {
			float dd = xLength/yLength;
			acumNumberX = Math.round(dd*STEP_ACUM_MAX);
		}
		if(xSteps > 0 && acumNumberY == STEP_ACUM_MAX)
			acumNumberY--;
		if(ySteps > 0 && acumNumberX == STEP_ACUM_MAX)
			acumNumberX--;
		
		
		
		
		// X				
		timerTMPX = new Timer (200, new ActionListener () { 
		    public void actionPerformed(ActionEvent e) { 
		    	timerTMPX.stop();
		    	
		    	currentPosTMP.x = targetPos.x;
		    	
		    	if(xSteps > 0) {
					if(targetPos.x < currentPos.x) {
						stepsLeftCommand(Math.abs(xSteps), acumNumberX);				
					} else {
						stepsRightCommand(Math.abs(xSteps), acumNumberX);
					} 						
		    	} else {		    		
		    		onSerialReceived("H");	
		    	}
			} 
		});	
		timerTMPX.start();
		
		// Y			
		
		
		timerTMPY = new Timer (400, new ActionListener () { 
		    public void actionPerformed(ActionEvent e) { 
		    	timerTMPY.stop();
		    	
		    	currentPosTMP.y = targetPos.y;
		    	
		    	if(ySteps > 0) {
			    	if(targetPos.y < currentPos.y) {
			    		stepsFrontCommand(Math.abs(ySteps), acumNumberY);
			    	} else {
			    		stepsBackCommand(Math.abs(ySteps), acumNumberY);
			    	}	
			    	
		    	} else {		    		
		    		onSerialReceived("Y");
		    	}
            } 
		});	
		timerTMPY.start(); 
	}
	
	public void onSerialReceived(String str) {
		if(str.equals("Y") || str.equals("H")) {
			String st;
			
			if(str.equals("Y")) {
				st = "Front-Back OK";
				System.out.println(st);
				
				modeler.mainUI.btnFront.setBackground(Color.CYAN);
				modeler.mainUI.btnBack.setBackground(Color.CYAN);
				
				receivedFB = 1;
			} else {
				st = "Left-Right OK";
				System.out.println(st);
				
				modeler.mainUI.btnLeft.setBackground(Color.CYAN);
				modeler.mainUI.btnRight.setBackground(Color.CYAN);
				
				receivedLR = 1;
			}
			
						
			if(receivedFB == 1 && receivedLR == 1) {
				System.out.println("Next...");
				System.out.println();
				
				receivedFB = 0;
				receivedLR = 0;
								
				setCurrentPosX(currentPosTMP.x);
				setCurrentPosY(currentPosTMP.y);
				
				runRunnables();
			}
		}
	} 
	
	public void addRunnable(Runnable runnable) {
		arrRunnables[arrRunnablesLength] = runnable;
		arrRunnablesLength++;
		//System.out.println(arrRunnablesLength+" in stack");
	}
	
	public void runRunnables() {
		timerTM = new Timer (10, new ActionListener () { 
		    public void actionPerformed(ActionEvent e) { 
		    	timerTM.stop();
		    	
		    	arrRunnables[0].run();
				
				
				Runnable arrRunnablesTMP[] = new Runnable[30];
				for(int n=1; n < arrRunnablesTMP.length; n++) {
					arrRunnablesTMP[n-1] = arrRunnables[n];
				}
				arrRunnables = arrRunnablesTMP;
				arrRunnablesLength--;
            } 
		});	
		timerTM.start();
	}
	
	
	public void setCurrentStackId(int id) {
		currentStackId = id;
	}
	
	public void procXY(final int id, final Runnable runnable) {
		if(stackMovements.isStartTraceValues.get(id).equals("1")) {
			// target position is normal (1). Must be down now	
			makeMovementZ(0, new Thread(new Runnable() {
			    @Override
			    public void run() {
			    	makeMovementXY(stackMovements.xValues.get(id), stackMovements.yValues.get(id), runnable);	
			    }
			}));
		} else if(stackMovements.isStartTraceValues.get(id).equals("2") ||
				stackMovements.isStartTraceValues.get(id).equals("3")) {
			// target position is start (2) or drill hole (3). Must be up now	
			makeMovementZ(1, new Thread(new Runnable() {
			    @Override
			    public void run() {
			    	makeMovementXY(currentPos.x, stackMovements.yValues.get(id), new Thread(new Runnable() {
						@Override
					    public void run() {	

							makeMovementXY(stackMovements.xValues.get(id), currentPos.y, runnable);
							
						}
					}));	
			    }
			}));
		}
	}
	
	
	
	public void nextStackMovement() {
		if(executingStackCanvasMovements) {
			if(stackMovements.getStackMovementSize() > 0) {
				
				if(stackMovements.isStartTraceValues.get(currentStackId).equals("2") ||
					stackMovements.isStartTraceValues.get(currentStackId).equals("1")) {	
					// current position is normal (1) or start (2)
					//System.out.println("Start of target "+currentStackId);
					
					procXY(currentStackId+1, new Thread(new Runnable() {
						@Override
					    public void run() {	
							shiftMovementFromStack();
							nextStackMovement(); // get new movement from stack
						}
					}));
				} else if(stackMovements.isStartTraceValues.get(currentStackId).equals("3")) {
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
								makeMovementXY(	(stackMovements.xValues.get(currentStackId)+rBx), (stackMovements.yValues.get(currentStackId)+rBy), new Thread(new Runnable() {
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
		currentStackId++;
		modeler.mainUI.updateUIMovementList();
		modeler.mainUI.execStartId.setText(currentStackId.toString());
		System.out.println("");
		System.out.println("");
		System.out.println("");
		System.out.println("");
	}
	
	public void execStackMovements() {
		System.out.println("Executing stack...");
		
		executingStackCanvasMovements = true;	
		stackMovements.resetMovementStack();
		
		stackMovements.xValues = modeler.mainUI.stackCanvasMovements.xValues;
		stackMovements.yValues = modeler.mainUI.stackCanvasMovements.yValues;
		stackMovements.isStartTraceValues = modeler.mainUI.stackCanvasMovements.isStartTraceValues;				
		//currentStackId = 0;		
		receivedFB = 0;
		receivedLR = 0;
		
		procXY(currentStackId, new Thread(new Runnable() {
			@Override
		    public void run() {	
				nextStackMovement();	
			}
		}));
	}
}
