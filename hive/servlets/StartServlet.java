package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import others.HtmlTools;
import others.ServletSocket;

public class StartServlet extends HttpServlet {
	private static final long serialVersionUID = -29775284919628189L;

	@Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();

		try {
			//generate html page
			String result = HtmlTools.getBlankHtmlTemplate();
			result = HtmlTools.insertIntoHtmlSection(result, HtmlTools.META_SECTION, "<script type=\"text/javascript\" src=\"./assets/js/start.js\"></script>");
			result = HtmlTools.insertNavigationButtonHtml(result, "Dreamlings", "/start", "dreamlings_btn");
			result = HtmlTools.insertNavigationButtonHtml(result, "RS-Accounts", "/accgen", "accgen_btn");
			result = HtmlTools.insertNavigationButtonHtml(result, "Logout", "/logout", "logout_btn");
			
			result = HtmlTools.addTitle(result, "Dream Hive\nDreamlings");
			result = HtmlTools.insertSidebarButtonHtml(result, "showAll", "Show all Dreamlings");
			
			ServletSocket c = new ServletSocket();
			List<String> tabs = c.getTabs();
			for (String line : tabs) {
				result = HtmlTools.insertSidebarButtonHtml(result, line, line);
			}
			c.die();
			
			String staticContent = "<div class=\"hiveTools\">"
					+ "<div id=\"hideBreak\">Hide Break-Info <input type=\"checkbox\" id=\"hideBreakBox\"></div><br />"
					+ "<button id=\"startAllDreamlings\">Start ALL Dreamlings</button>"
					+ "<button id=\"killAllDreamlings\">Kill ALL Dreamlings</button>"
					+ "<button id=\"replaceClient\">Upload new Dreamling</button>"
					+ "<button id=\"replaceScripts\">Upload new Scripts</button>"
					+ "<button id=\"runBash\">Upload bash script</button>"
					+ "</div>";
			
			result = HtmlTools.insertContentHtml(result, "Hive Tools:", staticContent, true);
			
			
			String dreamlingContent = "<div id=\"accounts\">No Dreamling selected</div>"
					+ "<div id=\"dialog\"></div>";
			
			result = HtmlTools.insertContentHtml(result, "", dreamlingContent, true);
			
			out.println(result);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (out != null) out.close();
		}
		
	}
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
}
