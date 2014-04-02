package traveling;
import lejos.nxt.*;
import lejos.util.Timer;
import lejos.util.TimerListener;

/**
 * The <code>Odometer</code> class implements a <code>TimerListener</code>
 * and keeps track of the relative position of the robot from a point it starts at.
 * 
 * It has methods to access the varaibles which describe the relative positions and
 * orientations. 
 * 
 * An instance of this class holds and instance of the <code>Navigation</code> class which will be
 * interacting with it, as well as an instance of <code>OdometryCorrection</code> to fix the odometer's
 * readings every time it cross lines. This <code>Odometer</code> is a timer which is started in the constructor
 * with a default period. This <code>Odometer</code> also holds values for the left and right radius and
 * width of the robot. 
 * 
 * The <code>Odometer</code> has a lock which is used to synchronize methods for different calls 
 * to the thread and avoid deadlocks.
 * 
 * @author ale
 *
 */
public class Odometer implements TimerListener {
    private static final int DEFAULT_PERIOD = 25;
    private NXTRegulatedMotor leftMotor, rightMotor;
    private Timer odometerTimer;
    private Navigation navigation;
    private OdometryCorrection odometryCorrection;
    private Object lock;
    private double x, y, theta;
    private double [] oldDH, dDH;
      
    private double leftRadius, rightRadius, width;
      

     /**
      * The constructor of the <code>Odometer</code> takes in a period, which determines how often
      * the timer will restart, and inilializes the dimensions of the robot (right radius, 
      * left radius and width) and instances of the classes <code>Navigation</code> and 
      * <code>OdometryCorrection</code>. Finally it sets the inital position of the robot to the 
      * origin with angle 0. The <code>boolean</code> start will determine whether the 
      * <code>Odometer</code> timer will start in the constructer.
      * 
      * @param period The <code>int</code> period that will determine the rate at which
      * the odometer timer is called
      * @param start The <code>boolean</code> value which determines whether the odometer
      * will star the timer
      * @param odometerCorrectionLeft The left <code>ColorSensor</code> that will be used for odometry correction
      * @param odometerCorrectionRight The right <code>ColorSensor</code> that will be used for odometry correction
      */
    public Odometer(int period, boolean start, ColorSensor odometerCorrectionLeft, ColorSensor odometerCorrectionRight) {
        
        this.navigation = new Navigation(this);
        //odometryCorrection = new OdometryCorrection(this, odometerCorrectionLeft, odometerCorrectionRight);

        odometerTimer = new Timer(period, this);
        leftMotor = Motor.A;
        rightMotor = Motor.B;
        x = 0.0;
        y = 0.0;
        theta = 0.0;
        oldDH = new double [2];
        dDH = new double [2];
        lock = new Object();
        this.rightRadius = -2.1;
        this.leftRadius = -2.1;
        this.width = 17.25;
          
        //If the use wants, start the timer
        if (start){
            odometerTimer.start();
        }
        
        //odometryCorrection.start();
    }
    

    /**
     * The constructor of the <code>Odometer</code> takes in the default period, which determines how often
     * the timer will restart, and inilializes the dimensions of the robot (right radius, 
     * left radius and width) and instances of the classes <code>Navigation</code> and 
     * <code>OdometryCorrection</code>. Finally it sets the inital position of the robot to the 
     * origin with angle 0 and 
     * 
     * @param odometerCorrectionLeft The left <code>ColorSensor</code> that will be used for odometry correction
     * @param odometerCorrectionRight The right <code>ColorSensor</code> that will be used for odometry correction
     */
    public Odometer(ColorSensor odometerCorrectionLeft, ColorSensor odometerCorrectionRight) {
        this(DEFAULT_PERIOD, false, odometerCorrectionLeft, odometerCorrectionRight);
    }
      
    /**
     * The constructor of the <code>Odometer</code> takes in the default period, which determines how often
     * the timer will restart, and inilializes the dimensions of the robot (right radius, 
     * left radius and width) and instances of the classes <code>Navigation</code> and 
     * <code>OdometryCorrection</code>. Finally it sets the inital position of the robot to the 
     * origin with angle 0 and 
     * 
     * @param start the <code>boolean<code> value that determines whther the odometer timer will start
     * in the constructor
     * @param odometerCorrectionLeft The left <code>ColorSensor</code> that will be used for odometry correction
     * @param odometerCorrectionRight The right <code>ColorSensor</code> that will be used for odometry correction
     */
    public Odometer(boolean start, ColorSensor odometerCorrectionLeft, ColorSensor odometerCorrectionRight) {
        this(DEFAULT_PERIOD, start, odometerCorrectionLeft, odometerCorrectionRight);
    }
      
