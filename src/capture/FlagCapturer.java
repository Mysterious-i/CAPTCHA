package capture;
import traveling.Navigation;
import traveling.Odometer;
import lejos.nxt.ColorSensor;
import lejos.nxt.Sound;
import lejos.nxt.UltrasonicSensor;
import lejos.nxt.remote.RemoteMotor;

/**
 * The <code>FlagCapturer</code> class is used to navigate the robot to a specified location, search for the flag
 * in this location, find it, grab it, and then go to the desired location to capture the flag.
 * 
 * It navigates the robot to the desired location by using the <code>Navigation</code> and <code>Odometer</code> classes.
 * 
 * Each instance of the <code>FlagCapturer</code> class is able to have an instance of the <code>Navigaiton</code> class, 
 * an instance of the <code>Odometer</code> class, an instance of the <code>Detection</code> class and holds the current position
 * as well as past check-pointed positions which were safe.
 * 
 * @see Detection
 * @see Main
 * 
 * @author  Alessandro Parisi
 * @since   1.0
 * @version 1.0
 */
public class FlagCapturer {

	private Navigation navigation;
	private Detection detection;
	private Odometer odometer;
	private static double ver = 0;
	private static double hor = 0;
	private final int MAX_DISTANCE = 25;
	private RemoteMotor grabberRight, grabberLeft;
	
	/**
	 * The constructer for the <code>FlagCapturer</code> class initializes the odometer and navigation
	 * as well as creates an object of type <code>Detection</code>. 
	 * 
	 * @param colorSensor The <code>ColorSensor</code> that is used by the <code>Detection</code> class
	 * @param us The <code>UltrasonicSensor</code> that is used by the <code>Detection</code> class
	 * @param odometer The <code>Odometer</code> that is used by the <code>Detection</code> class
	 */
	public FlagCapturer(ColorSensor colorSensor, UltrasonicSensor usRight, UltrasonicSensor usLeft, Odometer odometer, RemoteMotor grabberRight, RemoteMotor grabberLeft) {
	    	
	    this.odometer = odometer;
		this.navigation = odometer.getNavigation();
		detection = new Detection(colorSensor, usRight, usLeft, MAX_DISTANCE);
		this.grabberRight = grabberRight;
		this.grabberLeft = grabberLeft;
	}


