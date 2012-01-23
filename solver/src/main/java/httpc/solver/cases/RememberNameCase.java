package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RememberNameCase implements Case {
	Pattern p = Pattern.compile("What is my name");
	Pattern p2 = Pattern.compile("My name is (\\w+). What is my name");

	public boolean matches(IncomingRequest r) {
		return r.hasParam("q") && p.matcher(r.question()).find();
	}

	public Result solve(IncomingRequest r) {
		if (containsName(r) != null) {
			String name = containsName(r);
			r.setSession("name", name);
			Result result = new Result(name);
			result.sendCookie("name", name);
			return result;
		} else {
			return new Result(r.cookie("name"));
		}
	}

	private String containsName(IncomingRequest r) {
		Matcher matcher = p2.matcher(r.question());
		if (matcher.matches()) {
			String name = matcher.group(1);
			return name;
		}
		return null;
	}

}
