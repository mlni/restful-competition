package httpc.solver;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("rawtypes")
public class IncomingRequest {
	private final Map params = new HashMap();
	private final Map headers = new HashMap();
	private final Map cookies = new HashMap();
	
	private String method = "GET";
	private String path = "/";
	private String body = null;
	private Map<String, String> session;

	@SuppressWarnings("unchecked")
	public IncomingRequest(HttpServletRequest request, Map<String, String> session) throws IOException {
		Map params = request.getParameterMap();
		Cookie cookies[] = request.getCookies();
		
		for (Object key : params.keySet()) {
			String val[] = (String[]) params.get(key);
			this.params.put(key, val[0]);
		}
		
		if (cookies != null) {
			for (Cookie c : cookies) {
				this.cookies.put(c.getName(), c.getValue());
			}
		}
		
		for (Enumeration e = request.getHeaderNames(); e.hasMoreElements();) {
			String name = (String) e.nextElement();
			this.withHeader(name, request.getHeader(name));
		}
		
		this.session = session;
		this.method = request.getMethod();
		this.path = request.getRequestURI();
		try {
			if (request.getInputStream() != null) {
				StringBuilder b = new StringBuilder();
				byte buf[] = new byte[1024];
				ServletInputStream in = request.getInputStream();
				while (in.available() > 0) {
					int read = in.read(buf);
					b.append(new String(buf, 0, read, "UTF-8"));
				}
				this.body = b.toString();
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public IncomingRequest(String key, String value) {
		put(key, value);
	}
	
	public String getMethod() {
		return method;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setSession(String key, String value) {
		if (session == null)
			session = new HashMap<String, String>();
		
		session.put(key, value);
	}
	
	public String session(String key) {
		if (session == null)
			return null;
		return (String) session.get(key);
	}
	
	public IncomingRequest() {
	}

	public IncomingRequest(String key, String value, Map<String, String> session) {
		this(key, value);
		this.session = session;
	}

	@SuppressWarnings("unchecked")
	private void put(String key, String value) {
		params.put(key, value);
	}

	public String param(String name) {
		return (String) params.get(name);
	}

	public boolean hasParam(String name) {
		return param(name) != null;
	}

	public IncomingRequest withParam(String k, String v) {
		put(k, v);
		return this;
	}
	
	public IncomingRequest withCookie(String k, String v) {
		cookies.put(k, v);
		return this;
	}

	public IncomingRequest withHeader(String k, String v) {
		headers.put(k.toLowerCase(), v);
		return this;
	}
	
	public String header(String k) {
		return (String) headers.get(k.toLowerCase());
	}

	public String cookie(String name) {
		return (String) cookies.get(name);
	}
	
	public String toString() {
		return "" + (params.isEmpty() ? "" : params)
			+ " " + (cookies.isEmpty() ? "" : cookies)
			+ " " + headers;
	}

	public String getBody() {
		return body;
	}

	public Map<String,String> getSession() {
		return session;
	}
	
	public IncomingRequest nextRequest(String key, String value) {
		return new IncomingRequest(key, value, session);
	}
	
	public String question() {
		return hasParam("q") ? param("q") : "";
	}

	public IncomingRequest withCookies(Cookie[] cookiesHeaders) {
		for (Cookie c : cookiesHeaders)
			cookies.put(c.getName(), c.getValue());
		return this;
	}
}
