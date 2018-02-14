package servlets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import others.ServletSocket;

@MultipartConfig //The @MultipartConfig annotation indicates that the servlet expects 
//requests to be made using the multipart/form-data MIME type.
public class DataServlet extends HttpServlet {
	
	public static final String DREAMLING_SEPARATOR = "DREAMLING_SEPARATOR";
	private static final long serialVersionUID = -5969551349385367249L;
	public static final String UPLOAD_PATH = "C:/Program Files/Apache Software Foundation/Tomcat/";
	
	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		String selected = request.getParameter("dreamling");
		if (selected != null && !selected.isEmpty()) {
			String[] dreamlings = selected.split(DREAMLING_SEPARATOR);
			String result = getContents(dreamlings);
			System.out.println(result);
			out.print(result);
		}
		
		out.close();
	}
	
	//creates new thread for each request
	public String getContents(String[] dreamlings) {
		String[] results = new String[dreamlings.length];
		List<Thread> threads = new ArrayList<Thread>();
		int pos = 0;
		for (String dreamling : dreamlings) {
			final int threadPos = pos;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					String result = "";
					ServletSocket c = new ServletSocket();
					List<String> accs = c.getContent(dreamling); //including titles as first line
					c.die();
					for (String line : accs) {
						result += line + "!";
					}
					result = result.substring(0, Math.max(result.length() - 1, 0)); //remove last "!"
					results[threadPos] = result;
				}
			});
			threads.add(t);
			t.start();
			pos++;
		}
		try {
			for (Thread t : threads) {
				t.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		String result = "";
		for (String r : results) {
			result += r + DREAMLING_SEPARATOR;
		}
		result = result.substring(0, Math.max(result.length() - DREAMLING_SEPARATOR.length(), 0)); //remove last DREAMLING_SEPARATOR
		return result;
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		String pk = request.getParameter("pk");
		if (pk != null && !pk.isEmpty()) {
			System.out.println("pk: " + pk);
			String accId = pk.split(":")[0];
			String attrName = pk.split(":")[1];
			String dreamling = pk.split(":")[2];
			ServletSocket c = new ServletSocket();
			boolean success = c.sendAction(dreamling, accId, attrName, request.getParameter("value"));
			c.die();
			if (!success) {
				System.out.println("Error: Value update not successful.");
				response.setStatus(400);
				PrintWriter out = response.getWriter();
				out.println("Error: Value update not successful.");
				out.close();
			}
		} 
		String nextAccs = request.getParameter("nextAccs");
		if (nextAccs != null && !nextAccs.isEmpty()) {
			System.out.println("nextAccs: " + nextAccs);
			ServletSocket c = new ServletSocket();
			boolean success = c.sendAccs(request.getParameter("dreamling"), nextAccs);
			c.die();
			if (!success) {
				System.out.println("Error: ACCs update not successful.");
			}
		}
		String replaceAcc = request.getParameter("replaceAcc");
		if (replaceAcc != null && !replaceAcc.isEmpty()) {
			System.out.println("replacing account: " + replaceAcc);
			String dreamling = request.getParameter("dreamling");
			String credentials = request.getParameter("credentials");
			ServletSocket c = new ServletSocket();
			boolean success = c.replaceAcc(dreamling, replaceAcc, credentials);
			c.die();
			if (!success) {
				System.out.println("Error: Replacing Accounts not successful.");
			}
		}
		String replaceBanned = request.getParameter("replaceBanned");
		if (replaceBanned != null && !replaceBanned.isEmpty()) {
			System.out.println("replaceBanned: " + replaceBanned);
			ServletSocket c = new ServletSocket();
			boolean success = c.replaceBanned(request.getParameter("dreamling"), replaceBanned);
			c.die();
			if (!success) {
				System.out.println("Error: Replacing Banned not successful.");
			}
		}
		String nextProxies = request.getParameter("nextProxies");
		if (nextProxies != null && !nextProxies.isEmpty()) {
			System.out.println("nextProxies: " + nextProxies);
			ServletSocket c = new ServletSocket();
			boolean success = c.sendProxies(request.getParameter("dreamling"), nextProxies);
			c.die();
			if (!success) {
				System.out.println("Error: Proxy update not successful.");
			}
		}
		String newName = request.getParameter("newName");
		if (newName != null && !newName.isEmpty()) {
			System.out.println("rename to: " + newName);
			ServletSocket c = new ServletSocket();
			boolean success = c.renameDreamling(request.getParameter("dreamling"), newName);
			c.die();
			if (!success) {
				System.out.println("Error: Dreamling rename not successful.");
			}
		}
		String replaceClient = request.getParameter("replaceClient");
		if (replaceClient != null && !replaceClient.isEmpty()) {
			System.out.println("replaceClient: " + replaceClient);
			ServletSocket c = new ServletSocket();
			boolean success = c.replaceClient(replaceClient, request.getParameter("url"), request.getParameter("deleteConfigs"));
			c.die();
			if (!success) {
				System.out.println("Error: Client replace not successful.");
			}
		}
		String startLauncher = request.getParameter("startLauncher");
		if (startLauncher != null && !startLauncher.isEmpty()) {
			System.out.println("startLauncher: " + startLauncher);
			ServletSocket c = new ServletSocket();
			boolean success = c.startLauncher(request.getParameter("dreamling"));
			c.die();
			if (!success) {
				System.out.println("Error: startLauncher not successful.");
			}
		}
		String replaceXY = request.getParameter("replaceXY");
		if (replaceXY != null && !replaceXY.isEmpty()) {
			String x = request.getParameter("X");
			String y = request.getParameter("Y");
			System.out.println("replaceXY: " + x + " with " + y);
			ServletSocket c = new ServletSocket();
			boolean success = c.replaceXY(replaceXY, x, y);
			c.die();
			if (!success) {
				System.out.println("Error: replaceXY not successful: " + x + " with " + y);
			}
		}
		
		
		try {
			final Part filePart1 = request.getPart("file1");
			if (filePart1 != null) {
				writePart(filePart1);
			    System.out.println("uploaded");
			    ServletSocket c = new ServletSocket();
			    List<String> tabs = c.getTabs();
			    List<Boolean> successes = new ArrayList<Boolean>();
			    String url = "https://" + getIp() + "/uploaded/tempfile";
			    for (String name : tabs) {
				    boolean success = c.replaceClient(name, url, "true");
			    	successes.add(success);
			    }
			    c.die();
				int all = 0;
			    int success = 0;
			    for (Boolean result : successes) {
			    	all++;
			    	if (result.booleanValue()) success++;
			    }
			    System.out.println("replacing Clients: " + success + "/" + all + " successfully replaced.");
			    try {
					Thread.sleep(15000); //wait 15s for clients to restart.
				} catch (Exception e) {}
			    response.sendRedirect("/start");
			}
		} catch (Exception e) {}

		try {
			final Part filePart2 = request.getPart("file2");
			if (filePart2 != null) {
				writePart(filePart2);
			    System.out.println("uploaded file");
			    ServletSocket c = new ServletSocket();
			    List<String> tabs = c.getTabs();
			    List<Boolean> successes = new ArrayList<Boolean>();
			    String url = "https://" + getIp() + "/uploaded/tempfile";
			    for (String name : tabs) {
				    boolean success = c.replaceClient(name, url, "false");
			    	successes.add(success);
			    }
			    c.die();
				int all = 0;
			    int success = 0;
			    for (Boolean result : successes) {
			    	all++;
			    	if (result.booleanValue()) success++;
			    }
			    System.out.println("replacing Clients: " + success + "/" + all + " successfully replaced.");
			    try {
					Thread.sleep(15000); //wait 15s for clients to restart.
				} catch (Exception e) {}
			    response.sendRedirect("/start");
			}
		} catch (Exception e) {}
		
		//uploaded scripts
		try {
			final Part filePart3 = request.getPart("file3"); //get one script to see if scripts are being uploaded
			if (filePart3 != null) {
				final Collection<Part> fileParts = request.getParts();
				if (fileParts != null && !fileParts.isEmpty()) {
					String urls = ""; //separated by "!"
					final String ip = getIp();
					for (Part filePart : fileParts) {
						if (filePart != null) {
							writePart(filePart, filePart.getSubmittedFileName());
							urls += "https://" + ip + "/uploaded/" + filePart.getSubmittedFileName() + "!";
						}
					}
					if (urls.length() > 0) {
						urls = urls.substring(0, urls.length() - 1); //remove last "!"
					}
				    System.out.println("uploaded " + fileParts.size() + " files");
		
				    ServletSocket c = new ServletSocket();
				    List<String> tabs = c.getTabs();
				    List<Boolean> successes = new ArrayList<Boolean>();
				    for (String name : tabs) {
					    boolean success = c.replaceScripts(name, urls);
				    	successes.add(success);
				    }
				    c.die();
					int all = 0;
				    int success = 0;
				    for (Boolean result : successes) {
				    	all++;
				    	if (result.booleanValue()) success++;
				    }
				    System.out.println("replacing Scripts: " + success + "/" + all + " successfully replaced.");
				    response.sendRedirect("/start");
				}
			}
		} catch (Exception e) {}

		//uploaded bash script
		try {
			final Part filePart4 = request.getPart("file4"); //get one script to see if scripts are being uploaded
			if (filePart4 != null) {
				writePart(filePart4);
			    System.out.println("uploaded file");
			    ServletSocket c = new ServletSocket();
			    List<String> tabs = c.getTabs();
			    List<Boolean> successes = new ArrayList<Boolean>();
			    String url = "https://" + getIp() + "/uploaded/tempfile";
			    for (String name : tabs) {
				    boolean success = c.runBash(name, url);
			    	successes.add(success);
			    }
			    c.die();
				int all = 0;
			    int success = 0;
			    for (Boolean result : successes) {
			    	all++;
			    	if (result.booleanValue()) success++;
			    }
			    System.out.println("running bash: " + success + "/" + all + " successfully replaced.");
			    try {
					Thread.sleep(15000); //wait 15s for clients to restart.
				} catch (Exception e) {}
			    response.sendRedirect("/start");
			}
		} catch (Exception e) {}
	}
	
	public static void writePart(Part part) {
		writePart(part, "tempfile");
	}
	
	public static void writePart(Part part, String filename) {
		OutputStream out = null;
        InputStream filecontent = null;
        System.out.println("writing file: " + filename);
        try {
        	if (!new File(UPLOAD_PATH + "webapps/ROOT/uploaded/" + filename).exists()) {
        		new File(UPLOAD_PATH + "webapps/ROOT/uploaded").mkdirs();
				new File(UPLOAD_PATH + "webapps/ROOT/uploaded/" + filename).createNewFile();
			}
        	out = new FileOutputStream(new File(UPLOAD_PATH + "webapps/ROOT/uploaded/" + filename));
            filecontent = part.getInputStream();

            int read = 0;
            final byte[] bytes = new byte[1024];

            while ((read = filecontent.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            
        } catch (Exception e) {
        	e.printStackTrace();
        } finally {
        	try {
	            if (out != null) {
	                out.close();
	            }
	            if (filecontent != null) {
	                filecontent.close();
	            }
        	} catch (Exception e1) {
        		e1.printStackTrace();
        	}
        }
	}
	
	public static String getIp() {
		BufferedReader in = null;
		String ip = null;
		try {
			URL whatismyip = new URL("http://checkip.amazonaws.com");
			in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
			ip = in.readLine(); //you get the IP as a String
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return ip;
	}
}
