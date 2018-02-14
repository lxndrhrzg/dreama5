package dreamling.client;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.swing.text.JTextComponent;

import GUI.interfaces.CommandExecutor;
import dreamling.objects.Account;
import sockets.Sendable;

public class ClockThread extends Thread implements CommandExecutor {

	public static final String COMMAND = "cmd=cmd";
	public static final String CALLBACK = "callback=";
	
	long lastMinutes;
	int lastDate;
	Dreamling d;
	JTextComponent console;
	
	public static ClockThread init(Dreamling d, JTextComponent console) {
		ClockThread ct = new ClockThread();
		ct.lastMinutes = 0;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd");
		ct.lastDate = Integer.parseInt(sdf3.format(cal.getTime()));
		ct.d = d;
		ct.console = console;
		ct.start();
		return ct;
	}
	
	@Override
	public void run() {
		super.run();
		System.out.println("Starting Clock Thread.");
		while (true) {
			try {
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("mm");
				long minutes = Integer.parseInt(sdf.format(cal.getTime()));
				//SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm");
				//long currentTime = Long.parseLong(sdf2.format(cal.getTime()));
				SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd");
				int currentDate = Integer.parseInt(sdf3.format(cal.getTime()));
				
				for (Account acc : d.accounts) {
					acc.updatePid();
				}
				
				//runs once per minute
				if (lastMinutes != minutes) {
					lastMinutes = minutes;
					
					//runs once per day
					if (lastDate != currentDate) {
						lastDate = currentDate;
						
						d.outInterceptor.setFile(new File("output" + currentDate + ".txt"));
						d.errInterceptor.setFile(new File("error" + currentDate + ".txt"));
					}
					
					//check acknowledgement for accs
					for (Account acc : d.accounts) {
						acc.updatePid();
						acc.checkAckn(d);
					}
				}
				try {
					sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void runCommand(String command) {
		if (command != null && !command.isEmpty()
				&& command.contains(COMMAND)) {
			try {
				command = command.replace(";", "");
				List<String> c1 = Arrays.asList(command.split("&"));
				HashMap<String, String> map = new HashMap<String, String>();
				for (String c2 : c1) {
					String[] split = c2.split("=");
					if (split.length >= 2) {
						map.put(split[0], split[1]);
					}
				}
				if (map.containsKey("callMule") && map.containsKey("botName") && map.containsKey("world") && map.containsKey("mats")) {
					Sendable request = new Sendable();
					request.cmd = "callMule";
					request.data.add(map.get("callMule"));
					request.data.add(map.get("botName"));
					request.data.add(map.get("world"));
					request.data.add(map.get("mats"));
					d.ds.sendSendable(request);
				}
				if (map.containsKey("kill")) {
					String botName = map.get("kill");
					if (botName != null && !botName.isEmpty()) {
						Sendable request = new Sendable();
						request.cmd = "killBot";
						request.data.add(d.ds.dreamlingName);
						request.data.add(botName);
						d.ds.sendSendable(request);
					}
				}
				if (map.containsKey("callback")) {
					String accName = map.get("callback");
					if (accName != null && !accName.isEmpty()) {
						for (Account acc : d.accounts) {
							acc.updatePid();
							if (acc.recentName.equalsIgnoreCase(accName)
									&& !acc.ackn
									&& acc.getPid() != 0) {
								acc.ackn = true;
							}
						}
					}
				}
				if (map.containsKey("state") && map.containsKey("botName")) {
					String accName = map.get("botName");
					String state = map.get("state");
					if (accName != null && !accName.isEmpty()) {
						for (Account acc : d.accounts) {
							if (acc.recentName.equalsIgnoreCase(accName)
									&& !acc.state.equalsIgnoreCase(state)) {
								acc.state = state;
								Sendable request = new Sendable();
								request.cmd = "update";
								request.data.add(d.ds.dreamlingName);
								request.data.add(acc.id);
								request.data.add("state=" + acc.state);
								d.ds.sendSendable(request);
							}
						}
					}
				}
				if (map.containsKey("restart")) { //state is optional
					String accName = map.get("restart");
					String state = map.get("state");
					if (accName != null && !accName.isEmpty()) {
						for (Account acc : d.accounts) {
							if (acc.recentName.equalsIgnoreCase(accName)) {
								acc.kill(d);
								acc.startBot();
								if (state != null && !state.isEmpty()
										&& !acc.state.equalsIgnoreCase(state)) {
									acc.state = state;
									Sendable request = new Sendable();
									request.cmd = "update";
									request.data.add(acc.id);
									request.data.add(acc.state);
									d.ds.sendSendable(request);
								}
							}
						}
					}
					
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
