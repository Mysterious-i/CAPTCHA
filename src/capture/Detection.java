package capture;

import lejos.nxt.ColorSensor;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.comm.RConsole;
import lejos.util.Delay;

/**
 * The <code>Detection</code> class is used to manage the detection of all
 * objects in front of the light sensor. It will determine what color the object
 * and at what distance it is. With the color it can determine which type of
 * object it is (i.e. the opponents flag, your flag, or a wall block.
 * 
 * Each instance of the <code> Detection </code> class has a Color and
 * Ultrasonic sensor.
 * 
 * @see FlagCapture
 * 
 * @author Alessandro Parisi
 * @author Stefan T
 * @since 1.0
 * @version 1.0
 * 
 */
public class Detection {

	private ColorSensor colorSensor;
	private UltrasonicSensor usRight;
	private UltrasonicSensor usLeft;
	private int MAX_DISTANCE;
	private int[] pollingRight = new int[10];
	private int[] pollingLeft = new int[10];
	private int countLeft = 0, countRight = 0;

	/**
	 * Constructor that initiliazes the Detection object which sorts the color
	 * and ultrasonic sensors as well as sets the floodlight of the color sensor
	 * on.
	 * 
	 * @param colorSensor
	 *            A color sensor used for the robot to differentiate objects by
	 *            their color
	 * @param usRight
	 *            The left ultrasonic sensor used for the robot to determine the
	 *            distance from the object
	 * @param usLeft
	 *            The right ultrasonic sensor used for the robot to determine the
	 *            distance from the object
	 * @param MAX_DISTANCE the maximum distance for the distance detection       
	 */

	public Detection(ColorSensor colorSensor, UltrasonicSensor usRight,
			UltrasonicSensor usLeft, int MAX_DISTANCE) {
		this.colorSensor = colorSensor;
		this.usRight = usRight;
		this.usLeft = usLeft;
		this.MAX_DISTANCE = MAX_DISTANCE;

		for (int i = 0; i < 10; i++) {
			pollingRight[i] = 255;
			pollingLeft[i] = 255;
		}
	}

	/**
	 * Returns the <code>int</code> number representing the red value of RBG
	 * value seen by the color sensor
	 * 
	 * @return the <code>int</code> number representing the red value of RBG
	 *         value seen by the color sensor
	 */
	public int getRed() {

		final ColorSensor.Color color = colorSensor.getColor();

		RConsole.println(Integer.toString(color.getRed()) + " Red\n");

		return color.getRed();
	}

	/**
	 * Returns the <code>int</code> number representing the green value of RBG
	 * value seen by the color sensor
	 * 
	 * @return the <code>int</code> number representing the green value of RBG
	 *         value seen by the color sensor
	 */
	public int getGreen() {

		final ColorSensor.Color color = colorSensor.getColor();

		RConsole.println(Integer.toString(color.getGreen()) + " Green\n");

		return color.getGreen();
	}

	/**
	 * Returns the <code>int</code> number representing the blue value of RBG
	 * value seen by the color sensor
	 * 
	 * @return the <code>int</code> number representing the blue value of RBG
	 *         value seen by the color sensor
	 */
	public int getBlue() {

		final ColorSensor.Color color = colorSensor.getColor();

		RConsole.println(Integer.toString(color.getBlue()) + " Blue\n");

		return color.getBlue();
	}

	/**
	 * Returns the <code>int</code> average distance read by the left ultrasonic sensor after 10 pings
	 * 
	 * @return the <code>int</code> average distance read by the left ultrasonic sensor after 10 pings
	 */
	public int getLeftDistance() {
		int average = getAverageDistance(usLeft, 10);
		Delay.msDelay(100);
		return average;
	}
	
	/**
	 * Returns the <code>int</code> average distance read by the right ultrasonic sensor after 10 pings
	 * 
	 * @return the <code>int</code> average distance read by the right ultrasonic sensor after 10 pings
	 */
	public int getRightDistance() {
		int average = getAverageDistance(usRight, 10);
		Delay.msDelay(100);
		return average;
	}
	/**
	 * Returns the <code>int</code> distance read by the left ultrasonic sensor from one ping
	 * 
	 * @return the <code>int</code> distance read by the left ultrasonic sensor from one ping
	 */
	public int getLeftDistanceOnce() {
		return usLeft.getDistance();
	}
	/**
	 * Returns the <code>int</code> distance read by the right ultrasonic sensor from one ping
	 * 
	 * @return the <code>int</code> distance read by the right ultrasonic sensor from one ping
	 */
	public int getRightDistanceOnce() {
		return usRight.getDistance();
	}
	
	/**
	 * Returns the <code>int</code> average distance read by the ultrasonic sensor after n readings
	 * 
	 * @param us the <code>UltrasonicSensor</code> that will be used to get the readings
	 * @param n the <code> int </code> number of readings in the average
	 * 
	 * @return the <code>int</code> average distance read by the ultrasonic sensor after n readings
	 */
	public int getAverageDistance(UltrasonicSensor us, int n) {
		int sum = 0;
		for(int i = 0; i < n; i++){
			sum += us.getDistance();
		}
		return sum/n;
	}
	/**
	 * Returns a <code>boolean</code> true if either sensor sees a wall in front
	 * of it
	 * 
	 * @return a <code>boolean</code> true if either sensor sees a wall in front
	 *         of it
	 */
	public boolean wallInFront() {
		return ((getRightDistance() < MAX_DISTANCE) || (getLeftDistance() < MAX_DISTANCE));
	}

	/**
	 * Returns a <code>boolean</code> true if both sensors sees a wall in front
	 * of both sensors
	 * 
	 * @return a <code>boolean</code> true if both sensors sees a wall in front
	 *         of both sensors
	 */
	public boolean wallInFrontOfBoth() {
		return ((getRightDistance() < MAX_DISTANCE) && (getLeftDistance() < MAX_DISTANCE));
	}

	/**
	 * Method that calculates the block type depending on the color of the
	 * object read by the color sensor.
	 * 
	 * @return the <code>String</code> name of the type of block that is
	 *         currently in front of the sensors
	 */
	public String getBlockType() {

		String s;

		// Get the RGB values and set them as ints.
		int red = getRed();
		int green = getGreen();
		int blue = getBlue();

		// Set the string s to different colored blocks depending on the values
		// of red, green and blue.
		if (red > 2.5 * blue && red > 2.5 * green)
			s = "red";
		else if (red > 2 * blue && green > 2 * blue)
			s = "yellow";
		else if (blue > 1.8 * red)
			s = "dark blue";
		else if (red > 1.4 * green && red > 1.4 * blue)
			s = "Wood Block";
		else if (red > 1.1 * blue)
			s = "white";
		else
			s = "light blue";

		return s;
	}

	/**
	 * Returns a number representing the different block types depending. 0 - Failed to detect the block 
	 * Recognizable Block, 1 - Light Blue, 2 - Red, 3 - Yellow, 4 - White, 5 - Dark Blue, 6 - Wooden
	 * @return the <code>int</code> number that represents the block
	 */
	public int getBlockNumber() {

		String s;
		s = getBlockType();

		if (s.equals("light blue"))
			return 1;
		else if (s.equals("red"))
			return 2;
		else if (s.equals("yellow"))
			return 3;
		else if (s.equals("white"))
			return 4;
		else if (s.equals("dark blue"))
			return 5;
		else if (s.equals("Wood Block"))
			return 6;
		return 0;
	}
}
