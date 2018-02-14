package dreamling.listMaker;

import GUI.interfaces.ExitControl;

public class ExitListener implements ExitControl {

	DreamlistMaker dm;
	
	public ExitListener(DreamlistMaker dm) {
		this.dm = dm;
	}
	
	public void doBeforeExit() {
		dm.saveConfig();
	}
	
}
