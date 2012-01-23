package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import javax.xml.bind.DatatypeConverter;


public class UsernameCase implements Case {
	@Override
	public boolean matches(IncomingRequest r) {
		return r.question().startsWith("What is my username");
	}

	@Override
	public Result solve(IncomingRequest r) {
		String authHeader = r.header("authorization");
		try {
			String base64Pair = authHeader.split(" ")[1];
			String userPassPair = new String(DatatypeConverter.parseBase64Binary(base64Pair));
			return new Result(userPassPair.split(":")[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Result("FAIL");
	}

}
