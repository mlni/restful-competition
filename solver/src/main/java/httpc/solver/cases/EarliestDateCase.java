package httpc.solver.cases;

import static httpc.solver.cases.DaysBetweenCase.parse;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.text.ParseException;
import java.util.Date;


public class EarliestDateCase implements Case {

	@Override
	public boolean matches(IncomingRequest r) {
		return r.question().startsWith("Which of the following dates is the earliest");
	}

	@Override
	public Result solve(IncomingRequest r) {
		String[] dates = r.question().substring(r.question().indexOf(":") + 1).split(", ");
		
		try {
			String val = dates[0];
			Date earliest = parse(dates[0].trim());
			for (String dayStr : dates) {
				Date d = parse(dayStr);
				if (d.before(earliest)) {
					earliest = d;
					val = dayStr;
				}
			}
			return new Result(val);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Result("FAIL");
	}

}
