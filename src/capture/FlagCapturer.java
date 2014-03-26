package capture;
import traveling.Navigation;
import traveling.Odometer;
import lejos.nxt.ColorSensor;
import lejos.nxt.UltrasonicSensor;

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
	private static final int UNTIL_END = 10000;
	private final int MAX_DISTANCE = 25;
	private boolean[][] isValidMapPoint = new boolean[7][7];
	/**
	 * The constructer for the <code>FlagCapturer</code> class initializes the odometer and navigation
	 * as well as creates an object of type <code>Detection</code>. 
	 * 
	 * @param colorSensor The <code>ColorSensor</code> that is used by the <code>Detection</code> class
	 * @param us The <code>UltrasonicSensor</code> that is used by the <code>Detection</code> class
	 * @param odometer The <code>Odometer</code> that is used by the <code>Detection</code> class
	 */
	public FlagCapturer(ColorSensor colorSensor, UltrasonicSensor usRight, UltrasonicSensor usLeft, Odometer odometer) {
	    	
	    this.odometer = odometer;
		this.navigation = odometer.getNavigation();
		detection = new Detection(colorSensor, usRight, usLeft, MAX_DISTANCE);
		//initializeMap();
	}


	/**
	 * This method takes in the coordinates of the area of the flag and coordiantes of the area where
	 * the robot needs to navigate after grabbing the flag and makes the robot go grab the flag which is in
	 * the specific location and bring it to the final desired position.
	 * 
	 * @param FlagPosCoords The <code>int</code> positions of the area where the flag is
	 * @param FinalPosCoords The <code>int</code> positions of the area where the flag is
	 */
	public void captureFlag(int[] FlagPosCoords, int[] FinalPosCoords) {
		
		
	    pathToFlag(90, 90);
	}
	
	
	/*
	 * Up = 0, Right = 1, Down = 2, Left = 3
	 * 
	 * TODO make it work in the case it reaches an extremity of the map and cant move right no more
	 * 
	 * TODO instead of making it go down for 30 make it back track to the previous safe point and rerun
	 * the algorithm but this time by making it chose a different direction.
	 * 
	 * This method works by checking each possibility to navigate the XDest and YDest
	 * It works by preferring to move in the direciton it is given, right now it is hard coded to move
	 * to the top-right most corner. It then checks if it cannot move in these positions, it checks down, then up.
	 * Once it finds a direciton to move in, if it moves in a direciton that is towards the destination then move it in that
	 * direciton forever (i.e. till it reaches the destination or sees a block), if not move just 30 to avoid a block
	 * then try a good direction again (i.e. top or right in this case)
	 * 
	 * TODO fix that it is hard coded to move to the top-right most corner.
	 * 
	 * 
	 */
/*
	private void findPath(int direction, int length, double XDest, double YDest) {
		int tempL = 0;
		while((( direction == 2 || direction == 3) && (tempL < length)) || 
				((direction == 0) && (odometer.getX() <= XDest)) || 
				((direction == 1) && (odometer.getY() <= YDest))){
			
			if (detection.wallInFront()) {
				if (direction == 0 || direction == 1) {
					if (direction == 0) {
						navigation.turnTo(90, true);
						if (detection.wallInFront()) {
							navigation.turnTo(180, true);
							if (detection.wallInFront()) {
								navigation.turnTo(270, true);
								findPath(3, 30, XDest, YDest);
								findPath(0, UNTIL_END, XDest, YDest);
								
							} else {
							    	findPath(2, 30, XDest, YDest);
							    	findPath(1, UNTIL_END, XDest, YDest);
							}
						}
						findPath(1, UNTIL_END, XDest, YDest);
					} else {
						navigation.turnTo(0, true);
						if (detection.wallInFront()) {
							navigation.turnTo(180, true);
							if (detection.wallInFront()) {
								navigation.turnTo(270, true);
								findPath(3, 30, XDest, YDest);
								findPath(0, UNTIL_END, XDest, YDest);
							} else {
							    	findPath(2, 30, XDest, YDest);
								findPath(1, UNTIL_END, XDest, YDest);
							}
						}
						findPath(0, UNTIL_END, XDest, YDest);
					}
				} else if (direction == 2) {
				    	findPath(3, 30, XDest, YDest);
				} else {
				    	findPath(2, 30, XDest, YDest);
				}
			}
	
			switch (direction) {
			case 0:
				ver += 30;
				break;
			case 1:
				hor += 30;
				break;
			case 2:
				ver -= 30;
				break;
			case 3:
				hor -= 30;
				break;	
			}
			tempL+= 30;
			
			navigation.travelTo(ver,hor);
		}
		return;
	}*/
	/*
	 * Up = 0, Right = 1, Down = 2, Left = 3
	 * 
	 */
	private void pathToFlag(double XDest, double YDest){
		int direction = 0;
		double XCheckPoint = 0;
		double YCheckPoint = 0;
		boolean atEndY = false;
		boolean atEndX = false;
		double counter = 0;
		
		while(odometer.getX() < XDest || odometer.getY() < YDest){

			
			while(!detection.wallInFront()){
				if(atEndY && direction == 1 && counter == 0){
					direction = 3;
				}
				if(direction == 3 && counter >= 1){
					direction = 0;
					counter = 0;
					atEndY = false;
				}
				if(atEndX && direction == 0 && counter == 0 ){
					direction = 2;
				}
				if(direction == 2 && counter >= 1){
					direction = 1;
					counter = 0;
					atEndX = false;
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
					break;
				case 3:
					hor -= 20;
					break;	
				}
				counter++;
				XCheckPoint = odometer.getX();
				YCheckPoint = odometer.getY();
				navigation.travelTo(ver, hor);
				
				if((odometer.getX() >= XDest) && direction == 0){
					atEndX = true;
				}
				else if((odometer.getY() >= YDest) && direction == 1){
					atEndY = true;
				}
			}
			if(direction == 0){		
				navigation.turnTo(90, true);
				if(detection.wallInFront()){
					ver = XCheckPoint;
					hor = YCheckPoint;
					navigation.travelTo(ver, hor);
				}
				direction = 1;
			}
			
			else if (direction == 1){
				navigation.turnTo(0, true);
				if(detection.wallInFront()){
					ver = XCheckPoint;
					hor = YCheckPoint;
					navigation.travelTo(YCheckPoint, XCheckPoint);
				}
				direction = 0;
			}


		}
	}
	/*
	 * This method is used to search for the flag when given the coordinates of the area the Flag is in
	 */
	private void searchForFlag(int[] FlagPosCoords){
	    
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
	    
	}
	private void initializeMap(){
		for(int i = 0; i < isValidMapPoint.length; i++){
			for(int j = 0; j < isValidMapPoint[0].length; j++){
				isValidMapPoint[i][j] = true;
			}
		}
	}
	
}
