package others;

import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import dreamling.client.ClockThread;
import dreamling.client.Dreamling;

public class Account implements Serializable {
	
	private static final long serialVersionUID = -4269905500299478313L;
	private Map<String, String> values;
	
	public Account() {
		values = new LinkedHashMap<String, String>();
		
		final String AB = "0123456789abcdefghijklmnopqrstuvwxyz";
		SecureRandom rnd = new SecureRandom();
		StringBuilder sb = new StringBuilder(20);
		for( int i = 0; i < 20; i++ ) {
			sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
		}
		values.put("id", sb.toString()); //id should never be changed
		values.put("name", "");
		values.put("email", "");
		values.put("password", "");
		values.put("script", "");
		values.put("proxy", "");
		values.put("world", "301");
		values.put("params", "");
		values.put("pid", "0");
		values.put("state", "offline");
		values.put("stateMinutes", String.valueOf(new Date().getTime()));
		
	}
	
	public String get(String key) {
		if (key != null && key.equalsIgnoreCase("stateMinutes")) {
			return String.valueOf(getStateMinutes());
		}
		return values.get(key);
	}
	
	public void put(String key, String value) {
		if (key != null && key.equalsIgnoreCase("state")) {
			if (!value.equalsIgnoreCase(values.get("state"))) {
				values.put("state", value);
				values.put("stateMinutes", String.valueOf(new Date().getTime()));
			}
		} else {
			values.put(key, value);
		}
	}
	
	public long getStateMinutes() {
		String lastStateUpdate = values.get("stateMinutes");
		if (lastStateUpdate != null && !lastStateUpdate.isEmpty()) {
			Date then = new Date(Long.parseLong(lastStateUpdate));
			long diff = new Date().getTime() - then.getTime();
			return diff / (60 * 1000);
		}
		return -1;
	}
	
	public Set<String> getTitles() {
		return (Set<String>) values.keySet();
	}
	
	public List<String> getValuesForTitles(Set<String> titles) {
		List<String> result = new ArrayList<String>();
		if (titles != null) {
			for (String title : titles) {
				result.add(get(title));
			}
		}
		return result;
	}
	
	public String generateCmdParams() {
		String[] credentials = {"defaultUsername", "defaultPassword"};
		try {
			credentials = Files.readAllLines(Paths.get("dreambotVIP.txt"), StandardCharsets.UTF_8).get(0).split(":");
		} catch (Exception e) {e.printStackTrace();}
		final String username = credentials[0];
		final String password = credentials[1];
		String result = " -username " + username + " -password " + password
				+ " -script " + get("script")
				+ " -world " + get("world")
				+ " -title " + get("name")
				+ " -params " + ClockThread.CALLBACK + get("name") 
					+ "&email=" + get("email")
					+ "&password=" + get("password")
					+ "&" + get("params");
		String proxy = get("proxy");
		if (proxy != null && !proxy.isEmpty() && proxy.contains(":")) {
			result += " -proxyHostArg " + proxy.split(":")[0]
					+ " -proxyPortArg " + proxy.split(":")[1];
		}
		return result;
	}
	
}
