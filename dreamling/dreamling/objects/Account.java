package dreamling.objects;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.Scanner;

import com.sun.jna.Pointer;

import dreamling.win32.Kernel32;
import dreamling.win32.W32API;
import sockets.Sendable;
import dreamling.client.Dreamling;

public class Account implements Serializable {
	
	private static final long serialVersionUID = 2917476734040270086L;
	
	private int acknChecks;
	public boolean ackn;
	public Process p;
	private int pid;
	public String state;
	public String id;
	private String recentCmdParams;
	public String recentName;
	
	public Account(String id) {
		acknChecks = 0;
		state = "offline";
		this.id = id;
		pid = 0;
		recentCmdParams = "";
		recentName = "";
	}
	
	//should be called once a minute
	//returns whether something changed
	public synchronized boolean checkAckn(Dreamling d) {
		if (getPid() != 0) {
			acknChecks++;
			if (acknChecks >= 8) {
				acknChecks = 0;
				if (!ackn) { //need restart since no callback for 8 minutes
					kill(d);
					startBot();
					return true;
				} else {
					ackn = false; //so next acknowledgement is expected
					return false;
				}
			}
			return false;
		} else {
			acknChecks = 0;
			if (ackn) {
				ackn = false;
				return true;
			} else {
				ackn = false;
				return false;
			}
		}
	}
	
	public synchronized void kill(Dreamling d) {
		updatePid();
		if (!state.equalsIgnoreCase("BANNED")) {
			state = "killed";
			Sendable request = new Sendable();
			request.cmd = "state";
			request.data.add(id);
			request.data.add("killed");
			d.ds.sendSendable(request);
		}
		if (getPid() != 0) {
			try {
				int tries = 0;
				while (getPid() != 0 && tries < 10) {
					//Runtime.getRuntime().exec("taskkill /PID " + getPid());
					if (p != null) {
						p.destroyForcibly();
					}
					Thread.sleep(500);
					updatePid();
					tries++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//only usable if bot was started before by hive
	public void startBot() {
		startBot(recentCmdParams, recentName);
	}
	
	public synchronized void startBot(String cmdParams, String name) {
		if (getPid() == 0) {
			recentCmdParams = cmdParams;
			recentName = name; //needed for callback functionality
			try {
				Runtime rt = Runtime.getRuntime();
				String user_path = System.getProperty("user.home");
				String cmd = "java -Xbootclasspath/p:" + user_path + "/DreamBot/BotData/client.jar"
						+ " -Xmx256M -jar " + user_path + "/DreamBot/BotData/client.jar"
						+ cmdParams;
				Process pr = rt.exec(cmd);
				System.out.println(cmd);
				try {
					if (pr.exitValue() != 0) { //if error happened
						System.out.println(convertStreamToString(pr.getErrorStream())); //blocks until process finishes.
					}
				} catch (Exception e) {}
				
				this.p = pr;
				sniffLogs(p);
				
				updatePid();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void sniffLogs(Process p) {
		Thread t1 = new Thread(new Runnable() {
			@Override
			public void run() {
				String line;
				try {
					BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
					while ((line = error.readLine()) != null) {
						if (!line.contains("log4j")) {
							System.err.println(line);
						}
					}
					error.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t1.start();
		Thread t2 = new Thread(new Runnable() {
			@Override
			public void run() {
				String line;
				try {
					BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
					while ((line = input.readLine()) != null) {
						System.out.println(line);
					}
					input.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t2.start();
	}
	
	public static String convertStreamToString(InputStream is) {
	    Scanner s1 = new Scanner(is);
	    Scanner s = s1.useDelimiter("\\A");
	    String result = s.hasNext() ? s.next() : "";
	    s.close();
	    s1.close();
	    return result;
	}
	
	//checks if process with pid is still running. otherwise sets pid to 0
	//returns true if pid changed
	public boolean updatePid() {
		if (p == null || !p.isAlive()) {
			if (pid != 0) {
				ackn = false;
				pid = 0;
				p = null;
				return true;
			}
			p = null;
			return false;
		} else {
			if (pid == 0) {
				getPid(); //update it
				return true;
			} else {
				return false;
			}
		}
	}
	
	public int getPid() {
		return getPid(p);
	}
	
	public int getPid(Process p) {
		if (p != null && p.isAlive()) {
			if (pid != 0) return pid; //assume pid was already set right on start
			if (p.getClass().getName().equals("java.lang.Win32Process") ||
				p.getClass().getName().equals("java.lang.ProcessImpl")) {
				/* determine the pid on windows plattforms */
				try {
					Field f = p.getClass().getDeclaredField("handle");
					f.setAccessible(true);
					long handl = f.getLong(p);
					
					Kernel32 kernel = Kernel32.INSTANCE;
					W32API.HANDLE handle = new W32API.HANDLE();
					handle.setPointer(Pointer.createConstant(handl));
					pid = kernel.GetProcessId(handle);
					return pid;
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			if (p.getClass().getName().equals("java.lang.UNIXProcess")) {
				/* get the PID on unix/linux systems */
				try {
					Field f = p.getClass().getDeclaredField("pid");
					f.setAccessible(true);
					pid = f.getInt(p);
					return pid;
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
			return pid = 0;
		} else {
			if (pid != 0) {
				ackn = false;
				pid = 0;
				p = null;
			} else {
				p = null;
			}
			return pid;
		}
	}
	
	public void sendMessage(String msg) {
		msg = msg + "\n"; //cuz a string is only a line of it ends with \n
		if (p != null && p.isAlive()) {
			try {
				OutputStream out = p.getOutputStream();
				out.write(msg.getBytes(Charset.forName("UTF-8")));
				out.flush();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}
