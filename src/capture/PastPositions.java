package capture;

import java.util.Stack;

public class PastPositions {

	
	Stack<Double> XPositions;
	Stack<Double> YPositions;
	
	public PastPositions(){
		XPositions = new Stack<Double>();
		YPositions = new Stack<Double>();
	}
	
	public void putPoint(double x, double y){
		XPositions.push(x);
		YPositions.push(y);
	}
	public double getPointX(){
		return XPositions.pop();
	}
	public double getPointY(){
		return YPositions.pop();
	}
	
}
