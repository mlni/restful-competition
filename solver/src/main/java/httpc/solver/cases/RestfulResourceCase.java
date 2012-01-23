package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.util.HashMap;
import java.util.Map;


public class RestfulResourceCase implements Case {
	private static Map<String, String> resources = new HashMap<String, String>();
	
	public boolean matches(IncomingRequest r) {
		return r.getPath().startsWith("/resource/");
	}

	public Result solve(IncomingRequest r) {
		if ("PUT".equals(r.getMethod())) {
			resources.put(r.getPath(), r.getBody());
			return ok();
		} else if ("GET".equals(r.getMethod())) {
			if (resources.containsKey(r.getPath()))
				return new Result(resources.get(r.getPath()));
			else
				return new Result("Not Found").withResponseCode(404);
		} else if ("DELETE".equals(r.getMethod())) {
			resources.remove(r.getPath());
			return ok();
		}
		return null;
	}

	private Result ok() {
		return new Result("");
	}
}
