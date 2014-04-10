package capture;

import java.util.Stack;

/** The <code>PastPositions</code> class is used to keep track of
 * the past positions of the robot. It contains two <code>Stack</code>s
 * to keep past positions.
 */
public class PastPositions {


	Stack<Double> XPositions;
	Stack<Double> YPositions;

	/** A constructor that initliazes teh stack that keeps the past positions.
	 *
	 */
	public PastPositions(){
		XPositions = new Stack<Double>();
		YPositions = new Stack<Double>();
	}

	/** This method takes in two integers which are the x and y positions
	 * and puts them on their respective stacks.
	 *
	 * @param x the x position of the robot
	 * @param y the y position of the robot
	 */
	public void putPoint(double x, double y){
		XPositions.push(x);
		YPositions.push(y);
	}
	/** This method returns and removes the latest safe x position
	 *
	 * @return the <code>int</code> of the latest safe position in x
	 */
	public double getPointX(){
		return XPositions.pop();
	}
	/** This method returns and removes the latest safe y position
	 *
	 * @return the <code>int</code> of the latest safe position in y
	 */
	public double getPointY(){
		return YPositions.pop();
	}

}
