package dreamling.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import org.apache.tomcat.util.http.fileupload.FileUtils;

import GUI.logging.Interceptor;
import GUI.windowTools.ControlWindow;
import GUI.windowTools.ExitWindowListener;
import dreamling.objects.Account;

public class Dreamling implements Serializable {
	
	private static final long serialVersionUID = 3221298114516647966L;
	public static final String USERNAME = "bonbom1";
	public static final String PASSWORD = "tomppa500";
	public static final int LOGGER_WIDTH = 100;
	public static final int LOGGER_HEIGHT = 20;
	
	public DreamlingSocket ds;
	public ControlWindow cw;
	public List<Account> accounts;
	public JLabel clockLabel;
	public ClockThread ct;
	
	public Interceptor outInterceptor;
	public Interceptor errInterceptor;
	
	public static void main(String[] args) {
		new Dreamling();
	}
	
	public Dreamling() {
		accounts = new ArrayList<Account>();
		
		cw = new ControlWindow("Dreamling");
		cw.setResizable(false);
		cw.setDefaultCloseOperation(ControlWindow.EXIT_ON_CLOSE);
		cw.addWindowListener(new ExitWindowListener(new OnExitSaver(this)));
		
		clockLabel = cw.addLabel(cw.createLabel(" Initializing "));
		
		final JButton btnOutLog = cw.addButton(cw.createButton("Toggle Out Log"));
		final JButton btnErrLog = cw.addButton(cw.createButton("Toggle Error Log"));

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd");
		int date = Integer.parseInt(sdf3.format(cal.getTime()));
		
		final ControlWindow logger = new ControlWindow("Output Console");
		logger.setVisible(false);
		logger.setResizable(false);
		JTextArea console = logger.createTextArea(false, LOGGER_HEIGHT, LOGGER_WIDTH);
		console.setLineWrap(true);
		console.setWrapStyleWord(true);
		logger.addTextArea(console);
		outInterceptor = new Interceptor(System.out, new File("output" + date + ".txt"), console);
		System.setOut(outInterceptor);
		System.out.println("Starting Dreamling.");
		
		final ControlWindow error = new ControlWindow("Error Console");
		error.setVisible(false);
		error.setResizable(false);
		JTextArea console2 = error.createTextArea(false, LOGGER_HEIGHT, LOGGER_WIDTH);
		console2.setLineWrap(true);
		console2.setWrapStyleWord(true);
		error.addTextArea(console2);
		errInterceptor = new Interceptor(System.err, new File("error" + date + ".txt"), console2);
		System.setErr(errInterceptor);
		System.err.println("Starting Dreamling. (not an error)");
		
		cw.nextRow();
		cw.nextRow();
		
		//buttons have to run in new Threads so swing can still update
		btnOutLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							if (logger != null) {
								if (logger.isVisible()) {
									logger.setVisible(false);
								} else {
									logger.setLocation(cw.getX(), cw.getY() + cw.getHeight());
									logger.setVisible(true);
									logger.pack();
								}
							}
						}
					});
					t.start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		btnErrLog.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							if (error != null) {
								if (error.isVisible()) {
									error.setVisible(false);
								} else {
									error.setLocation(cw.getX() + logger.getWidth(), cw.getY() + cw.getHeight());
									error.setVisible(true);
									error.pack();
								}
							}
						}
					});
					t.start();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
			}
		});
		
		ct = ClockThread.init(this, console);
		ClockListener.init(this);
		outInterceptor.setCommandExecutor(ct);
		ds = connect();
	}
	
	public DreamlingSocket connect() {
		return new DreamlingSocket(this);
	}
	
	public void disconnect() {
		if (ds != null) {
			/*try { //probably not necessary since hive will auto-dequeue that dreamling on disconnect
				Sendable disconnectMessage = new Sendable();
				disconnectMessage.cmd = "disconnect";
				disconnectMessage.data.add(ds.dreamlingName);
				ds.sendSendable(disconnectMessage);
			} catch (Exception e) {
				e.printStackTrace();
			}*/
			ds.die();
		}
	}
	
	public synchronized void update() {
		if (ct.isAlive()) {
			if (!clockLabel.getText().contains("alive")) {
				clockLabel.setText("<html><body>&nbsp; Clock: <font color=\"blue\">alive</font>&nbsp;</body></html>");
			}
		} else {
			if (!clockLabel.getText().contains("dead")) {
				clockLabel.setText("<html><body>&nbsp; Clock: <font color=\"red\">dead</font>&nbsp;</body></html>");
				System.err.println("CLOCK DIED!!!!!");
			}
		}
		cw.pack();
	}
	
	public synchronized void replaceClient(String url, boolean deleteConfigs) {
		//generate name
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm");
		long currentTime = Long.parseLong(sdf2.format(cal.getTime()));
		int rnd = (int) (Math.random() * 10000);
		String name = "dreamling" + currentTime + "-" + rnd + ".jar";
		
		//download new client
		System.out.println("trying to download new client from: " + url);
		try {
			trustNonVerifiedSSL();
			URL website = new URL(url);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(name);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
			System.out.println("downloaded file.");
		} catch (Exception e) {
			System.out.println("failed to download file: ");
			System.out.println(e.toString());
		}
		
		if (deleteConfigs) {
			//willSave = false;
			new File("account_infos.bin").delete();
			new File("dreamling_id.bin").delete();
		}
		
		DreamlingUpdater.delayedStart(name);
		
		cw.dispatchEvent(new WindowEvent(cw, WindowEvent.WINDOW_CLOSING));
	}
	
	//works only for linux for now
	public boolean replaceScripts(String urls) {
		System.out.println("replacing scripts from: " + urls);
		String scriptFolder = "/root/DreamBot/Scripts/";
		try {
			try {
				FileUtils.cleanDirectory(new File(scriptFolder));
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.out.println("trying to download new scripts from: " + urls);
			String[] urlsArray = urls.split("!");
			try {
				trustNonVerifiedSSL();
				for (String url : urlsArray) {
					String filename = url.split("/")[url.split("/").length - 1];
					URL website = new URL(url);
					ReadableByteChannel rbc = Channels.newChannel(website.openStream());
					FileOutputStream fos = new FileOutputStream(scriptFolder + filename);
					fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
					fos.close();
					System.out.println("downloaded file " + filename);
				}
			} catch (Exception e) {
				System.err.println("error downloading file from urls: " + urls);
				e.printStackTrace();
				return false;
			}
			return true;
		} catch (Exception e) {
			System.err.println("error: " + e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	private void trustNonVerifiedSSL() throws Exception {
		// Create a new trust manager that trust all certificates
		TrustManager[] trustAllCerts = new TrustManager[]{
		    new X509TrustManager() {
		        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		            return null;
		        }
		        public void checkClientTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		        public void checkServerTrusted(
		            java.security.cert.X509Certificate[] certs, String authType) {
		        }
		    }
		};

		// Activate the new trust manager
	    SSLContext sc = SSLContext.getInstance("SSL");
	    sc.init(null, trustAllCerts, new java.security.SecureRandom());
	    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		    
		// Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
	}
	
	public boolean startLauncher() {
		try {
			Runtime rt = Runtime.getRuntime();
			String user_path = System.getProperty("user.home");
			String cmd = "java -jar " + user_path + "/DreamBotLauncher.jar";
			Process pr = rt.exec(cmd);
			Thread.sleep(60000);
			pr.destroyForcibly();
			return true;
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
		return false;
	}	
	
	public boolean runBash(String url) {
		System.out.println("downloading bash script from: " + url);
		try {
			trustNonVerifiedSSL();
			String filename = url.split("/")[url.split("/").length - 1];
			URL website = new URL(url);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			FileOutputStream fos = new FileOutputStream(filename);
			fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
			fos.close();
			System.out.println("downloaded file " + filename);
			
			Runtime rt = Runtime.getRuntime();
			//String current_path = System.getProperty("user.dir");
			String cmd = "./" + filename;
			Process pr = rt.exec(cmd);
			try {
				if (pr.exitValue() != 0) { //if error happened
					System.out.println(Account.convertStreamToString(pr.getErrorStream())); //blocks until process finishes.
				}
			} catch (Exception e) {}
			
			Account.sniffLogs(pr);
			//pr.waitFor();
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
}









