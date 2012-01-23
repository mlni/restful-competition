package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class GreatestCommonDivisor implements Case {

	@Override
	public boolean matches(IncomingRequest r) {
		return r.question().matches("What is the greatest common divisor .*");
	}

	@Override
	public Result solve(IncomingRequest r) {
		Matcher matcher = Pattern.compile(".* ([0-9]+) and ([0-9]+)").matcher(r.question());
		matcher.matches();
		
		BigInteger a = new BigInteger(matcher.group(1));
		BigInteger b = new BigInteger(matcher.group(2));
		
		return new Result(a.gcd(b).toString());
	}
}
