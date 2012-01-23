package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WeekdayCase implements Case {

	@Override
	public boolean matches(IncomingRequest r) {
		return r.question().startsWith("What was the weekday of");
	}

	@Override
	public Result solve(IncomingRequest r) {
		Matcher matcher = Pattern.compile(".* of ([0-9.-]+)").matcher(r.question());
		matcher.matches();
		String d = matcher.group(1);
		
		try {
			Date day = DaysBetweenCase.parse(d);
			Calendar cal = Calendar.getInstance();
			cal.setTime(day);
			int dow = cal.get(Calendar.DAY_OF_WEEK);
			String weekday = new String[] { "", "Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" }[dow];
			return new Result(weekday);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return new Result("FAIL");
	}

}