    /**
     * The constructor of the <code>Odometer</code> takes in a period, which determines how often
     * the timer will restart, and inilializes the dimensions of the robot (right radius, 
     * left radius and width) and instances of the classes <code>Navigation</code> and 
     * <code>OdometryCorrection</code>. Finally it sets the inital position of the robot to the 
     * origin with angle 0. The <code>Odometer</code> timer will not start in the constructor.
     * 
     * @param period the <code>int</code> period that will determine the rate at which
     * the odometer timer is called
     * @param odometerCorrectionLeft the <code>ColorSensor</code> that will be used for odometry correction
     * @param odometerCorrectionRight the <code>ColorSensor</code> that will be used for odometry correction
     */
    public Odometer(int period, ColorSensor odometerCorrectionLeft, ColorSensor odometerCorrectionRight) {
        this(period, false, odometerCorrectionLeft, odometerCorrectionRight);
    }
  
      
    /**
     * It updates the x and y depending on the angle it is travelling at and past positions
     * {@inheritDoc}
     */
    public void timedOut() {    
         this.getVector(dDH);
        dDH[0] -= oldDH[0];
        dDH[1] -= oldDH[1];
  
        // update the position in a critical region
        synchronized (this) {
            theta -= dDH[1];
            theta = fixDegAngle(theta);
  
            x += dDH[0] * Math.cos(Math.toRadians(theta));
            y += dDH[0] * Math.sin(Math.toRadians(theta));
        }
  
        oldDH[0] += dDH[0];
        oldDH[1] += dDH[1]; 
    }
    
    //Calculates the displacement and direction
    private void getVector(double[] data) {
        int leftTacho, rightTacho;
        leftTacho = leftMotor.getTachoCount();
        rightTacho = rightMotor.getTachoCount();
  
        data[0] = (leftTacho * leftRadius + rightTacho * rightRadius) * Math.PI / 360.0;
        data[1] = (rightTacho * rightRadius - leftTacho * leftRadius) / width;
    }
    
    /**
     * Returns the x of the robot
     * @return the <code>double</code> x of the robot
     */
    public double getX() {
        synchronized (lock) {
            return x;
        }
    }
    /**
     * Returns the y of the robot
     * @return the <code>double</code> y of the robot
     */
    public double getY() {
        synchronized (lock) {
            return y;
        }
    }
    /**
     * Returns the angle of the robot
     * @return the <code>double</code> angle of the robot
     */
    public double getAng() {
        synchronized (lock) {
            return theta;
        }
    }
    /**
     * Sets the <code>double</code> angle of the odometer
     * @param angle the <code>double</code> angle used to update the odometer
     */
    public void setAng(double angle) {
        synchronized (lock) {
            theta = angle;
        }
    } 
    
    /**
     * Updates the position of the robot that will be udpated by the
     * <code>Odometer</code>'s positions
     * @param pos the positions of the robot that will be udpated by the
     * <code>Odometer</code>'s positions
     */
    public void getPosition(double [] pos) {
        synchronized (lock) {
            pos[0] = x;
            pos[1] = y;
            pos[2] = theta;
        }
    }
      
    // accessors to motors
    	/**
    	 * Returns the motors array of the robot
    	 * @return the motors array of the robot
    	 */
        public NXTRegulatedMotor [] getMotors() {
            return new NXTRegulatedMotor[] {this.leftMotor, this.rightMotor};
        }
    	/**
    	 * Returns the left motor of the robot
    	 * @return the left motor of the robot
    	 */
        public NXTRegulatedMotor getLeftMotor() {
            return this.leftMotor;
        }
    	/**
    	 * Returns the right motor of the robot
    	 * @return the right motor of the robot
    	 */
        public NXTRegulatedMotor getRightMotor() {
            return this.rightMotor;
        }
    	/**
    	 * Returns the navigation of the robot
    	 * @return the <code>Navigation</code> of the robot
    	 */
        public Navigation getNavigation() {
            return this.navigation;
        }
          
    /**
     * 
     * @param pos the <code>double</code> position array which will be used to update
     * the x y and angle of the odometer 
     * @param update the <code>boolean</code> array that indicate which elements in the position
     * array need to be updated 
     */
    public void setPosition(double [] pos, boolean [] update) {
        synchronized (lock) {
            if (update[0]) x = pos[0];
            if (update[1]) y = pos[1];
            if (update[2]) theta = pos[2];
        }
    }
  
    // static helper methods
    private static double fixDegAngle(double angle) {        
        if (angle < 0.0)
            angle = 360.0 + (angle % 360.0);
          
        return angle % 360.0;
    }
      
    //This method calculates the minimum angle from two doubles
    private static double s(double a, double b) {
        double d = fixDegAngle(b - a);
          
        if (d < 180.0)
            return d;
        else
            return d - 360.0;
    }
    /**
     * Sets the <code>double</code> x of the odometer
     * @param x the <code>double</code> x used to update the odometer
     */
    public void setX(double x) {
        synchronized (lock) {
            this.x = x;
        }
    } 
    /**
     * Sets the <code>double</code> y of the odometer
     * @param y the <code>double</code> y used to update the odometer
     */
    public void setY(double y) {
        synchronized (lock) {
            this.y = y;
        }
    } 
} 