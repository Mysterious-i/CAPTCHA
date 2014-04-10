package maincontrol;
import traveling.Odometer;
import lejos.nxt.LCD;
import lejos.util.Timer;
import lejos.util.TimerListener;
/**
 * The <code>LCDInfo</code> class is used to print stuff to the LCD monitor on the NXT brick.
 * 
 * An instance of the <code>LCDInfo</code> class holds a <code>int</code> refresh rate, and
 * <code>Timer</code> lcdTimer which controls the rate at which elements will be displayed on
 * the screen.
 * 
 * @see Main
 * 
 * @author Alessandro Parisi
 * @author Stefan Ti
 * @author Bei Chen liu
 *  
 * @version 1.0
 * @since   1.0
 *
 */
public class LCDInfo implements TimerListener{
    private static final int LCD_REFRESH = 350;
	private Timer lcdTimer;
	private Odometer odometer;
	/**
	 * The constructor of the <code>LCDInfo</code> class will initiate the <code>Odometer</code>,
	 * and <code>Timer</code>. It then starts the <code>Timer</code>
	 * @param odo The <code>odometer</code> instance use to update the LCD
	 */
	public LCDInfo(Odometer odometer) {
		this.lcdTimer = new Timer(LCD_REFRESH, this);
		LCD.clear();
		this.odometer = odometer;
		// start the timer
		lcdTimer.start();
	}
	
	/**
	 * This method is used to time out the timer.
	 * {@inheritDoc}
	 */
	public void timedOut() { 
		LCD.drawString("odo x : " + odometer.getX(), 0, 0);
		LCD.drawString("odo y : " + odometer.getY(), 0, 1);

	}
}
