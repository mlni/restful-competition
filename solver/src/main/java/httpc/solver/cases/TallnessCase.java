package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TallnessCase implements Case {

	@Override
	public boolean matches(IncomingRequest r) {
		return r.hasParam("q") && 
			(   r.question().matches("\\w+ is .* tall. How tall is he in .*")
			 || r.question().startsWith("Which of them is taller"));
	}

	@Override
	public Result solve(IncomingRequest r) {
		String q = r.question();
		if (q.matches("\\w+ is [0-9]+'.*")) {
			Matcher matcher = Pattern.compile("(\\w+) is ([0-9]+)'.*").matcher(r.question());
			matcher.matches();
			
			String name = matcher.group(1);
			int feet = Integer.parseInt(matcher.group(2));
			int inches = 0;
			if (q.matches(".*[0-9]+'[0-9]+\" .*")) {
				Matcher inchesMatcher = Pattern.compile(".*[0-9]+'([0-9]+)\" .*").matcher(r.question());
				inchesMatcher.matches();
				inches = Integer.parseInt(inchesMatcher.group(1));
			}
			double cm = feet * 12 * 2.54 + inches * 2.54;
			
			r.setSession("name1", name);
			r.setSession("len1", Double.toString(cm));
			
			return result(cm);
		} else if (q.matches(".*is [0-9]+ cm .*")) {
			Matcher matcher = Pattern.compile("(\\w+) is ([0-9]+) cm .*").matcher(r.question());
			matcher.matches();
			
			String name = matcher.group(1);
			int cm = Integer.parseInt(matcher.group(2));
			r.setSession("name2", name);
			r.setSession("len2", Integer.toString(cm));
			
			return result(cm / 2.54);
		} else {
			String name = Double.parseDouble(r.session("len1")) > Double.parseDouble(r.session("len2")) ? r.session("name1") : r.session("name2");
			return new Result(name);
		}
	}

	private Result result(double inexact) {
		return new Result("" + (int) Math.round(inexact));
	}
}