	/**
	 * This method takes in the coordinates of the area of the flag and coordiantes of the area where
	 * the robot needs to navigate after grabbing the flag and makes the robot go grab the flag which is in
	 * the specific location and bring it to the final desired position.
	 * 
	 * @param FlagPosCoords The <code>int</code> positions of the area where the flag is
	 * @param FinalPosCoords The <code>int</code> positions of the area where the flag is
	 * @param color The <code>int</code> color of the flag needed to capture
	 */
	public void captureFlag(int[] FlagPosCoords, int[] FinalPosCoords, int color) {
	    //double XStart = FlagPosCoords[0]*30;
	    //double YStart = FlagPosCoords[1]*30;
		//pathToFlag(XStart, YStart);  
		
		//navigation.travelTo(XStart, YStart);
		//navigation.turnTo(0, true);
		
		searchForFlag(FlagPosCoords, color);
	
	}
	/*
	 * Up = 0, Right = 1, Down = 2, Left = 3
	 * This method find a path to the given X destination and Y destination
	 */
	private void pathToFlag(double XDest, double YDest){
		int direction = 0;
		double XCheckPoint = 0;
		double YCheckPoint = 0;
		boolean atEndY = false;
		boolean atEndX = false;
		int counterDown = 0, counterLeft = 0;
		
		while(odometer.getX() < XDest || odometer.getY() < YDest){
			
			while(!detection.wallInFront()){

				if(odometer.getX() > XDest && odometer.getY() > YDest){
					break;
				}
				
				switch (direction) {
				case 0:
					ver += 20;
					break;
				case 1:
					hor += 20;
					break;
				case 2:
					ver -= 20;
					counterDown++;
					break;
				case 3:
					hor -= 20;
					counterLeft++;
					break;	
				}
				
				if(counterDown >= 1){
					counterDown = 0;
					direction = 1;
					atEndX = false;
				}
				if(counterLeft >= 1){
					counterLeft = 0;
					direction = 0;
					atEndY = false;
				}
				
				XCheckPoint = odometer.getX();
				YCheckPoint = odometer.getY();
				navigation.travelTo(ver, hor);
				
				if(odometer.getX() >= XDest && direction == 0){
					atEndX = true;
					direction = 1;
				}
				else if(odometer.getY() >= YDest && direction == 1){
					atEndY = true;
					direction = 0;
				}
			}
			if(odometer.getX() > XDest && odometer.getY() > YDest){
				break;
			}
			if(direction == 0){
				if(!atEndY){
					navigation.turnTo(90, true);
					direction = 1;
				}
				else{
					navigation.turnTo(270, true);
					direction = 3;
				}
				
				if(detection.wallInFront()){
					ver = XCheckPoint;
					hor = YCheckPoint;
					navigation.travelTo(ver, hor);
				}
			}
			
			else if (direction == 1){
				if(!atEndX){
					navigation.turnTo(0, true);
					direction = 0;
				}
				else{
					navigation.turnTo(180, true);
					direction = 2;
				}
				if(detection.wallInFront()){
					ver = XCheckPoint;
					hor = YCheckPoint;
					navigation.travelTo(YCheckPoint, XCheckPoint);
				}
				direction = 0;
			}
			else if (direction == 2){
				direction = 3;
			}
			else if (direction == 3){
				direction = 2;
			}


		}
	}
	/*
	 * This method is used to search for the flag when given the coordinates of the area the Flag is in
	 * and the color of the flag
	 */
	private void searchForFlag(int[] FlagPosCoords, int color){
	    double XStart = FlagPosCoords[0]*30;
	    double YStart = FlagPosCoords[1]*30;
	    double XEnd = FlagPosCoords[2]*30;
	    double YEnd = FlagPosCoords[3]*30;
	    
	    boolean flagIsCaptured = false;
	    
	    navigation.turnTo(0, true);
	   
	    boolean isDone = captureAtCorner(XStart, YStart, color, 0, 90);
	    if(!isDone){
		   navigation.travelTo(XStart, YStart);
		   navigation.travelTo(XStart, YEnd);
	      while(!isDone){
		    navigation.travelTo(XEnd, YEnd);
		   	isDone = captureAtCorner(XEnd, YEnd, color, 180, 270);
		  }
	    }

	}
	private double distanceTravelled(double x1, double y1, double x2, double y2){
		return ((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
	}
	/*
	 * This method will drop the flag once it is in the correct location
	 */
	private void dropFlag(){
	    
	}
	/*
	 * This method will grab the flag once it is in the correct location
	 */
	private void grabFlag(){
	    grabberRight.rotateTo(-200, true);
	    grabberLeft.rotateTo(-200, false);

	}
	/*
	 * This method will scan for the block given an initial starting position, respective angles, and
	 * color of the block.
	 */
	private boolean captureAtCorner(double XStart, double YStart, int color, int startAngle, int endAngle){
	    int currentAngle = startAngle;
	    boolean failedAttempt = false;
	    boolean flagIsCaptured = false;
	    double XBlock, YBlock;
	    
	    while(!flagIsCaptured && currentAngle <= endAngle){  
			do{
				currentAngle += 5;
		    	navigation.turnTo(currentAngle, true);
		    }while(currentAngle <= endAngle && (detection.getLeftDistance() > 55));
			
			if(currentAngle >= endAngle){
				break;
			}
			
			currentAngle += 15;
			navigation.turnTo(currentAngle, true);
			
			while(detection.getLeftDistance() > 5){
				
				navigation.goForwardSpeed(-100);
				
				if(distanceTravelled(XStart, YStart, odometer.getX(), odometer.getY()) > 900){
					failedAttempt = true;
					break;
				}
			}
			
			if(color == (detection.getBlockNumber()) && !failedAttempt){
				
				XBlock = odometer.getX();
				YBlock = odometer.getY();
				
				while(detection.getLeftDistance() < 15){
					navigation.goForwardSpeed(100);
					
				}
				navigation.setSpeeds(0,0);
				
				navigation.turnTo(180 + odometer.getAng(), true);
				
				while(odometer.getX() < XBlock+5 || odometer.getY() < YBlock+5){
					navigation.setSpeeds(100, 100);
				}
				grabFlag();
				flagIsCaptured = true;
				navigation.turnTo(0, true);
			}
			else{
				while(detection.getLeftDistance() < 15){
					navigation.goForwardSpeed(100);
				}
				navigation.travelTo(XStart, YStart);
				currentAngle+=30;
				failedAttempt = false;
			}
	   }
	    return flagIsCaptured;
	}
	
}
