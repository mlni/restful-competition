package httpc.solver.cases;

import httpc.solver.IncomingRequest;
import httpc.solver.Result;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class SecondLargestCase implements Case {
	@Override
	public boolean matches(IncomingRequest r) {
		return r.question().indexOf("is second largest") != -1;
	}

	@Override
	public Result solve(IncomingRequest r) {
		int radix = 10;
		int startIndex = 0;
		if (r.question().indexOf("0x") != -1) {
			radix = 16;
			startIndex = 2;
		}
		
		List<Integer> nums = new ArrayList<Integer>();
		for (String n : r.question().substring(r.question().indexOf(":") + 1).split(", ")) {
			nums.add(Integer.parseInt(n.trim().substring(startIndex), radix));
		}
		
		Collections.sort(nums);
		Collections.reverse(nums);
		
		return new Result((radix == 16 ? "0x" : "") + Integer.toString(nums.get(1), radix));
	}
}
