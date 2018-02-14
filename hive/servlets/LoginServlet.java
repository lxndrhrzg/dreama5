package servlets;
import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import others.HtmlTools;

 
public class LoginServlet extends HttpServlet {
	
	private static final long serialVersionUID = 3166640329206731298L;

	// The doGet() runs once per HTTP GET request to this servlet.
   @Override
   public void doGet(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
		response.setContentType("text/html;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		try {
			//generate html page
			String result = HtmlTools.getBlankHtmlTemplate();
			
			result = HtmlTools.addTitle(result, "Login");
			
			String siteContent = "";
			if (request.getParameter("error") != null) {
				siteContent += "<b><font color=\"#AA0000\">Wrong password!</font></b>\n\r";
			}
			
			siteContent += "<form action=\"start\" method=\"post\">\n\rPassword: <input type=\"password\" name=\"password\"/><br />\r\n"
					+ "<br /><input type=\"submit\" class=\"black\" value=\"Login\">\r\n</form><br /><br />";
			result = HtmlTools.insertContentHtml(result, "Dream Hive Login:", siteContent, true);
			
			out.println(result);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (out != null) out.close();
		}
   }
   
   @Override
   public void doPost(HttpServletRequest request, HttpServletResponse response)
           throws ServletException, IOException {
	   doGet(request, response);
   }
   
}