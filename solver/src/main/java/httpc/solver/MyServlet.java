package httpc.solver;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class MyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		run(request, response);
	}

	protected void run(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Solver s = new Solver();
		try {
			IncomingRequest r = new IncomingRequest(request, (Map<String, String>) request.getSession().getAttribute("session"));
			System.out.println(r);
			Result result = s.solve(r);

			if (r.getSession() != null)
				request.getSession().setAttribute("session", r.getSession());
			
			response.setContentType("text/plain");
			response.setStatus(result.getCode());

			for (Cookie cookie : result.cookiesHeaders()) {
				response.addCookie(cookie);
			}

			response.getWriter().println(result.getContent());
		} catch (Exception e) {
			e.printStackTrace();
			response.getWriter().println("FAIL");
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		run(req, resp);
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		run(req, resp);
	}
}
