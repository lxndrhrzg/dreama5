package servlets;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import others.HtmlTools;
import others.ServletSocket;

@MultipartConfig //The @MultipartConfig annotation indicates that the servlet expects 
//requests to made using the multipart/form-data MIME type.
public class AccGenServlet extends HttpServlet {

	private static final long serialVersionUID = -7581891463122663317L;

	public static final String DREAMLIST = "dreamlist.txt";
	
	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		try {
			//generate html page
			String result = HtmlTools.getBlankHtmlTemplate();
			result = HtmlTools.insertIntoHtmlSection(result, HtmlTools.META_SECTION, "<script type=\"text/javascript\" src=\"./assets/js/accgen.js\"></script>");
			result = HtmlTools.insertNavigationButtonHtml(result, "Dreamlings", "/start", "dreamlings_btn");
			result = HtmlTools.insertNavigationButtonHtml(result, "RS-Accounts", "/accgen", "accgen_btn");
			result = HtmlTools.insertNavigationButtonHtml(result, "Logout", "/logout", "logout_btn");
			
			result = HtmlTools.addTitle(result, "Dream Hive\nRS-Accounts");

			result = HtmlTools.insertSidebarButtonHtml(result, "viewAccounts", "View Dreamlist");
			result = HtmlTools.insertSidebarButtonHtml(result, "uploadAccounts", "Upload Dreamlist");
			
			ServletSocket c = new ServletSocket();
			List<String> accGens = c.getAccGens();
			for (String line : accGens) {
				result = HtmlTools.insertSidebarButtonHtml(result, line, line);
			}
			c.die();
			
			String siteContent = "";
			if (accGens.isEmpty()) {
				siteContent += "<div class=\"horiz-center\">No Account-Generators online.</div><br />";
			}
			
			siteContent += "<div id=\"dreamlist\" class=\"horiz-center\" style=\"overflow:auto\">"
					+ "<span class=\"contentTitle\">RS-Accounts</span><br /><br />"
					+ "<table name=\"accTable\" id=\"accTable\" class=\"mbr-article-custom accTable sortable horiz-center\">"
					+ "<tr>"
					+ "<td class=\"tableTitle\"><b>Username</b></td>"
					+ "<td class=\"tableTitle\"><b>Email</b></td>"
					+ "<td class=\"tableTitle\"><b>Password</b></td>"
					+ "</tr>";
			List<String> dreamlist = getAccounts();
			for (String line : dreamlist) {
				String[] data = line.split(":");
				if (data.length == 3) {
					siteContent += "<tr>"
							+ "<td class=\"tableCell\">" + data[0] + "</td>"
							+ "<td class=\"tableCell\">" + data[1] + "</td>"
							+ "<td class=\"tableCell\">" + data[2] + "</td>"
							+ "</tr>";
				} else if (data.length == 2) {
					siteContent += "<tr>"
							+ "<td class=\"tableCell\"></td>"
							+ "<td class=\"tableCell\">" + data[0] + "</td>"
							+ "<td class=\"tableCell\">" + data[1] + "</td>"
							+ "</tr>";
				}
			}
			siteContent += "</table></div>";
			
			result = HtmlTools.insertIntoHtmlSection(result, HtmlTools.CONTENT_SECTION, siteContent);
			
			siteContent = "<div id=\"uploadSection\" class=\"horiz-center\" style=\"display: none;\">"
					+ "<span class=\"contentTitle\">Upload Dreamlist</span><br /><br />"
					+ "<form method=\"POST\" action=\"accgen\" enctype=\"multipart/form-data\">"
					+ "<input type=\"file\" name=\"file\" id=\"file\" class=\"horiz-center\"><br />"
					+ "<input type=\"submit\" id=\"upload\" value=\"Upload\">"
					+ "</form></div>";
			result = HtmlTools.insertIntoHtmlSection(result, HtmlTools.CONTENT_SECTION, siteContent);
			
			out.println(result);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (out != null) out.close();
		}
		
	}
	
	public static void writePart(Part part) {
		OutputStream out = null;
        InputStream filecontent = null;
        try {
        	if (!new File("download.txt").exists()) {
				new File("download.txt").createNewFile();
			}
        	out = new FileOutputStream(new File("download.txt"));
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
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		System.out.println("trying upload");
		final Part filePart = request.getPart("file");
		if (filePart != null) {
			writePart(filePart);
		    
		    
		    BufferedReader br = null;
		    File f = null;
		    PrintWriter out = null;
		    if (!new File(DREAMLIST).exists()) {
		    	new File(DREAMLIST).createNewFile();
		    }
		    try {
	    	    out = new PrintWriter(new BufferedWriter(new FileWriter(DREAMLIST, true)));
		    	f = new File("download.txt");
		    	br = new BufferedReader(new FileReader(f));
		    	String line = br.readLine();
		        while (line != null) {
		        	out.println(line);
		            line = br.readLine();
		        }
		    } catch (Exception e) {
		        e.printStackTrace();
		    } finally {
		    	if (out != null) {
		    		out.close();
		    	}
		        if (br != null) {
		            br.close();
		        }
		        if (f != null) {
		        	f.delete();
		        }
		    }
		} else {
			System.out.println("error: no file selected.");
		}
		doGet(request, response);
	}
	
	public List<String> getAccounts() {
		List<String> result = new ArrayList<String>();
		if (new File("dreamlist.txt").exists()) {
			try {
				BufferedReader br = new BufferedReader(new FileReader("dreamlist.txt"));
				String line = br.readLine();
				while (line != null) {
					result.add(line);
					line = br.readLine();
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
