package localize;

import traveling.Navigation;
import traveling.Odometer;
import lejos.nxt.ColorSensor;
import lejos.nxt.LCD;
import lejos.nxt.Motor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.util.Delay;

/**
 * The class <code>Localizer</code> can perform full localization using
 * the two instances of the <code>UltrasonicSensor</code> class and two
 * instances of the <code>ColorSensor</code> class. It leaves the robot 
 * oriented at 0 degrees with a wall to the right and in the back of it
 * and the first gird line corner. It works for when the robot is in a 
 * corner with a perfect right angle.
 * 
 * An instance of this class holds ambient colors of light and measures a relative 
 * difference to find when it detects a grid line. It also holds an indstace of the class
 * <code>Odometer</code> and <code>Navigation</code>. In addition it has an <code>int</code>
 * speed at which it travels at.
 * many readings were done.
 * 
 * @author Bei Chen Liu
 * @version 1.0
 * @since 1.0
 */

public class Localizer {
	private UltrasonicSensor usLeft;
	private UltrasonicSensor usRight;
	private Odometer odo;
	private ColorSensor colorLeft;
	private ColorSensor colorRight;
	private Navigation navigation;
	private int ambientLeft = 0, ambientRight = 0;
	private static final int SPEED = 150;

	/**
	 * The constructor of the <code>Localizer</code> class takes as parameter 
	 * the two instances of the <code>UltrasonicSensor</code> class and two
	 * instances of the <code>ColorSensor</code> class. It also takes in the
	 * <code>Odometer</code> that is being used. It then sets its attributes 
	 * to the respective parameters.
	 * @param usLeft the left <code>UltrasonicSensor</code> that was is used in the localization
	 * @param usRight the right <code>UltrasonicSensor</code> that was is used in the localization
	 * @param odo the <code>Odometer</code> that is used for the robot
	 * @param colorLeft the left <code>ColorSensor</code> of the robot
	 * @param colorRight the right <code>ColorSensor</code> of the robot
	 */
	public Localizer(UltrasonicSensor usLeft, UltrasonicSensor usRight,
			Odometer odo, ColorSensor colorLeft, ColorSensor colorRight) {
		this.usLeft = usLeft;
		this.usRight = usRight;
		this.odo = odo;
		this.colorLeft = colorLeft;
		this.colorRight = colorRight;
		this.navigation = odo.getNavigation();
	}

	/**
	 * This method uses the <code>UltrasonicSensor</code> to locate walls 
	 * and turns to the angle that leaves it facing the corner of the two walls.
	 */
	public void USlocalize() {

		colorLeft.setFloodlight(true);
		colorRight.setFloodlight(true);

		boolean facingWall = false;
		int distance;
		int i = 0;
		double angle[] = { 0, 0 };
		int lightCount = 0;
		usLeft.continuous();
		Delay.msDelay(1000);
		navigation.setSpeeds(SPEED, -SPEED);
		distance = usLeft.getDistance();

		// If the robot is facing a wall, rotate when it's not facing the wall	
		if (distance < 50) {
			Sound.buzz();   
			facingWall = true;
			while (distance < 90) {
				distance = usLeft.getDistance();				
			}
		}
		int count = 0;

		//try to find the falling edge of the wall, record the data for angle
		while (count < 2) {
			distance = usLeft.getDistance();
			if (Math.abs(distance - 30) < 10) {
				int tempDistance = distance;

				angle[count] += odo.getAng();
				count++;
				Sound.beep();
				Delay.msDelay(2000);
				while (distance < (tempDistance+10)) {
					Delay.msDelay(50);
					distance = usLeft.getDistance();

				}
				angle[count] = odo.getAng();
				Sound.beep();
				count++;
			}
			//pull the light data for later use
			Delay.msDelay(20);
			ambientRight += colorLeft.getRawLightValue();
			ambientLeft += colorLeft.getRawLightValue();
			lightCount++;

		}
		//prepare for light localization
		ambientRight /= lightCount;
		ambientLeft /= lightCount;
		navigation.setSpeeds(0, 0);
		navigation.turnTo((facingWall ? 180 : 0) + (angle[0] + angle[1]) / 2,
				true);

	}

