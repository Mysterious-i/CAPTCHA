package maincontrol;
import java.io.IOException;

import capture.FlagCapturer;
import traveling.Odometer;
import lejos.nxt.*;
import lejos.nxt.comm.RS485;
import lejos.nxt.remote.RemoteMotor;
import lejos.nxt.remote.RemoteNXT;
import lejos.util.Delay;
import lejos.util.Timer;
import localize.USLocalizer;
import localize.USLocalizer.LocalizationType;
import maincontrol.NXTLCPRespond.Responder;

/**
 * The <code>Main</code> class structures the different stages that need to be preformed
 * for the robot to succesfully capture a flag. This includes setting up the 
 * <code>Odometer</code>, preforming <code>USLocalization</code>, and then preforming
 * <code>FlagCapturer</code>.
 * 
 * An instance of this class will hold the coordinates that surround the area where the flag will be
 * and the coordinates that surround the area where the flag needs to be placed. 
 * 
 * This class directly communicates with the instance of the <code>LCDInfo</code> class for
 * printing on the NXT brick screen.
 * 
 * @author  Alessandro Parisi
 * @version 1.0
 * @since   1.0
 *
 */
public class Main {

    private static int[] FinalPosCoords = {0};

    private static int[] FlagPosCoords = {0};
    
    private static Odometer odometer;
    private static USLocalizer usl;
    private static LCDInfo lcd;
    
    /**
     * This main method will initialize the instance of the classes <code>Odometer</code>, 
     * <code>UltrasonicSensor</code>, and <code>ColorSensor</code> which will be used in the program.
     * 
     * It waits for the user to clicks the button left on the NXT brick and then starts.
     * It exists if they clikc the exit button on the NXT brick.
     * 
     * @param args
     */
	public static void main(String[] args) {
		
		int buttonChoice;
		//Create the slave NXT
		RemoteNXT slaveNXT = null;
		try {
			slaveNXT = new RemoteNXT("NXT", RS485.getConnector());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//Initialize the slave's ports
		//TODO the Color sensor is undefined for slave so make sure ports are good
		ColorSensor detectionSensor = new ColorSensor(SensorPort.S4);
		UltrasonicSensor usRight = new UltrasonicSensor(slaveNXT.S2);
		UltrasonicSensor usLeft = new UltrasonicSensor(slaveNXT.S3);
		RemoteMotor grabberRight = slaveNXT.A;
		RemoteMotor grabberLeft = slaveNXT.B;
		
		//Initilize the master's ports
		LightSensor odometerCorrectionRight = new LightSensor(SensorPort.S1);
		LightSensor odometerCorrectionLeft = new LightSensor(SensorPort.S2);
		
		// setup the odometer, display, and ultrasonic and light sensors

		odometer = new Odometer(true, odometerCorrectionRight, odometerCorrectionLeft);
		FlagCapturer flagCapturer = new FlagCapturer(detectionSensor, usRight, usLeft, odometer);
		
		do {
			// clear the display
			LCD.clear();

			// ask the user whether the motors should drive in a square or float
			LCD.drawString("< Left>", 0, 0);
			LCD.drawString("        ", 0, 1);
			LCD.drawString(" Start", 0, 2);
 
			buttonChoice = Button.waitForAnyPress();
		} while (buttonChoice != Button.ID_LEFT
				&& buttonChoice != Button.ID_RIGHT);

		if (buttonChoice == Button.ID_LEFT) {	
						
			lcd = new LCDInfo(odometer);

			LCD.clear();
			

			//usl = new USLocalizer (odometer, usRight, USLocalizer.LocalizationType.RISING_EDGE);
			//usl.doLocalization();
			
			//Switch the mode of the ultrasonic sensor to coninuous because it was in ping mode for the localization
			usRight.continuous();
			
			odometer.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
			odometer.setAng(0);
			
			//NavigationTest navTest = new NavigationTest(odometer);
			//navTest.goInASquare();
			flagCapturer.captureFlag(FlagPosCoords, FinalPosCoords);
			
		}while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		
		System.exit(0);		
	}

}

