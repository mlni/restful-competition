package httpc.solver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

public class Result {
	private final String body;
	private final Map<String, String> cookies = new HashMap<String, String>();
	private int code = 200;
	public Result(String content) {
		this.body = content;
	}
	
	public Result sendCookie(String name, String value) {
		cookies.put(name, value);
		return this;
	}
	
	public String getContent() {
		return body;
	}
	
	public Cookie[] cookiesHeaders() {
		if (cookies.isEmpty())
			return new Cookie[0];
		List<Cookie> result = new ArrayList<Cookie>();
		for (String name : cookies.keySet()) {
			String val = name + "=" + cookies.get(name) + "; PATH=/";
			result.add(new Cookie(name, cookies.get(name)));
		}
		return result.toArray(new Cookie[result.size()]);
	}
	
	public String toString() {
		return body;
	}

	public Result withResponseCode(int code) {
		this.code = code;
		return this;
	}
	
	public int getCode() {
		return code;
	}
}
