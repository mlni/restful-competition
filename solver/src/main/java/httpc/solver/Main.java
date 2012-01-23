package httpc.solver;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

public class Main {
	public static void main(String[] args) throws Exception {
		Server server = new Server(4001);

		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath("/");
		server.setHandler(context);
		context.addServlet(new ServletHolder(new MyServlet()), "/*");

		server.start();
		server.join();
	}
}