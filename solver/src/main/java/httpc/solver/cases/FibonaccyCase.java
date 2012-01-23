package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FibonaccyCase implements Case {
	@Override
	public boolean matches(IncomingRequest r) {
		return r.question().matches("What is the [0-9]+th number in Fibonacci sequence");
	}

	@Override
	public Result solve(IncomingRequest r) {
		Matcher matcher = Pattern.compile(".* ([0-9]+)th .*").matcher(r.question());
		matcher.matches();
		
		int nth = Integer.parseInt(matcher.group(1));
		return new Result("" + fib(nth));
	}

	private String fib(int nth) {
		BigInteger a = BigInteger.ONE;
		BigInteger b = BigInteger.ONE;
		for (int i=0; i<nth - 1; i++) {
			BigInteger c = a.add(b);
			a = b;
			b = c;
		}
		return a.toString();
	}
}
