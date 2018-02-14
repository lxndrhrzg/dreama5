package dreamling.client;

import GUI.interfaces.ExitControl;

public class OnExitSaver implements ExitControl {

	Dreamling d;
	
	public OnExitSaver(Dreamling c) {
		this.d = c;
	}
	
	@Override
	public void doBeforeExit() {
		System.out.println("Closing Dreamling.");
		System.err.println("Closing Dreamling. (not an error)");
		d.disconnect();
	}

}
