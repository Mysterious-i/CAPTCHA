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
 * The class <code>USLocalizer</code> can preform ultrasonic localization using
 * the ultra sonic sensor with falling and rising edge techniques. This will
 * make the robot recognize which corner he is and move himself so that he is at
 * the first corner of the grid lines at an angle of 0 facing along an axis.
 * 
 * An instance of this class holds an instance of the <code>Navigation</code>
 * class, <code>Odometer</code> class. and <code>TwoWheeledRobot</code> class.
 * It also holds the <code>double</code> speeds at which the robot will rotate
 * and travel while it is doing it's localization. It will also hold the
 * constants of localiation d and k, as well as a counter to keep track of how
 * many readings were done.
 * 
 * @author Alessandro Parisi
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

	public Localizer(UltrasonicSensor usLeft, UltrasonicSensor usRight,
			Odometer odo, ColorSensor colorLeft, ColorSensor colorRight) {
		this.usLeft = usLeft;
		this.usRight = usRight;
		this.odo = odo;
		this.colorLeft = colorLeft;
		this.colorRight = colorRight;
		this.navigation = odo.getNavigation();
	}

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
		if (distance < 50) {
			Sound.buzz();
			facingWall = true;
			while (distance < 90) {
				distance = usLeft.getDistance();				
			}
		}
		int count = 0;

		while (count < 2) {
			distance = usLeft.getDistance();
			if (Math.abs(distance - 30) < 10) {
				int tempDistance = distance;

				angle[count] += odo.getAng();
				count++;
				Sound.beep();
				Delay.msDelay(400);
				while (distance < (tempDistance+10)) {
					Delay.msDelay(50);
					distance = usLeft.getDistance();

				}
				angle[count] = odo.getAng();
				Sound.beep();
				count++;
			}
			Delay.msDelay(20);
			ambientRight += colorLeft.getRawLightValue();
			ambientLeft += colorLeft.getRawLightValue();
			lightCount++;

		}
		ambientRight /= lightCount;
		ambientLeft /= lightCount;
		navigation.setSpeeds(0, 0);
		navigation.turnTo((facingWall ? 180 : 0) + (angle[0] + angle[1]) / 2,
				true);

	}

	public void lightLocalize() {
		odo.setAng(0);
		odo.setX(0);
		odo.setY(0);
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
		navigation.setSpeeds(SPEED, SPEED);
		double[] leftX = new double[2];
		double[] rightX = new double[2];
		int[] tone = new int[4];
		int indexLeft = 0, indexRight = 0;
		boolean leftLast = false;
		while ((indexLeft + indexRight) < 2) {
			if ((ambientLeft * 0.85 > colorLeft.getRawLightValue())
					&& (indexLeft < 1)) {
				leftX[indexLeft] = odo.getX();
				tone[indexLeft + indexRight] = 330;
				indexLeft++;
				Sound.playTone(330, 100);

			}

			if ((ambientRight * 0.85 > colorRight.getRawLightValue())
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
		navigation.moveForward(SPEED, -x - 7.3);
		double theta = (leftLast ? 180 : -90) + Math.toDegrees(dTheta);
		navigation.turnTo(theta, true);
		navigation.moveForward(SPEED, -y);
		navigation.turnTo(Math.toDegrees(dTheta) + 180, true);
		Delay.msDelay(200);
		odo.setAng(0);
		odo.setX(0);
		odo.setY(0);

	}

	public void localize() {
		USlocalize();
		lightLocalize();
	}

}