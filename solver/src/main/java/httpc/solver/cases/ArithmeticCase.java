package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArithmeticCase implements Case {

	public boolean matches(IncomingRequest r) {
		return r.question().matches("How much is [0-9]+ [+*-/] [0-9]+");
	}

	public Result solve(IncomingRequest r) {
		Matcher matcher = Pattern.compile("How much is ([0-9]+) ([+*-/]) ([0-9]+)").matcher(r.question());
		matcher.matches();
		String op = matcher.group(2);
		
		return new Result(Integer.toString(calc(op, Integer.parseInt(matcher.group(1)), Integer.parseInt(matcher.group(3)))));
	}

	private int calc(String op, int i, int j) {
		if ("+".equals(op))
			return i + j;
		else if ("*".equals(op))
			return i * j;
		else if ("-".equals(op))
			return i - j;
		else if ("/".equals(op))
			return i / j;
		return 0;
	}
}
