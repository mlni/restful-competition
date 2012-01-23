package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

public class RefererCase implements Case {

	public boolean matches(IncomingRequest r) {
		return "Which page am I coming from".equals(r.question());
	}

	public Result solve(IncomingRequest r) {
		return new Result(r.header("referer"));
	}

}
