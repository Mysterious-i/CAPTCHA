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
	private int MAX_DISTANCE = 20;
	private RemoteMotor grabberRight, grabberLeft;
	private boolean sawNothing = false;
    private final double TILE_LENGTH = 30.3;
	private double XFlagLowerLeft, YFlagLowerLeft;
	private double XFlagUpperRight, YFlagUpperRight;
	private double XDropOff, YDropOff;
	private double XFlagMid, YFlagMid;
	private final int LIGHT_DISTANCE = 7, MAX_TRAVEL_DISTANCE = 40, INTERVAL = 15;
	private int MAX_OBJECT_DISTANCE = 30;
	private final int OFFSET_INZONE = 7, DISTANCE_TO_TURN = 9;
	private PastPositions pastPos;
	private UltrasonicPoller usPoller; 

	private double avoidZoneLowerX, avoidZoneLowerY, avoidZoneUpperX, avoidZoneUpperY;
	private final double EXTRA_ROBOT_SIZE = 5.0;
	
	/**
	 * The constructor for the <code>FlagCapturer</code> class initializes the odometer and navigation
	 * as well as creates an object of type <code>Detection</code>. It also takes in and initializes
	 * the <code>Motor</code> for both grabber arms. It sets a default speed of 70 and current rotation 
	 * angle to 0. Finally it takes in as parameter, the ultrasonic sensor's (both left and right). 
	 * 
	 * @param colorSensor The <code>ColorSensor</code> that is used by the <code>Detection</code> class
	 * @param usRight The <code>UltrasonicSensor</code> that is used by the <code>Detection</code> class
	 * @param usLeft The <code>UltrasonicSensor</code> that is used by the <code>Detection</code> class
	 * @param odometer The <code>Odometer</code> that is used to keep track of the position of the robot
	 * @param grabberLeft The left <code>RemoteMotor</code> that is used for grabbing the blocks 
	 * @param grabberRight The right <code>RemoteMotor</code> that is used for grabbing the blocks 
	 */
	public FlagCapturer(ColorSensor colorSensor, UltrasonicSensor usRight, UltrasonicSensor usLeft, Odometer odometer, RemoteMotor grabberRight, RemoteMotor grabberLeft) {
	    	
	    this.odometer = odometer;
		this.navigation = odometer.getNavigation();
		
		detection = new Detection(colorSensor, usRight, usLeft, MAX_DISTANCE);
		usPoller = new UltrasonicPoller(usLeft, usRight, MAX_DISTANCE);
		
		grabberRight.rotateTo(0);
		grabberLeft.rotateTo(0);
		grabberRight.setSpeed(70);
		grabberLeft.setSpeed(70);
		
		this.grabberRight = grabberRight;
		this.grabberLeft = grabberLeft;
		pastPos = new PastPositions();
		
		
	}


	/**
	 * This method takes in the coordinates of the area of the flag and coordinates of the area where
	 * the robot needs to navigate after grabbing the flag. This methods leads the robot to 
	 * search and grab the specified <code>int</code> color flag which is within the <code>int</code> 
	 * array of flag position coordinates and brings it to the final position which is specified by the
	 * <code>int</code> array of final positions.
	 * 
	 * @param FlagPosCoords The <code>int</code> array of positions of the area where the flag is
	 * @param FinalPosCoords The <code>int</code> array of positions of the area where the flag is
	 * @param color The <code>int</code> color of the flag needed to capture
	 */
	public void captureFlag(int[] FlagPosCoordsLower, int[] FlagPosCoordsUpper, int[] FinalPosCoords, int[] AvoidZone, int color) {
        
		//Get the coordonates of the lower left and upper right positions of the flag zone
        this.XFlagLowerLeft = FlagPosCoordsLower[0]*TILE_LENGTH;
        this.YFlagLowerLeft = FlagPosCoordsLower[1]*TILE_LENGTH;
        this.XFlagUpperRight = FlagPosCoordsUpper[2]*TILE_LENGTH;
        this.YFlagUpperRight = FlagPosCoordsUpper[3]*TILE_LENGTH;
        
        this.avoidZoneLowerX = AvoidZone[0] * TILE_LENGTH + EXTRA_ROBOT_SIZE;
        this.avoidZoneLowerY = AvoidZone[1] * TILE_LENGTH + EXTRA_ROBOT_SIZE;
        this.avoidZoneUpperX = AvoidZone[0] * TILE_LENGTH + TILE_LENGTH + EXTRA_ROBOT_SIZE;
        this.avoidZoneUpperY = AvoidZone[1] * TILE_LENGTH + TILE_LENGTH + EXTRA_ROBOT_SIZE;
        
        //Calculate the middle coordiantes of the Flag Zone
	    this.XFlagMid = ((FlagPosCoordsUpper[2] - FlagPosCoordsLower[0])/2 + FlagPosCoordsLower[0])*TILE_LENGTH;
	    this.YFlagMid = ((FlagPosCoordsUpper[3] - FlagPosCoordsLower[1])/2 + FlagPosCoordsLower[1])*TILE_LENGTH;
        
	    //Calculate the middle coordinates of the square the block needs to be dropped off at
        this.XDropOff = FinalPosCoords[0]*TILE_LENGTH + TILE_LENGTH/2;
	    this.YDropOff = FinalPosCoords[1]*TILE_LENGTH + TILE_LENGTH/2;
        
	    MAX_DISTANCE = (int) ((FlagPosCoordsUpper[3] - FlagPosCoordsLower[1])/2 * TILE_LENGTH) - 15;
	    
		usPoller.start();
		
        pathTo(XFlagLowerLeft, YFlagLowerLeft);
		navigation.travelTo(XFlagLowerLeft, YFlagLowerLeft);
		
		searchForFlag(color);
        
        pathTo(XDropOff, YDropOff);
        navigation.travelTo(XDropOff, YDropOff);
        
        //Drop off the flag
        navigation.turnTo(225, true);
        dropFlag();
	
	}
	/*
	 * This method finds a path to a specified X and Y position.
	 * It will always try to move up and right until it reaches its destination.
	 * In the case it has walls on both the right and up directions, it will back track to
	 * a previous point and try a different direction.
	 */
	private void pathTo(double XDest, double YDest){		
		int direction = 0;
		boolean atEndY = false;
		boolean atEndX = false;
		double XCheckPoint = 0, YCheckPoint = 0;
		pastPos.putPoint(0, 0);
		//Set the current vertical and horizontal position
		ver = odometer.getX();
		hor = odometer.getY();
		
		//While we are not past our destination, keep moving
		while(odometer.getX() < XDest || odometer.getY() < YDest){
			
			//While there is not block in front
			while(!usPoller.getIsWall() || isInOtherTeamDropOff()){
				
				//If we are at our destination, exit the loop
				if(odometer.getX() > XDest && odometer.getY() > YDest){
					break;
				}
				
				/*Depending on the direction we are moving, update the
				 * respective horizontal and verticle positions
				 */
				switch (direction) {
				case 0:
					ver = XDest;
					hor = odometer.getY();
					break;
				case 1:
					hor = YDest;
					ver = odometer.getX();
					break;
				case 2:
					hor = odometer.getY();
					ver -= 2*INTERVAL;
					break;
				case 3:
					ver = odometer.getX();
					hor -= 2*INTERVAL;
					break;
				}

				
				/*Store the current x and y positions as safe points for future
				 * backtracking reference.
				 */
				if(distanceTravelled(XCheckPoint, YCheckPoint, odometer.getX(), odometer.getY()) > 15){
					Sound.beep();
					pastPos.putPoint(odometer.getX(), odometer.getY());
					
					XCheckPoint = odometer.getX();
					YCheckPoint = odometer.getY();
				
				}
				
				navigation.travelTo(ver, hor);
				
				while(direction == 2 && odometer.getX() > ver + 1){
					Sound.beep();
				}
				
				while(direction == 3 && odometer.getY() > hor + 1){
					
				}
				
				if(direction == 2){
					direction = 1;
					atEndX = false;
				}
				if(direction == 3){
					direction = 0;
					atEndY = false;
				}
				
				/*
				 * If you are at your destinations x position and moving in that direction
				 * change your direction to right.
				 */
				if(odometer.getX() >= XDest && direction == 0){
					atEndX = true;
					navigation.turnTo(90, true);
					direction = 1;
				}
				/*
				 * If you are at your destinations y position and moving in that direction
				 * change your direction to up.
				 */
				else if(odometer.getY() >= YDest && direction == 1){
					atEndY = true;
					navigation.turnTo(0, true);
					direction = 0;
				}
			}
			
			navigation.stopMotors();
			
			//If you are at your destination, exit the loop
			if(odometer.getX() > XDest && odometer.getY() > YDest){
				break;
			}
			/*
			 * If you were moving up when you saw something in front of you,
			 * IF you are not in your final position on your right, turn right,else
			 * are then go turn left.  
			 */
			if(direction == 0){
				if(!atEndY){
					navigation.turnTo(90, true);
					direction = 1;
				}
				else{
					navigation.turnTo(270, true);
					direction = 3;
				}
				usPoller.initializePolls();
				usPoller.setIsWall(false);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/*If there is something in your way in this direction as well,
				 * backtrack to a previous safe point.
				 */
				
				if(usPoller.getIsWall()){
					Sound.beep();
					ver = pastPos.getPointX();
					hor = pastPos.getPointY();
					while(distanceTravelled(odometer.getX(), odometer.getY(), ver, hor) < 20){
						ver = pastPos.getPointX();
						hor = pastPos.getPointY();
					}
					navigation.stopMotors();
					navigation.travelTo(ver, hor);
					while(distanceTravelled(odometer.getX(), odometer.getY(), ver, hor) >= 2){
						
					}
				}
			}
			/*
			 * If you were moving right when you saw something in front of you,
			 * If you are not in your final position on top, turn up, else
			 * then go turn left.   
			 */
			else if (direction == 1){
				if(!atEndX){
					navigation.turnTo(0, true);
					direction = 0;
				}
				else{
					navigation.turnTo(180, true);
					direction = 2;
				}
				/*If there is something in your way in this direction as well,
				 * backtrack to a previous safe point.
				 */
				usPoller.initializePolls();
				usPoller.setIsWall(false);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(usPoller.getIsWall()){
					Sound.beep();
					ver = pastPos.getPointX();
					hor = pastPos.getPointY();
					while(distanceTravelled(odometer.getX(), odometer.getY(), ver, hor) < 20){
						ver = pastPos.getPointX();
						hor = pastPos.getPointY();
					}
					navigation.stopMotors();
					navigation.travelTo(ver, hor);
					while(distanceTravelled(odometer.getX(), odometer.getY(), ver, hor) >= 2){
						
					};
				}
			}
			else if (direction == 2){
				direction = 3;
			}
			else if (direction == 3){
				direction = 2;
			}
			
			usPoller.initializePolls();
			usPoller.setIsWall(false);

		}
	}

	
	/*
	 * This method is used to search for a flag of specified color. It will
	 * start at the bottom of the flag zone and incrementally move its way up
	 * while scanning a 180 degree angle till it finds a block. If the block is the
	 * incorrect color, then move it away from the zone, if it is the correct color
	 * then grab it and move to the specified corner of the flag zone.
	 * 
	 */
	private void searchForFlag(int color){	
		int offSetX = 0;
	    boolean isDone = false;
	    boolean oppositeDirection = false;
	    /*Try to capture the flag from the lower middle of the flag zone and scanning angles
	     * from -90 to 90
	     */
	    isDone = captureAtCorner(XFlagLowerLeft + OFFSET_INZONE, YFlagMid, color, -90, 90);
	   
	    /*While the robot did not find the flag, it will move upwards by a constant amount
	     * and keep scanning the same angle -90 to 90, until it finds the correct block.
	     */
		while(!isDone){
		   
			if(!oppositeDirection){
		    	offSetX = offSetX+20;
		    }
		    else{
		    	offSetX = offSetX-20;
		    }
			
		    if(offSetX + XFlagLowerLeft >= XFlagUpperRight - MAX_DISTANCE && !oppositeDirection){
		    	oppositeDirection = true;
		    	offSetX = (int) (XFlagUpperRight - MAX_DISTANCE);
		    }
		    if(offSetX + XFlagLowerLeft <= XFlagLowerLeft + MAX_DISTANCE && oppositeDirection){
		    	oppositeDirection = false;
		    	offSetX = (int) (XFlagLowerLeft + MAX_DISTANCE);
		    }

		    
		    isDone = captureAtCorner(XFlagLowerLeft + offSetX, YFlagMid, color, -90, 90);

		}
		navigation.travelTo(XFlagUpperRight, YFlagUpperRight);
	}
	private double distanceTravelled(double x1, double y1, double x2, double y2){
		return Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
	}
	/*
	 * This method will drop the flag.
	 */
	private void dropFlag(){
	    grabberRight.rotateTo(0, true);
	    grabberLeft.rotateTo(0, false);
	}
	/*
	 * This method will grab the flag.
	 */
	private void grabFlag(){
	    grabberRight.rotateTo(-200, true);
	    grabberLeft.rotateTo(-200, false);

	}
	/*
	 * This method will scan from a specified location and from a specified 
	 * starting and final angle. On it's way, it will grab blocks that are not
	 * the right color and move them outside the flag zone. If it finds the
	 * right colored block in the specified angle it returns true, else returns
	 * false.
	 */
	private boolean captureAtCorner(double XSearchStart, double YSearchStart, int finalColor, int startAngle, int endAngle){
	    int currentAngle = startAngle;
	    boolean flagIsCaptured = false;
	    double XBlock, YBlock;
	    boolean fail = false;
        
	    navigation.travelTo(XSearchStart, YSearchStart);
        
	    /*While the flag is not yet captured, we try to find it
	     */
	    while(!flagIsCaptured && currentAngle <= endAngle){  
			/*
			 * Increase the angle in intervals of 5 until the final angle is reached 
			 * or the ultrasonic sensor scans something with both sensor.
			 */
	    	do{
				currentAngle += 5;
		    	navigation.turnTo(currentAngle, true);
		    }while(currentAngle <= endAngle && ((detection.getLeftDistance() > MAX_OBJECT_DISTANCE) || (detection.getRightDistance() > MAX_OBJECT_DISTANCE)));
			
	    	//If the final angle was reached, exit the loop
			if(currentAngle >= endAngle){
				break;
			}
			
			/*
			 * If something was scanned, move the angle a bit more to ensure the 
			 * light sensor is at an appropriate angle to scan the block
			 */
			currentAngle += 12;
			navigation.turnTo(currentAngle, true);
			
			/*
			 * Move forward towards to block until you are close enough to scan it.
			 * If you moved a big distance and still did not see any block, the angle was bad
			 * so return to the scanning coordinates and start scanning again.
			 */
			while(detection.getLeftDistanceOnce() > LIGHT_DISTANCE && detection.getRightDistanceOnce() > LIGHT_DISTANCE){
				
				navigation.goForwardSpeed(-150);
				
				if(distanceTravelled(XSearchStart, YSearchStart, odometer.getX(), odometer.getY()) > MAX_TRAVEL_DISTANCE){
					fail = true;
					break;
				}
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			navigation.setSpeeds(0, 0);
			/*
			 * While he robot did not fail in finding a block check the color and get the 
			 * current x and y of the block.  
			 */
			if(!fail){
				          
		        int recordedColor = detection.getBlockNumber();
				XBlock = odometer.getX();
				YBlock = odometer.getY();
					
				navigation.goForwardSpeed(150);
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				//Back up until the robot has enough distance to turn
				while(detection.getLeftDistance() < DISTANCE_TO_TURN || detection.getRightDistance() < DISTANCE_TO_TURN){
					navigation.goForwardSpeed(150);
				}
				navigation.setSpeeds(0,0);
					
				//Turn the robot around
				navigation.turnTo(180 + odometer.getAng(), true);
				
				/*The robot will move backwards until your are close to previously
				 * recorded position of the block
				 */
				while(Math.abs(odometer.getX() - XBlock) < 3 || Math.abs(odometer.getY() - YBlock) < 3 ){
					navigation.setSpeeds(100, 100);
				}
				
				/*Move backwards a bit more to ensure the block is direction behind you.
				 */
				navigation.setSpeeds(100,100);
				
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				/*
				 * If the color is correct, move backwards a bit more
				 * to make sure the block is touching the back of our robot,
				 * grab it, stop, turn to a zero angle heading, and return with
				 * falg being captured as true.
				 */
				if(recordedColor == finalColor){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					navigation.setSpeeds(0,0);
					grabFlag();				
					flagIsCaptured = true;
					navigation.turnTo(0,true);
				}
				/*
				 * If the flag was not the correct color, then grab the flag and bring
				 * it to the bottom center of the safe zone and drop it off there. After
				 * move the coordinates you were searching at and continue the search.
				 */
				else{
					
					navigation.setSpeeds(0,0);
					grabFlag();	
					
					navigation.travelTo(XFlagLowerLeft,YFlagMid);
					navigation.turnTo(0, true);	
					dropFlag();
					navigation.travelTo(XSearchStart, YSearchStart);
				} 
			}
			else{
				navigation.travelTo(XSearchStart, YSearchStart);
			}
	    }  
	    return flagIsCaptured;
	}
	private boolean isInOtherTeamDropOff(){
		return ((odometer.getX() > avoidZoneLowerX) && (odometer.getX() < avoidZoneUpperX) &&(odometer.getY() > avoidZoneLowerY) && (odometer.getY() > avoidZoneUpperY));
	}
}
