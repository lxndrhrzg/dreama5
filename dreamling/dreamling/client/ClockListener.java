package dreamling.client;

public class ClockListener extends Thread {
	
	Dreamling c;

	public static void init(Dreamling c) {
		ClockListener cl = new ClockListener();
		cl.c = c;
		cl.start();
	}
	
	@Override
	public void run() {
		super.run();
		while (true) {
			try {
				c.update();
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
