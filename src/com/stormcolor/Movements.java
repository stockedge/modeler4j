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
		for(int n=0; n < xValues.size(); n++) {
			idsProccessed.add(false);
		}
		ArrayList<Float> xValuesTMP = new ArrayList<Float>();
		ArrayList<Float> yValuesTMP = new ArrayList<Float>();
		ArrayList<String> isStartTraceValuesTMP = new ArrayList<String>();	// string 2 is start of trace
		
		
		Float x = 0f;
		Float y = 0f;
		while(true) {
			int nearId = 0;
			Float nearDist = 1000000f;
			for(int n=0; n < xValues.size(); n++) {
				if(isStartTraceValues.get(n).equals("2") && idsProccessed.get(n).equals(false)) {
					Float distTo = Math.abs(xValues.get(n)-x) + Math.abs(yValues.get(n)-y);
					if(distTo <= nearDist) {
						nearDist = distTo;
						nearId = n;
					}
				}
			}
			
			while(true) {
				if((nearId) < xValues.size()) {
					xValuesTMP.add(xValues.get(nearId));
					yValuesTMP.add(yValues.get(nearId));
					isStartTraceValuesTMP.add(isStartTraceValues.get(nearId));
					idsProccessed.set(nearId, true);
					x = xValues.get(nearId);
					y = yValues.get(nearId);
					nearId++;
					if((nearId) < xValues.size() && isStartTraceValues.get(nearId).equals("2")) break;
					
				} else break;
			}
			
			boolean allProcess = true;
			for(int n=0; n < idsProccessed.size(); n++) {
				if(idsProccessed.get(n).equals(false)) allProcess = false;
			}
			if(allProcess) break;
		}
		
		xValues = xValuesTMP;
		yValues = yValuesTMP;
		isStartTraceValues = isStartTraceValuesTMP;		
	}
}
