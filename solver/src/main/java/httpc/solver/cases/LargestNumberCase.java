package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

public class LargestNumberCase implements Case {

	public boolean matches(IncomingRequest r) {
		return r.question().indexOf("is largest") != -1;
	}

	public Result solve(IncomingRequest r) {
		String q = r.question();
		String[] pcs = q.substring(q.indexOf(":") + 1).split(", ");
		
		int max = 0;
		for (String v : pcs) {
			int i = Integer.parseInt(v.trim());
			max = Math.max(max, i);
		}
		return new Result(Integer.toString(max));
	}

}
