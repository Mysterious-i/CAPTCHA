package maincontrol;

import traveling.Navigation;
import traveling.Odometer;

public class NavigationTest {
	Odometer odo;
	Navigation nav;
	public void goInASquare(){
		nav.travelTo(60, 0);
		nav.travelTo(60, 60);
		nav.travelTo(0, 60);
		nav.travelTo(0, 0);
		nav.turnTo(0, true);
	}
	public NavigationTest(Odometer odo){
		this.odo = odo;
		nav = odo.getNavigation();
	}
	
}