	/**
	 * This method uses both <code>ColorSensors</code> to detect grid lines and
	 * then turns to the orientation where there is a wall to its left and bottom and
	 * brings the robot to the first grid line encounter.
	 */
	public void lightLocalize() {
		//reset the odometer to simplify the calculation
		odo.setAng(0);
		odo.setX(0);
		odo.setY(0);
		//get ambient light data if it isn't generated by the ultrasonic localiztion
		if (ambientLeft == 0 || ambientRight == 0) {
			for (int i = 0; i < 20; i++) {
				ambientLeft += colorLeft.getRawLightValue();
				ambientRight += colorRight.getRawLightValue();

			}
			ambientLeft /= 20;
			ambientRight /= 20;

		}
		LCD.drawInt(ambientLeft, 0, 2);
		LCD.drawInt(ambientRight, 0, 3);

		//move backward, declare the need variable
		navigation.setSpeeds(SPEED, SPEED);
		double[] leftX = new double[2];
		double[] rightX = new double[2];
		int[] tone = new int[4];
		int indexLeft = 0, indexRight = 0;
		boolean leftLast = false;

		//sensing the grid and record distance
		while ((indexLeft + indexRight) < 2) {
			if ((ambientLeft * 0.9 > colorLeft.getRawLightValue())
					&& (indexLeft < 1)) {
				leftX[indexLeft] = odo.getX();
				tone[indexLeft + indexRight] = 330;
				indexLeft++;
				Sound.playTone(330, 100);

			}

			if ((ambientRight * 0.9 > colorRight.getRawLightValue())
					&& indexRight < 1) {
				rightX[indexRight] = odo.getX();
				tone[indexLeft + indexRight] = 261;
				indexRight++;
				Sound.playTone(261, 100);
			}
		}
		Delay.msDelay(400);
		while ((indexLeft + indexRight) < 3) {
			if ((ambientLeft * 0.85 > colorLeft.getRawLightValue())
					&& (indexLeft < 2)) {
				leftX[indexLeft] = odo.getX();
				tone[indexLeft + indexRight] = 392;
				leftLast = true;
				indexLeft++;
				Sound.playTone(392, 100);
			}

			if ((ambientRight * 0.85 > colorRight.getRawLightValue())
					&& indexRight < 2) {
				rightX[indexRight] = odo.getX();
				tone[indexLeft + indexRight] = 523;
				leftLast = false;
				indexRight++;
				Sound.playTone(523, 100);
			}
		}

		navigation.setSpeeds(0, 0);

		double distanceFromX;
		double dTheta;
		double x;
		double y;
		//calculate the angle and distance from the data recorded 
		if (leftLast) {
			distanceFromX = -(leftX[1] - rightX[0]) / 2;
			dTheta = -Math.atan2(7.3, distanceFromX);
			x = leftX[1] + rightX[0];
			y = (7.3 - (leftX[1] - leftX[0]) * Math.sin(2 * dTheta) / 2)
					/ (Math.sin(dTheta));
		} else {

			distanceFromX = -(rightX[1] - leftX[0]) / 2;
			dTheta = -Math.atan2(distanceFromX, 7.3);
			x = leftX[0] + rightX[1];
			y = (7.3 - (rightX[1] - rightX[0]) * Math.sin(2 * dTheta) / 2)
					/ (Math.sin(dTheta));
		}

		x = odo.getX() - x / 2;
		navigation.setSpeeds(0, 0);

		//move to 0,0 point and turn to correct angle
		navigation.moveForward(SPEED, -x - 7.3);
		double theta = (leftLast ? 180 : -90) + Math.toDegrees(dTheta);
		navigation.turnTo(theta, true);
		navigation.moveForward(SPEED, -y);
		navigation.turnTo(Math.toDegrees(dTheta) + 180, true);
		Delay.msDelay(200);

		//reset the odometer and the robot is localized
		odo.setAng(0);
		odo.setX(0);
		odo.setY(0);

	}

	/**
	 * This method makes the robot localize using both ultrasonic and light
	 * localization
	 */
	public void localize() {
		USlocalize();
		lightLocalize();
	}

}