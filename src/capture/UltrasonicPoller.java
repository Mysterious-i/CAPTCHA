package capture;
import lejos.nxt.UltrasonicSensor;

// the ultrasonicPoller Thread is used to allow continuous data sampling, a filter could be implemented but prove unnecessary by experimentation 
public class UltrasonicPoller extends Thread {
	private int distanceLeft = 255;
	private int distanceRight = 255;
	private UltrasonicSensor usLeft;
	private UltrasonicSensor usRight;
	private int MAX_DISTANCE;
	private int countRight = 0;
	private int countLeft = 0;
	private final int NUMBER_OF_POLLS = 10;
	private int[] pollingLeft = new int[NUMBER_OF_POLLS];
	private int[] pollingRight = new int[NUMBER_OF_POLLS];
	private boolean isThereAWall = false;
	
	public void run() {
		while (true) {

			
			
			if(getRightWindowedDistance() < MAX_DISTANCE || getLeftWindowedDistance() < MAX_DISTANCE){
				initializePolls();
				isThereAWall = true;
			}
			
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
		}
	}

	public int getLeftDistance() {
		return distanceLeft;
	}
	public int getRightDistance() {
		return distanceRight;
	}

	public UltrasonicPoller(UltrasonicSensor usRight, UltrasonicSensor usLeft, int MAX_DISTANCE) {
		this.usLeft = usLeft;
		this.usRight = usRight;
		this.MAX_DISTANCE = MAX_DISTANCE;
		initializePolls();
	}
	
	
	public void initializePolls(){
		for(int i = 0; i < NUMBER_OF_POLLS; i++){
			pollingLeft[i] = 255;
			pollingRight[i] = 255;
		}
	}
	
	/**
	 * Returns the <code>int</code> average distance read by the left ultrasonic sensor after 10 pings
	 * 
	 * @return the <code>int</code> average distance read by the left ultrasonic sensor after 10 pings
	 */
	public int getLeftWindowedDistance() {
		int average = 0;

		countLeft++;

		if (countLeft > 9) {
			countLeft = 0;
		}

		pollingLeft[countLeft] = usLeft.getDistance();

		for (int i = 0; i < 10; i++) {
			average += pollingLeft[i];
		}
		
		return average / 10;
	}
	
	/**
	 * Returns the <code>int</code> average distance read by the right ultrasonic sensor after 10 pings
	 * 
	 * @return the <code>int</code> average distance read by the right ultrasonic sensor after 10 pings
	 */
	public int getRightWindowedDistance() {
		int average = 0;

		countRight++;

		if (countRight > 9) {
			countRight = 0;
		}

		pollingRight[countRight] = usRight.getDistance();

		for (int i = 0; i < 10; i++) {
			average += pollingRight[i];
		}

		return average / 10;
	}
	public boolean getIsWall(){
		return isThereAWall;
	}
	public void setIsWall(boolean decision){
		isThereAWall = decision;
	}
}