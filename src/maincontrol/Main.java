package maincontrol;
import capture.FlagCapturer;
import traveling.Odometer;
import lejos.nxt.*;
import lejos.util.Timer;
import localize.USLocalizer;
import localize.USLocalizer.LocalizationType;

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

    private static int[] FinalPosCoords;

    private static int[] FlagPosCoords;
    
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
		UltrasonicSensor us = new UltrasonicSensor(SensorPort.S2);
		ColorSensor colorSensor = new ColorSensor(SensorPort.S1);
		
		// setup the odometer, display, and ultrasonic and light sensors

		odometer = new Odometer(true, colorSensor);
		FlagCapturer flagCapturer = new FlagCapturer(colorSensor, us, odometer);
		
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
			
			usl = new USLocalizer (odometer, us, USLocalizer.LocalizationType.RISING_EDGE);
			usl.doLocalization();
			
			//Switch the mode of the ultrasonic sensor to coninuous because it was in ping mode for the localization
			us.continuous();
			
			odometer.setPosition(new double [] {0.0, 0.0, 0.0}, new boolean [] {true, true, true});
			odometer.setAng(0);
			
			flagCapturer.captureFlag(FlagPosCoords, FinalPosCoords);
			
		}while (Button.waitForAnyPress() != Button.ID_ESCAPE);
		
		System.exit(0);
		
	}

}
