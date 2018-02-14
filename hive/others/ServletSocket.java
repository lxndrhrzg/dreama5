package others;

import java.util.ArrayList;
import java.util.List;

import sockets.DreamClient;
import sockets.DreamServer;
import sockets.Sendable;

public class ServletSocket extends DreamClient {
	
	//should be killed after usage with die(). otherwise unnecessary keepalive.
	
	public ServletSocket() {
		super(DreamServer.HOST, DreamServer.TCP);
	}
	
	@Override
	public Sendable getRegistration() {
		/*Sendable result = new Sendable();
		result.cmd = "reader";
		return result;*/
		return null;
	}
	
	public List<String> getTabs() {
		System.out.println("getting tabs");
		Sendable request = new Sendable();
		request.cmd = "tabs";
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data;
		}
		return new ArrayList<String>();
	}
	
	//including titles as first line
	public List<String> getContent(String name) {
		System.out.println("getting content");
		Sendable request = new Sendable();
		request.cmd = "requestDreamling";
		request.data.add(name);
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data;
		}
		return new ArrayList<String>();
	}
	
	public List<String> getAccGens() {
		System.out.println("getting accgens");
		Sendable request = new Sendable();
		request.cmd = "accgens";
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data;
		}
		return new ArrayList<String>();
	}
	
	//actions and values may be several ones split with DreamServer.VALUE_SEPARATOR
	public boolean sendAction(String dreamling, String accId, String attrNames, String values) {
		Sendable request = new Sendable();
		request.cmd = "action";
		request.data.add(dreamling);
		request.data.add(accId);
		request.data.add(attrNames);
		request.data.add(values);
		System.out.println("sending action request to " + dreamling + " " + accId + " " + attrNames + " " + values);
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data.get(0).equals("true");
		}
		return false;
	}
	
	public boolean sendAccs(String name, String accs) {
		Sendable request = new Sendable(name);
		request.cmd = "nextAccs";
		request.data.add(accs);
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data.get(0).equals("true");
		}
		return false;
	}
	
	public boolean replaceAcc(String name, String row, String credentials) {
		Sendable request = new Sendable(name);
		request.cmd = "replaceAcc";
		request.data.add(credentials);
		request.data.add(row);
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data.get(0).equals("true");
		}
		return false;
	}
	
	public boolean replaceBanned(String name, String accs) {
		Sendable request = new Sendable(name);
		request.cmd = "replaceBanned";
		request.data.add(accs);
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data.get(0).equals("true");
		}
		return false;
	}
	
	public boolean sendProxies(String name, String proxies) {
		Sendable request = new Sendable(name);
		request.cmd = "nextProxies";
		request.data.add(proxies);
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data.get(0).equals("true");
		}
		return false;
	}
	
	public boolean renameDreamling(String name, String newName) {
		Sendable request = new Sendable();
		request.cmd = "rename";
		request.data.add(name);
		request.data.add(newName);
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data.get(0).equals("true");
		}
		return false;
	}
	
	public boolean replaceClient(String name, String url, String deleteConfigs) {
		Sendable request = new Sendable(name);
		request.cmd = "replaceClient";
		request.data.add(url);
		request.data.add(deleteConfigs);
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data.get(0).equals("true");
		}
		return false;
	}
	
	public boolean startLauncher(String name) {
		Sendable request = new Sendable(name);
		request.cmd = "startLauncher";
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data.get(0).equals("true");
		}
		return false;
	}

	public boolean replaceXY(String name, String x, String y) {
		Sendable request = new Sendable(name);
		request.cmd = "replaceXY";
		request.data.add(x);
		request.data.add(y);
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data.get(0).equals("true");
		}
		return false;
	}
	
	public boolean replaceScripts(String name, String urls) {
		Sendable request = new Sendable(name);
		request.cmd = "replaceScripts";
		request.data.add(urls);
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data.get(0).equals("true");
		}
		return false;
	}
	
	public boolean runBash(String name, String url) {
		Sendable request = new Sendable(name);
		request.cmd = "runBash";
		request.data.add(url);
		Sendable answer = sendSendable(request, true);
		if (answer != null) {
			return answer.data.get(0).equals("true");
		}
		return false;
	}
	
	public static void main(String[] args) {
		/*ServletSocket sc = new ServletSocket();
		List<String> res = sc.getContent("Sendable201702232027-7682");
		for (String line : res) {
			System.out.println(line);
		}
		System.out.println("done");*/
	}
}	