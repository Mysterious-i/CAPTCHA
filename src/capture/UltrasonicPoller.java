package capture;
import lejos.nxt.UltrasonicSensor;

/** The <code>UltrasonicPoller</code> Thread is used to allow continuous data sampling,
 * a filter was implemented to improve the data and make it more reliable.
 *
 * @author Bei Chen liu
 * @author Alessandro Parisi
 * @author Stefan Ti
 */
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
	private boolean pingMode = false;
	private boolean running = true;
    
    /**The constructor of the <code>UltrasonicPoller</code> class takes in both Ultrasonic sensors and a
     * max distance and initializes them
     * 
     * @param usRight the right <code>UltrasonicSensor</code>
     * @param usLeft the left <code>UltrasonicSensor</code>
     * @param MAX_DISTANCE the <code>int</code> max distance before a object is reported as detected
     */
	public UltrasonicPoller(UltrasonicSensor usRight, UltrasonicSensor usLeft, int MAX_DISTANCE) {
		this.usLeft = usLeft;
		this.usRight = usRight;
		this.MAX_DISTANCE = MAX_DISTANCE;
		initializePolls();
	}
    /**
     * It updates the x and y depending on the angle it is travelling at and past positions
     * {@inheritDoc}
     */
	public void run() {
		while (running) {
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

    /**This method returns the distance read from the left ultrasonic sensor
     *
     * @return the <code>int</code> distance from the left ultrasonic sensor
     */
	public int getLeftDistance() {
		return distanceLeft;
	}
    
    /**This method returns the distance read from the right ultrasonic sensor
     *
     * @return the <code>int</code> distance from the right ultrasonic sensor
     */
	public int getRightDistance() {
		return distanceRight;
	}
	
	/** This method initliazes all the polls in the array of distances to 255
     *
     */
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
	 * Returns the <code>int</code> average distance read by the right ultrasonic sensor after taking a new ping and averaging out the past 10.
	 * 
	 * @return the <code>int</code> average distance read by the right ultrasonic sensor after taking a new ping and averaging out the past 10.
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
    
    /** Returns whether there was a wall in front of the robot
     *
     * @return the <code>boolean</code> of whether there is a wall in front
     */
	public boolean getIsWall(){
		return isThereAWall;
	}
    /** Sets whether there was a wall in front of the robot
     *
     * @param the <code>boolean</code> of whether there is a wall in front
     */
	public void setIsWall(boolean decision){
		isThereAWall = decision;
	}
    /** This method stops the thread from excecuting polls
     *
     */
	public void stop(){
		this.running = false;
		
	}
    /** This method starts the thread and makes it excecute polls
     *
     */
	public void start2(){
		this.running = true;
	}
}