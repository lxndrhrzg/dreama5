package GUI.logging;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.JTextArea;

import GUI.interfaces.CommandExecutor;

public class Interceptor extends PrintStream {
	
	File f;
	JTextArea ta;
	CommandExecutor ce;
	
	//output stream for redirection, file for logging, textarea for logging too
    public Interceptor(OutputStream out, File f, JTextArea ta) {
        super(out, true);
    	this.f = f;
    	this.ta = ta;
    }
    
    public void setFile(File f) {
    	this.f = f;
    }
    
    @Override
    public void println(String s) {
        super.println(s);
        if (f != null) {
        	try {
				//customly added: log messages to file:
				BufferedWriter writer = new BufferedWriter(new FileWriter(f, true)); //true for appending, not replacing 
				writer.write(getDate() + s + "\n"); //add date as beginning of line
				writer.close();
			} catch (Exception e) {
				//e.printStackTrace(); //this might cause endless loops of error-logs
			}
        }
        if (ce != null) {
        	ce.runCommand(s);
        }
        if (ta != null) {
        	ta.append(s + "\n");
        }
    }
    
    public void setCommandExecutor(CommandExecutor ce) {
    	this.ce = ce;
    }
    
    public static void main(String[] args) {
        PrintStream origOut = System.out;
        PrintStream interceptor = new Interceptor(origOut, new File("output.txt"), null);
        System.setOut(interceptor);// just add the interceptor
        System.out.println("Test123");
        System.out.println("123123123");
    }
    
    public static String getDate() {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("[dd.MM.yy HH:mm] ");
		return sdf.format(c.getTime());
	}
}