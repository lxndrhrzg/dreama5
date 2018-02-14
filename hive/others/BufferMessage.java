package others;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import sockets.Sendable;

public class BufferMessage {
	
	public Sendable sendable;
	public int timeAdded;
	
	public BufferMessage(Sendable sendable) {
		this.sendable = sendable;
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf2 = new SimpleDateFormat("HHmm");
		this.timeAdded = Integer.parseInt(sdf2.format(cal.getTime()));
	}
}