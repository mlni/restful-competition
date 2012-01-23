package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

public class UltimateQuestionCase implements Case {
	@Override
	public boolean matches(IncomingRequest r) {
		return r.header("x-the-ultimate-question") != null;
	}

	@Override
	public Result solve(IncomingRequest r) {
		return new Result("42");
	}
}
