package sockets;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class Sendable implements Serializable {

	private static final long serialVersionUID = 6817391387478331647L;
	public String name;
	public boolean isAnswer;
	public String cmd;
	public List<String> data;
	
	public Sendable() {
		this(generateId());
	}
	
	public Sendable(String name) {
		this.name = name;
		this.isAnswer = false;
		this.cmd = "";
		this.data = new ArrayList<String>();
	}
	
	public static String generateId() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm");
		long currentTime = Long.parseLong(sdf2.format(cal.getTime()));
		int rnd = (int) (Math.random() * 10000000);
		return "Sendable" + currentTime + "-" + rnd;
	}
}
