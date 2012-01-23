package httpc.solver;

import httpc.solver.cases.ArithmeticCase;
import httpc.solver.cases.ArithmeticWithHexNumbersCase;
import httpc.solver.cases.ArithmeticWithParamsCase;
import httpc.solver.cases.Case;
import httpc.solver.cases.DaysBetweenCase;
import httpc.solver.cases.EarliestDateCase;
import httpc.solver.cases.FactorialCase;
import httpc.solver.cases.FibonaccyCase;
import httpc.solver.cases.GreatestCommonDivisor;
import httpc.solver.cases.LargestNumberCase;
import httpc.solver.cases.RefererCase;
import httpc.solver.cases.RememberNameCase;
import httpc.solver.cases.RememberNumbersCase;
import httpc.solver.cases.RestfulResourceCase;
import httpc.solver.cases.SecondLargestCase;
import httpc.solver.cases.TallnessCase;
import httpc.solver.cases.UltimateQuestionCase;
import httpc.solver.cases.UserAgentCase;
import httpc.solver.cases.UsernameCase;
import httpc.solver.cases.WeekdayCase;
import httpc.solver.cases.YourNameCase;

public class Solver {
	public Solver() {
	}
	
	Case findCase(IncomingRequest r) {
		Case cases[] = new Case[] {
			new UltimateQuestionCase(),
			new LargestNumberCase(),
			new SecondLargestCase(),
			new ArithmeticCase(),
			new ArithmeticWithHexNumbersCase(),
			new UserAgentCase(),
			new ArithmeticWithParamsCase(),
			new RefererCase(),
			new RememberNameCase(),
			new RememberNumbersCase(),
			new RestfulResourceCase(),
			new YourNameCase(),
			new GreatestCommonDivisor(),
			new FactorialCase(),
			new FibonaccyCase(),
			new DaysBetweenCase(),
			new EarliestDateCase(),
			new WeekdayCase(),
			new TallnessCase(),
			new UsernameCase()
		};
		
		for (Case c : cases)
			if (c.matches(r))
				return c;
		return null;
	}
	
	public Result solve(IncomingRequest request) {
		Case solver = findCase(request);
		
		if (solver != null) {
			return solver.solve(request);
		} else if (request.param("q") != null) {
			System.out.println("Unhandled: " + request.question());
		}
		return new Result("FAIL");
	}
}
