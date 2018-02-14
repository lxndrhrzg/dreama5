package dreamling.client;

import java.io.File;

public class DelayedStart {

	public static void main(String[] args) {
		try {
			Thread.sleep(5000);
		} catch (Exception e) {}
		if (args.length >= 3) {
			if (args[2].equalsIgnoreCase("true")) {
				try {
					new File("account_infos.bin").delete();
					new File("dreamling_id.bin").delete();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				Runtime rt = Runtime.getRuntime();
				String cmd = "java -jar " + System.getProperty("user.dir") + File.separator + args[1];
				rt.exec(cmd);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
