package dreamling.client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import dreamling.objects.Account;
import sockets.DreamClient;
import sockets.DreamServer;
import sockets.Sendable;


public class DreamlingSocket extends DreamClient {
	
	private Dreamling d;
	public String dreamlingName;
	
	public static final String NAME_FILE_PATH = "dreamling_id.bin";
	
	public DreamlingSocket(Dreamling d) {
		super(DreamServer.HOST, DreamServer.TCP);
		this.d = d;
	}
	
	
	public void saveName() {
		if (!new File(NAME_FILE_PATH).exists()) {
			try {
				new File(NAME_FILE_PATH).createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			FileOutputStream fos = new FileOutputStream(new File(NAME_FILE_PATH));
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(dreamlingName);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean loadName() {
		if (new File(NAME_FILE_PATH).exists()) {
			try {
				FileInputStream fis = new FileInputStream(new File(NAME_FILE_PATH));
				ObjectInputStream ois = new ObjectInputStream(fis);
				dreamlingName = (String) ois.readObject();
				ois.close();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	@Override
	public Sendable getRegistration() {
		loadName();
		Sendable result = null;
		if (dreamlingName != null) {
			result = new Sendable(dreamlingName);
		} else {
			result = new Sendable();
			dreamlingName = result.name;
			saveName();
		}
		System.out.println("name: " + dreamlingName);
		result.cmd = "dreamling";
		return result;
	}
	
	@Override
	public Sendable processSendable(Sendable data) {
		while (d == null) {
			try {
				Thread.sleep(200);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Sendable result = null; //send nothing if no cmd found
		if (data.cmd != null) {
			
			if (data.cmd.equals("loadAccounts")) {
				List<String> accountIds = data.data;
				if (accountIds != null) {
					d.accounts = new ArrayList<Account>();
					for (String id : accountIds) {
						Account acc = new Account(id);
						d.accounts.add(acc);
					}
				}
			}
			if (data.cmd.equals("startBot")) { //data: botId, botName, cmdParams
				if (data.data.size() >= 2) {
					final String id = data.data.get(0);
					final String botName = data.data.get(1);
					final String cmdParams = data.data.get(2);
					for (Account acc : d.accounts) {
						if (acc.id.equalsIgnoreCase(id)) {
							acc.startBot(cmdParams, botName);
						}
					}
				}
			}
			if (data.cmd.equals("killBot")) { //data: botId
				if (data.data.size() >= 1) {
					final String id = data.data.get(0);
					for (Account acc : d.accounts) {
						if (acc.id.equalsIgnoreCase(id)) {
							acc.kill(d);
						}
					}
				}
			}
			if (data.cmd.equals("getInfo")) {
				if (data.data.size() >= 1) {
					final String id = data.data.get(0);
					for (Account acc : d.accounts) {
						if (acc.id.equalsIgnoreCase(id)) {
							result = new Sendable();
							result.cmd = "getInfo";
							result.isAnswer = true;
							result.data.add(acc.id);
							result.data.add(acc.state);
							result.data.add(String.valueOf(acc.getPid()));
						}
					}
				}
			}
			if (data.cmd.equals("killAll")) {
				for (Account acc : d.accounts) {
					acc.kill(d);
				}
			}
			if (data.cmd.equals("messageBot")) {
				if (data.data.size() >= 2) {
					final String id = data.data.get(0);
					final String message = data.data.get(1);
					for (Account acc : d.accounts) {
						if (acc.id.equalsIgnoreCase(id)) {
							acc.sendMessage(message);
						}
					}
				}
			}
			/*if (data.cmd.equals("update")) {
				boolean success = false;
				try {
					success = d.updateAcc(Integer.parseInt(data.data.get(0)), Integer.parseInt(data.data.get(1)), data.data.get(2));
				} catch (Exception e) {
					e.printStackTrace();
				}
				result = new Sendable(data.name);
				result.isAnswer = true;
				result.cmd = data.cmd;
				if (success) {
					result.data.add(String.valueOf(success));
				}
			}
			if (data.cmd.equals("get")) {
				result = new Sendable(data.name);
				result.isAnswer = true;
				result.cmd = data.cmd;
				result.data = d.getTextAccounts();
			}
			if (data.cmd.equals("nextAccs")) {
				boolean success = false;
				try {
					success = d.importAccsFrom(data.data.get(0));
				} catch (Exception e) {
					e.printStackTrace();
				}
				result = new Sendable(data.name);
				result.isAnswer = true;
				result.cmd = data.cmd;
				result.data.add(String.valueOf(success));
			}
			if (data.cmd.equals("replaceAcc")) {
				boolean success = false;
				try {
					success = d.replaceAcc(data.data.get(0), data.data.get(1));
				} catch (Exception e) {
					e.printStackTrace();
				}
				result = new Sendable(data.name);
				result.isAnswer = true;
				result.cmd = data.cmd;
				result.data.add(String.valueOf(success));
			}
			if (data.cmd.equals("replaceBanned")) {
				boolean success = false;
				try {
					success = d.replaceBanned(data.data.get(0));
				} catch (Exception e) {
					e.printStackTrace();
				}
				result = new Sendable(data.name);
				result.isAnswer = true;
				result.cmd = data.cmd;
				result.data.add(String.valueOf(success));
			}
			if (data.cmd.equals("nextProxies")) {
				boolean success = false;
				try {
					success = d.nextProxies(data.data.get(0));
				} catch (Exception e) {
					e.printStackTrace();
				}
				result = new Sendable(data.name);
				result.isAnswer = true;
				result.cmd = data.cmd;
				result.data.add(String.valueOf(success));
			}
			if (data.cmd.equals("rename")) {
				boolean success = false;
				try {
					name = data.data.get(0);
					saveName();
					success = true;
					System.out.println("saving new name: " + name);
				} catch (Exception e) {
					e.printStackTrace();
				}
				result = new Sendable(data.name);
				result.isAnswer = true;
				result.cmd = data.cmd;
				result.data.add(String.valueOf(success));
				sendSendable(result);
				result = null;
				die();
				d.connect(); //reconnect with new name
			}
			if (data.cmd.equals("replaceClient")) {
				result = new Sendable(data.name);
				result.isAnswer = true;
				result.cmd = data.cmd;
				result.data.add("true");
				sendSendable(result);
				//send sendable since dreamling will die after calling replaceClient()
				d.replaceClient(data.data.get(0), Boolean.parseBoolean(data.data.get(1)));
				result = null;
			}
			if (data.cmd.equals("replaceScripts")) {
				result = new Sendable(data.name);
				result.isAnswer = true;
				boolean success = d.replaceScripts(data.data.get(0));
				result.cmd = data.cmd;
				result.data.add(String.valueOf(success));
			}
			if (data.cmd.equals("startLauncher")) {
				result = new Sendable(data.name);
				result.isAnswer = true;
				boolean success = d.startLauncher();
				result.cmd = data.cmd;
				result.data.add(String.valueOf(success));
			}
			if (data.cmd.equals("replaceXY")) {
				result = new Sendable(data.name);
				result.isAnswer = true;
				boolean success = d.replaceXY(data.data.get(0), data.data.get(1));
				result.cmd = data.cmd;
				result.data.add(String.valueOf(success));
			}
			if (data.cmd.equals("runBash")) {
				result = new Sendable(data.name);
				result.isAnswer = true;
				boolean success = d.runBash(data.data.get(0));
				result.cmd = data.cmd;
				result.data.add(String.valueOf(success));
			}*/
		}
		return result;
	}
}	
