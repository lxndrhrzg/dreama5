package filters;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginFilter implements Filter {

	public static final String PASSWORD = "tomppa500";
	public static final String KEY = "bfal7vbylxcba64814cal";
	public static final String VALUE = "hafk3cbk2cf3vyc121";
	
	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		
		HttpSession session = ((HttpServletRequest) req).getSession(false);
		
		if (session != null && session.getAttribute(KEY) != null
				&& session.getAttribute(KEY).equals(VALUE)) {
			chain.doFilter(req, resp); //sends request to next resource
		} else {
			String password = req.getParameter("password");
			if (password != null && password.equals(PASSWORD)) {
				session = ((HttpServletRequest) req).getSession();
				session.setAttribute(KEY, VALUE);
				session.setMaxInactiveInterval(60 * 60 * 12); //12h timeout
				chain.doFilter(req, resp);
			} else if (password != null){
				((HttpServletResponse) resp).sendRedirect("/login?error=1");
			} else {
				((HttpServletResponse) resp).sendRedirect("/login");
			}
		}
		
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}

}
