package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

public class UserAgentCase implements Case {
	public boolean matches(IncomingRequest r) {
		return "Which browser am I using".equals(r.question());
	}

	public Result solve(IncomingRequest r) {
		return new Result(r.header("user-agent"));
	}
}
