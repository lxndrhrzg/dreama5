package dreamling.client;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class DreamlingUpdater {

	
	public static void delayedStart(String filename) {
		System.out.println("running delayedStart of " + filename);
		BufferedWriter writer = null;
		File f = null;
        try {
            f = new File("DelayedStart.java");
            if (f.exists()) {
            	f.delete();
            	f.createNewFile();
            }
            writer = new BufferedWriter(new FileWriter(f));
            String sourceCode = "import java.io.File;"
				+ "public class DelayedStart {"
				+ 	"public static void main(String[] args) {"
				+ 		"try {"
				+ 			"Thread.sleep(5000);"
				+ 		"} catch (Exception e) {}"
				+ "try {"
				+ "	Runtime rt = Runtime.getRuntime();"
				+ "	String cmd = \"java -jar \" + System.getProperty(\"user.dir\") + File.separator + args[0];"
				+ "	rt.exec(cmd);"
				+ "} catch (Exception e) {"
				+ "	e.printStackTrace();"
				+ "}"
				+ "}"
				+ "}";
            
            writer.write(sourceCode);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (Exception e) {
            }
        }
		System.out.println("DelayedStart.java written. now compiling.");
    	try {
    	    runProcess("javac " + System.getProperty("user.dir") + File.separator + "DelayedStart.java", true);
    		System.out.println("DelayedStart compiled. now running");
    	    runProcess("java -cp " + System.getProperty("user.dir") + " DelayedStart " + filename, false);
    		System.out.println("DelayedStart running.");
	    } catch (Exception e) {
			System.out.println("DelayedStart error: ");
	    	e.printStackTrace();
	    }
		System.out.println("DelayedStart finished.");
    	
	}

	private static void runProcess(String command, boolean waitFor) throws Exception {
		System.out.println("running command: " + command);
	    Process pro = Runtime.getRuntime().exec(command);
	    if (waitFor) {
	    	pro.waitFor();
		    System.out.println("exitValue: " + pro.exitValue());
	    }
	}
}
