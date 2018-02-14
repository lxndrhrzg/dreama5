package GUI.windowTools;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import GUI.interfaces.ExitControl;

public class ExitWindowListener extends WindowAdapter {
	
	/*
	 * how-to-use is described in ExitControl class
	 */
	ExitControl window;
	
	public ExitWindowListener(ExitControl ec) {
		window = ec;
	}
	
	public void windowClosing(WindowEvent e) {
        try {
        	window.doBeforeExit();
        	System.exit(0);
        } catch (Exception ex) {
			ex.printStackTrace();
		}
    }
}
