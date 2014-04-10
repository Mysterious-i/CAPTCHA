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
import localize.Localizer;
import maincontrol.NXTLCPRespond.Responder;

/**
 * The <code>Main</code> class structures the different stages that need to be
 * preformed for the robot to succesfully capture a flag. This includes setting
 * up the <code>Odometer</code>, preforming <code>USLocalization</code>, and
 * then preforming <code>FlagCapturer</code>.
 * 
 * An instance of this class will hold the coordinates that surround the area
 * where the flag will be and the coordinates that surround the area where the
 * flag needs to be placed.
 * 
 * This class directly communicates with the instance of the
 * <code>LCDInfo</code> class for printing on the NXT brick screen.
 * 
 * @see Odometer
 * @see FlagCapture
 * @see LCDInfo
 * 
 * @author Alessandro Parisi
 * 
 * @version 1.0
 * @since 1.0
 * 
 */
public class Main {

	private static int[] FinalPosCoords = {4, 2};
	private static int[] AvoidZone = {4, 0};
	private static int[] FlagPosCoordsLower = {0, 0};
	private static int[] FlagPosCoordsUpper = {2, 2};
	private static int[] temp = new int [2];
	private static int id = 1;
	private static int color = 1;

	private static Odometer odometer;
	private static Localizer localizer;
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

		// Sets all the respective variables from bluetooth
		Transmission t = conn.getTransmission();
		if (t == null) {
			LCD.drawString("Failed to read transmission", 0, 5);
		} else {
			PlayerRole role = t.role;
			StartCorner corner = t.startingCorner;

			if(role.getId() == 2){	

				FlagPosCoordsLower[0] = t.greenZoneLL_Y;
				FlagPosCoordsLower[1] = t.greenZoneLL_X;
				FlagPosCoordsUpper[0] = t.greenZoneUR_Y;
				FlagPosCoordsUpper[1] = t.greenZoneUR_X;

				FinalPosCoords[0] = t.redDZone_Y;
				FinalPosCoords[1] = t.redDZone_X;

				AvoidZone[0] = t.greenDZone_Y;
				AvoidZone[1] = t.greenDZone_X;

				color = t.redFlag;
			}
			else{

				FlagPosCoordsLower[0] = t.redZoneLL_Y;
				FlagPosCoordsLower[1] = t.redZoneLL_X;
				FlagPosCoordsUpper[0] = t.redZoneUR_Y;
				FlagPosCoordsUpper[1] = t.redZoneUR_X;

				FinalPosCoords[0] = t.greenDZone_Y;
				FinalPosCoords[1] = t.greenDZone_X;

				AvoidZone[0] = t.redDZone_Y;
				AvoidZone[1] = t.redDZone_X;

				color = t.greenFlag;

			}
			// print out the transmission information
			conn.printTransmission();
		}


		//Initialize the slave's ports
		UltrasonicSensor usRight = new UltrasonicSensor(slaveNXT.S2);
		UltrasonicSensor usLeft = new UltrasonicSensor(slaveNXT.S3);
		RemoteMotor grabberRight = slaveNXT.A;
		RemoteMotor  grabberLeft = slaveNXT.B;

		usRight.continuous();
		usLeft.continuous();

		//Initilize the master's ports
		ColorSensor odometerCorrectionRight = new ColorSensor(SensorPort.S1);
		ColorSensor odometerCorrectionLeft = new ColorSensor(SensorPort.S2);
		ColorSensor detectionSensor = new ColorSensor(SensorPort.S3);

		// setup the odometer, display, and falg capture class

		odometer = new Odometer(true, odometerCorrectionRight, odometerCorrectionLeft);
		FlagCapturer flagCapturer = new FlagCapturer(detectionSensor, usRight, usLeft, odometer, grabberRight, grabberLeft);


		lcd = new LCDInfo(odometer);

		LCD.clear();

		//localize
		localizer = new Localizer (usLeft, usRight, odometer, odometerCorrectionLeft, odometerCorrectionRight);
		localizer.localize();

		id = t.startingCorner.getId();
		//apply the change in axes
		FinalPosCoords = changeCoordinate(FinalPosCoords, id);
		AvoidZone = changeCoordinate(AvoidZone, id);
		FlagPosCoordsLower= changeCoordinate(FlagPosCoordsLower, id);
		FlagPosCoordsUpper= changeCoordinate(FlagPosCoordsUpper, id);

		//Swap the lower left and upper right if you are in corner 3 as it is closer
		if(id == 3){
			temp[0] = FlagPosCoordsLower[0];
			temp[1] = FlagPosCoordsLower[1];

			FlagPosCoordsLower[0] = FlagPosCoordsUpper[0];
			FlagPosCoordsLower[1] = FlagPosCoordsUpper[1];

			FlagPosCoordsUpper[0] = temp[0];
			FlagPosCoordsUpper[1] = temp[1];
		}


		//Switch the mode of the ultrasonic sensor to coninuous
		usRight.continuous();
		usLeft.continuous();

		odometer.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
		odometer.setAng(0);
		odometer.startOdometryCorrection();

		//Capture the flag
		flagCapturer.captureFlag(FlagPosCoordsLower, FlagPosCoordsUpper, FinalPosCoords, AvoidZone, color);


		System.exit(0);		
	}
	//This method changes the coordinate system depending on the starting corner
	private static int[] changeCoordinate(int[] coor,  int id) {
		return changeCoordinate(coor[0] , coor[1], id);
	}

	//This method changes the coordinate system depending on the starting corner
	private static int[] changeCoordinate(int x, int y, int id) {
		if (id == 1) {
			return new int[] {x, y}; 
		} else if (id == 2) {
			return new int[] {10 - y, x};
		} else if (id == 3) {
			return new int[] {10 - x, 10 - y};
		} else if (id == 4) {
			return new int[] {y, 10-x};
		} else {
			System.exit(1);
			return null;
		}
	}
}

