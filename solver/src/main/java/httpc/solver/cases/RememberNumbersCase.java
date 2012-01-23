package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RememberNumbersCase implements Case {
	Pattern set = Pattern.compile("Let (\\w+) be (\\w+)\\. What is (\\w+)");
	Pattern calc = Pattern.compile("Remember how much is (\\w+) ([+*-]+) (\\w+)");

	public boolean matches(IncomingRequest r) {
		return r.hasParam("q") && 
			(  set.matcher(r.question()).matches()
			|| calc.matcher(r.question()).matches());
	}

	public Result solve(IncomingRequest r) {
		Matcher m = set.matcher(r.question());
		if (m.matches()) {
			String key = m.group(1);
			String val = m.group(2);
			
			Result result = new Result(val);
			result.sendCookie(key, val);
			return result;
		} else {
			Matcher c = calc.matcher(r.question());
			c.matches();
			String a = c.group(1);
			String b = c.group(3);
			String op = c.group(2);
			
			return new Result(Integer.toString(calc(op, Integer.parseInt(lookup(r, a)), Integer.parseInt(lookup(r, b)))));
		}
	}
	
	private int calc(String op, int i, int j) {
		if ("+".equals(op))
			return i + j;
		else if ("*".equals(op))
			return i * j;
		else if ("-".equals(op))
			return i - j;
		return 0;
	}

	private String lookup(IncomingRequest r, String name) {
		if (r.param(name) != null)
		return r.param(name);
		else
			return r.cookie(name);
	}
}
