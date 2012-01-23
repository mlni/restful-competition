package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArithmeticWithHexNumbersCase implements Case {

	public boolean matches(IncomingRequest r) {
		return r.question().matches("How much is 0x[0-9a-h]+ [+*-] 0x[0-9a-h]+");
	}

	public Result solve(IncomingRequest r) {
		Matcher matcher = Pattern.compile("How much is 0x(\\w+) ([+*-]) 0x(\\w+)").matcher(r.question());
		matcher.matches();
		String op = matcher.group(2);
		
		int result = calc(op, Integer.parseInt(matcher.group(1), 16), Integer.parseInt(matcher.group(3), 16));
		return new Result("0x" + Integer.toString(result, 16));
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
}
