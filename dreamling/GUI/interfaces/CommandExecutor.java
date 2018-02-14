package GUI.interfaces;

public interface CommandExecutor {
	//Interceptor sends each line to this object. runCommand() should filter out unnecessary lines.
	public void runCommand(String command);
}
