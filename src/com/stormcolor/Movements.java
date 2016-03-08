package src.com.stormcolor;

import java.util.ArrayList;

public class Movements {	
	public ArrayList<Float> xValues = new ArrayList<Float>();
	public ArrayList<Float> yValues = new ArrayList<Float>();
	public ArrayList<String> isStartTraceValues = new ArrayList<String>();	// string 2 is start of trace
	
	public Movements() {		
		
	}
	
	public void addMovementToStack(Float xVal, Float yVal, String isStartTraceValues) { 
		xValues.add(xVal);
		yValues.add(yVal);
		this.isStartTraceValues.add(isStartTraceValues);
	}
	
	public int getStackMovementSize() {
		return xValues.size();
	}
	
	public void resetMovementStack() {
		xValues = new ArrayList<Float>();
		yValues = new ArrayList<Float>();
		isStartTraceValues = new ArrayList<String>();
	}
	
	public void optimizeStack() {
		ArrayList<Boolean> idsProccessed = new ArrayList<Boolean>();
		int startsCount = 0;
		int startsProccessed = 0;
		
		for(int n=0; n < xValues.size(); n++) {
			idsProccessed.add(false);
			if(isStartTraceValues.get(n).equals("2"))
				startsCount++;
		}
		ArrayList<Float> xValuesTMP = new ArrayList<Float>();
		ArrayList<Float> yValuesTMP = new ArrayList<Float>();
		ArrayList<String> isStartTraceValuesTMP = new ArrayList<String>();	// string 2 is start of trace
		
		
		Float x = 0f;
		Float y = 0f;
		while(true) {
			int currentId = 0;
			Float nearDist = 1000000f;
			for(int n=0; n < xValues.size(); n++) {
				if(isStartTraceValues.get(n).equals("2") && idsProccessed.get(n).equals(false)) {
					Float distTo = Math.abs(xValues.get(n)-x) + Math.abs(yValues.get(n)-y);
					if(distTo <= nearDist) {
						nearDist = distTo;
						currentId = n;
					}
				}
			}
			startsProccessed++;
			
			while(true) {
				if((currentId) < xValues.size()) {
					xValuesTMP.add(xValues.get(currentId));
					yValuesTMP.add(yValues.get(currentId));
					
					idsProccessed.set(currentId, true);					
					x = xValues.get(currentId);
					y = yValues.get(currentId);
					if(currentId > 0 && (x == xValues.get(currentId-1) && y == yValues.get(currentId-1))) {
						isStartTraceValuesTMP.add("1");
					} else {
						isStartTraceValuesTMP.add(isStartTraceValues.get(currentId));
					}
					currentId++;
					
					
					if(currentId < xValues.size()) {
						if( (isStartTraceValues.get(currentId).equals("2") && (xValues.get(currentId)!=x || yValues.get(currentId)!=y)) ||
							isStartTraceValues.get(currentId).equals("3") ) {							
							break;
						} else {
							for(int nb=0; nb < xValues.size(); nb++) {
								if( (isStartTraceValues.get(nb).equals("2") || isStartTraceValues.get(nb).equals("1")) && (xValues.get(nb)==x && yValues.get(nb)==y) && idsProccessed.get(nb).equals(false)) {	
									currentId = nb;
									break; // break for
								}
							}
						}
					} else {
						break;
					}
				} else {
					break;
				}
			}
			
			if(startsProccessed == startsCount) break;
		}
		
		xValues = xValuesTMP;
		yValues = yValuesTMP;
		isStartTraceValues = isStartTraceValuesTMP;		
	}
}
