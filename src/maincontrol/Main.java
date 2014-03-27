package maincontrol;
import java.io.IOException;

import bluetooth.BluetoothConnection;
import bluetooth.PlayerRole;
import bluetooth.StartCorner;
import bluetooth.Transmission;
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
 * @author  Alessandro Parisi
 * @version 1.0
 * @since   1.0
 *
 */
public class Main {

    private static int[] FinalPosCoords = {0};

    private static int[] FlagPosCoords = {4, 4, 6, 6};
    private static int color = 1;
    
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
     */
	public static void main(String[] args) {
		
		int buttonChoice;
		//Create the slave NXT
		RemoteNXT slaveNXT = null;
		try {
			slaveNXT = new RemoteNXT("TEAM11-2", RS485.getConnector());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		//Bluetooth Sample Code
		BluetoothConnection conn = new BluetoothConnection();
		
		// as of this point the bluetooth connection is closed again, and you can pair to another NXT (or PC) if you wish
		
		// example usage of Tranmission class
		Transmission t = conn.getTransmission();
		if (t == null) {
			LCD.drawString("Failed to read transmission", 0, 5);
		} else {
			PlayerRole role = t.role;
			StartCorner corner = t.startingCorner;
			int redZoneBottomLeft_X = t.redZoneLL_X;
			int redZoneBottomLeft_Y = t.redZoneLL_Y;
			int redZoneTopLeft_X = t.redZoneUR_X;			
			int redZoneTopLeft_Y = t.redZoneUR_Y;
			int	redFlag = t.redFlag;
			
			FlagPosCoords[0] = redZoneBottomLeft_X;
			FlagPosCoords[1] = redZoneBottomLeft_Y;
			FlagPosCoords[2] = redZoneTopLeft_X;
			FlagPosCoords[3] = redZoneTopLeft_Y;
			
			color = redFlag;
			// print out the transmission information
			conn.printTransmission();
		}
		
		//Initialize the slave's ports
		//TODO the Color sensor is undefined for slave so make sure ports are good
		ColorSensor detectionSensor = new ColorSensor(SensorPort.S3);
		UltrasonicSensor usRight = new UltrasonicSensor(slaveNXT.S2);
		UltrasonicSensor usLeft = new UltrasonicSensor(slaveNXT.S3);
		RemoteMotor grabberRight = slaveNXT.A;
		RemoteMotor  grabberLeft = slaveNXT.B;
		
		//Initilize the master's ports
		ColorSensor odometerCorrectionRight = new ColorSensor(SensorPort.S1);
		ColorSensor odometerCorrectionLeft = new ColorSensor(SensorPort.S2);
		
		// setup the odometer, display, and ultrasonic and light sensors

		odometer = new Odometer(true, odometerCorrectionRight, odometerCorrectionLeft);
		FlagCapturer flagCapturer = new FlagCapturer(detectionSensor, usRight, usLeft, odometer, grabberRight, grabberLeft);
		
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
			

			usl = new USLocalizer (odometer, usRight, USLocalizer.LocalizationType.RISING_EDGE, odometerCorrectionRight, odometerCorrectionLeft);
			usl.doLocalization();
			
			//Switch the mode of the ultrasonic sensor to coninuous because it was in ping mode for the localization
			usRight.continuous();
			usLeft.continuous();
			
			odometer.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
			odometer.setAng(0);
			
			//NavigationTest navTest = new NavigationTest(odometer);
			//navTest.goInASquare();
			flagCapturer.captureFlag(FlagPosCoords, FinalPosCoords, color);
			
		}while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		
		System.exit(0);		
	}

}

