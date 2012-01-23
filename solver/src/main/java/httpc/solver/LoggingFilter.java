package httpc.solver;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class LoggingFilter implements Filter {

	public void destroy() {
	}

	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
			ServletException {
		try {
			HttpServletRequest r = (HttpServletRequest) req;
			System.out.println(r.getMethod() + " " + r.getRequestURI());
			chain.doFilter(req, resp);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void init(FilterConfig arg0) throws ServletException {
	}

}
