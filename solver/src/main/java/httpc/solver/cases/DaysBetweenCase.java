package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DaysBetweenCase implements Case {

	@Override
	public boolean matches(IncomingRequest r) {
		return r.question().startsWith("How many days are between");
	}

	@Override
	public Result solve(IncomingRequest r) {
		Matcher matcher = Pattern.compile(".* ([0-9.-]+) and ([0-9.-]+)").matcher(r.question());
		matcher.matches();
		
		try {
			Date d1 = parse(matcher.group(1));
			Date d2 = parse(matcher.group(2));
			
			long days = abs((d1.getTime() - d2.getTime()) / (24 * 60 * 60 * 1000L));
			return new Result("" + days);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return new Result("FAIL");
	}

	private long abs(long x) {
		return x < 0 ? -x : x;
	}

	public static Date parse(String d) throws ParseException {
		d = d.trim();
		if (d.matches("[0-9]+-[0-9]+-[0-9]+"))
			return new SimpleDateFormat("yyyy-MM-dd").parse(d);
		else
			return new SimpleDateFormat("dd.MM.yyyy").parse(d);
	}

}
