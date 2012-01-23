package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

public class YourNameCase implements Case {

	@Override
	public boolean matches(IncomingRequest r) {
		return "What is your name?".equals(r.question());
	}

	@Override
	public Result solve(IncomingRequest r) {
		return new Result("matti");
	}

}
