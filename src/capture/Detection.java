package capture;
import lejos.nxt.ColorSensor;
import lejos.nxt.UltrasonicSensor;

/**
 * The <code>Detection</code> class is used to manage the detection of all objects in front of the light sensor. 
 * It will determine what color the object and at what distance it is. With the color it can
 * determine which type of object it is (i.e. the opponents flag, your flag, or a wall block.
 * 
 * Each instance of the <code> Detection </code> class has a Color and Ultrasonic sensor. 
 * 
 * @see FlagCapture
 *  
 * @author  Alessandro Parisi
 * @since   1.0
 * @version 1.0
 *
 */
public class Detection {
	
	private ColorSensor colorSensor;
	private UltrasonicSensor us;
	
	/**
	 * Constructor that initiliazes the Detection object which sotres the color and ultrasonic sensors as well
	 * as sets the floodlight of the color sensor on.
	 *  
	 * @param colorSensor A color sensor used for the robot to differentiate objects by their color
	 * @param us A ultrasonic sensor used for the robot to determine teh distance from the object
	 */
	
	public Detection(ColorSensor colorSensor, UltrasonicSensor us){
		this.colorSensor = colorSensor;
		this.us = us;
		this.colorSensor.setFloodlight(true);
	}
	
	/**
	 * Returns the <code>int</code> number representing the blue value of RBG value 
	 * seen by the color sensor
	 * @return the <code>int</code> number representing the blue value of RBG value 
	 * seen by the color sensor
	 */
	public int getColor(){
		return colorSensor.getColor().getBlue();
	}
	
        
        /**
         * Returns the <code>int</code> distance read by the ultrasonic sensor
         * @return the <code>int</code> distance read by the ultrasonic sensor
         */
	public int getDistance(){
		 return us.getDistance();
	}
	
	/**Method that calculates the block type depending on the distance read by the
	 * ultrasonic sensor and the color of the object read by the color sensor. It returns
	 * no block if nothing is in front of it.
	 * 
	 * @return the <code>String</code> name of the type of block that
	 * is currently in front of the sensors
	 */
	public String getBlockType(){
		String s;
		if(us.getDistance() < 26){
			if(getColor() > 26){
				s = "Blue Block";
			}
			else
				s = "Wall Block";
		}
		else
			s = "No Block";
		
		return s;
	}
	
	/**Returns a number representing the different block types depending.
	 * 0 - No Block, 
	 * 1 - Wall Block, 
	 * 2 - Blue Block
	 * 
	 * @return the <code>int</code> number that represents the block
	 */
	public int getBlockNumber(){
		String s;
		s = getBlockType();
		if (s.equals("Blue Block")){
			return 2;
		}
		else if (s.equals("Wall Block")){
			return 1;
		}
		return 0;
	}
}
