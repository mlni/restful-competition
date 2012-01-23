package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FactorialCase implements Case {

	@Override
	public boolean matches(IncomingRequest r) {
		return r.question().startsWith("What is the factorial of");
	}

	@Override
	public Result solve(IncomingRequest r) {
		Matcher matcher = Pattern.compile(".* of ([0-9]+)").matcher(r.question());
		matcher.matches();
		
		BigInteger upto = new BigInteger(matcher.group(1));
		BigInteger result = BigInteger.ONE;
		BigInteger x = BigInteger.ONE;
		while (x.compareTo(upto) <= 0) {
			result = result.multiply(x);
			x = x.add(BigInteger.ONE);
		}
		
		return new Result(result.toString());
	}

}
