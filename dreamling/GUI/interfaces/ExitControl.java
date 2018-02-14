package GUI.interfaces;

public interface ExitControl {
	
	/* how to use with ControlWindow:
	 * create new class in your project which implements this interface.
	 * define doBeforeExit method in it.
	 * (also create constructor if u want to pass parameters to work on in doBeforeExit)
	 * yourWindow.addWindowListener(new ExitWindowListener(new YourClass()));
	 */
	
	
	public void doBeforeExit();
}
