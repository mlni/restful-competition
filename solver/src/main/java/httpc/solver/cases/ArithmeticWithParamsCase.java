package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ArithmeticWithParamsCase implements Case {

	private Pattern pattern = Pattern.compile("How much is ([a-z]+) ([+*-]) ([a-z]+)");

	public boolean matches(IncomingRequest r) {
		return pattern.matcher(r.question()).matches();
	}

	public Result solve(IncomingRequest r) {
		Matcher matcher = pattern.matcher(r.question());
		matcher.matches();
		
		boolean isHex = lookup(r, matcher.group(1)).startsWith("0x");
		int radix = isHex ? 16 : 10;
		
		int a = parse(lookup(r, matcher.group(1)), radix);
		int b = parse(lookup(r, matcher.group(3)), radix);
		String op = matcher.group(2);
		return new Result(format(calculate(op, a, b), radix));
	}
	
	private int parse(String val, int radix) {
		if (radix == 16)
			return Integer.parseInt(val.substring(2), radix);
		return Integer.parseInt(val);
	}

	String format(int result, int radix) {
		return ((radix == 16) ? "0x" : "")
			+ Integer.toString(result, radix);
	}

	private String lookup(IncomingRequest r, String name) {
		if (r.param(name) != null)
			return r.param(name);
		else
			return r.cookie(name);
	}

	private int calculate(String op, int i, int j) {
		if ("+".equals(op))
			return i + j;
		else if ("*".equals(op))
			return i * j;
		else if ("-".equals(op))
			return i - j;
		return 0;
	}

}
