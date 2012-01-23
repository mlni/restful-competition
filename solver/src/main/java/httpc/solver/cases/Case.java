package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

public interface Case {
	boolean matches(IncomingRequest r);
	Result solve(IncomingRequest r);
}
