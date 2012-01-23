package httpc.solver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import httpc.solver.IncomingRequest;
import httpc.solver.Result;
import httpc.solver.Solver;
import httpc.solver.cases.ArithmeticCase;
import httpc.solver.cases.ArithmeticWithParamsCase;
import httpc.solver.cases.LargestNumberCase;
import httpc.solver.cases.RememberNumbersCase;
import httpc.solver.cases.UserAgentCase;

import org.junit.Assert;
import org.junit.Test;


public class SolverTests {
	private Solver s = new Solver();

	@Test
	public void testYourName() {
		Result result = s.solve(puzzle("What is your name?"));
		assertEquals("matti", result.getContent());
	}
	
	@Test
	public void testLargestNumber() {
		IncomingRequest r = puzzle("Which of the numbers is largest: 507, 441, 487, 882, 882");
		LargestNumberCase c = new LargestNumberCase();
		Assert.assertTrue(c.matches(r));
		assertEquals("882", c.solve(r).getContent());
	}
	
	@Test
	public void testSecondLargestNumber() {
		IncomingRequest r = puzzle("Which of the numbers is second largest: 507, 441, 487, 881, 882");
		assertEquals("881", s.solve(r).getContent());
	}
	
	@Test
	public void testSecondLargestNumberInHex() {
		IncomingRequest r = puzzle("Which of the numbers is second largest: 0x507, 0x441, 0x487, 0x881, 0x882");
		assertEquals("0x881", s.solve(r).getContent());
	}

	private IncomingRequest puzzle(String question) {
		return new IncomingRequest("q", question);
	}
	
	@Test
	public void testSolver() {
		Result result = s.solve(puzzle("Which of the numbers is largest: 507, 441, 487, 882, 882"));
		assertEquals("882", result.getContent());
	}
	
	@Test
	public void testUnknownCase() {
		Result result = s.solve(puzzle("This question does not exist"));
		assertEquals("FAIL", result.getContent());
	}
	
	@Test
	public void testSum() {
		Assert.assertEquals(true, new ArithmeticCase().matches(puzzle("How much is 11 + 12")));
		
		Result result = s.solve(puzzle("How much is 11 + 12"));
		assertEquals("23", result.getContent());
	}
	
	@Test
	public void testDivision() {
		Result result = s.solve(puzzle("How much is 24 / 6"));
		assertEquals("4", result.getContent());
	}
	
	@Test
	public void testSumWithHexParams() {
		Result result = s.solve(puzzle("How much is 0x1 + 0x1"));
		assertEquals("0x2", result.getContent());
	}
	
	@Test
	public void testUserAgent() {
		String agent = "Mozilla FireBug";
		IncomingRequest r = new IncomingRequest().withParam("q", "Which browser am I using").withHeader("User-AGENT", agent);
		
		UserAgentCase c = new UserAgentCase();
		assertTrue(c.matches(r));
		assertEquals(agent, c.solve(r).getContent());
		assertEquals(agent, s.solve(r).getContent());
	}
	
	@Test
	public void testUsername() {
		IncomingRequest r = new IncomingRequest().withParam("q", "What is my username").withHeader("Authorization", "Basic c2V4eWtpdHR5MTM6a2FsYW1hamE=");
		Result result = s.solve(r);
		assertEquals("sexykitty13", result.getContent());
	}
	
	@Test 
	public void testArithmeticWithParams() {
		IncomingRequest r = new IncomingRequest()
			.withParam("q", "How much is a + b")
			.withParam("a", "10")
			.withParam("b", "12");
		
		ArithmeticWithParamsCase c = new ArithmeticWithParamsCase();
		assertTrue(c.matches(r));
		assertEquals("22", c.solve(r).getContent());
	}
	
	@Test 
	public void testArithmeticWithParamsAndHexNumbers() {
		IncomingRequest r = new IncomingRequest()
		.withParam("q", "How much is a + b")
		.withParam("a", "0xA")
		.withParam("b", "0xB");
		
		ArithmeticWithParamsCase c = new ArithmeticWithParamsCase();
		assertTrue(c.matches(r));
		assertEquals("0x15", c.solve(r).getContent());
	}
	
	@Test
	public void testGreatestCommonDivisor() {
		Result result = s.solve(puzzle("What is the greatest common divisor of 8 and 24"));
		assertEquals("8", result.getContent());
	}

	@Test
	public void testDaysBetween() {
		Solver s = new Solver();
		Result result = s.solve(puzzle("How many days are between 17.09.2003 and 19.09.2003"));
		assertEquals("2", result.getContent());
	}
	
	@Test
	public void testDaysBetweenUsDate() {
		Result result = s.solve(puzzle("How many days are between 2003-09-01 and 2003-09-10"));
		assertEquals("9", result.getContent());
	}
	
	@Test
	public void testEarliestDate() {
		Result result = s.solve(puzzle("Which of the following dates is the earliest: 28.02.2010, 01.04.2006, 04.09.2009, 19.09.2002, 17.02.2010"));
		assertEquals("19.09.2002", result.getContent());
	}
	
	@Test
	public void testWeekday() {
		Result result = s.solve(puzzle("What was the weekday of 28.11.2011"));
		assertEquals("Monday", result.getContent());
	}
	
	@Test
	public void testTallnessInCentimeters() {
		Result result = s.solve(puzzle("Bob is 180 cm tall. How tall is he in inches"));
		assertEquals("71", result.getContent());
	}
	
	@Test
	public void testTallnessInFeet() {
		Result result = s.solve(puzzle("Bill is 5' tall. How tall is he in centimeters"));
		assertEquals("152", result.getContent());
	}
	
	@Test
	public void testTallnessInFeetAndInches() {
		Result result = s.solve(puzzle("Bill is 5'11\" tall. How tall is he in centimeters"));
		assertEquals("180", result.getContent());
	}
	
	@Test
	public void testRememberWhichIsTaller() {
		IncomingRequest question1 = puzzle("Bill is 5'11\" tall. How tall is he in centimeters");
		Result result = s.solve(question1);
		IncomingRequest question2 = question1.nextRequest("q", "Bob is 185 cm tall. How tall is he in inches");
		result = s.solve(question2);
		IncomingRequest question = question2.nextRequest("q", "Which of them is taller");
		result = s.solve(question);
		assertEquals("Bob", result.getContent());
	}
	
	@Test
	public void testFactorial() {
		Result result = s.solve(puzzle("What is the factorial of 5"));
		assertEquals("120", result.getContent());
	}
	
	@Test
	public void testFibonacci() {
		Result result = s.solve(puzzle("What is the 10th number in Fibonacci sequence"));
		assertEquals("55", result.getContent());
	}
	
	@Test
	public void testBonusQuestion() {
		Solver s = new Solver();
		Result result = s.solve(puzzle("What is your name?").withHeader("X-The-Ultimate-Question", "What is the answer to Question of Life, the Universe and Everything?"));
		assertEquals("42", result.getContent());
	}
	
	@Test 
	public void testArithmeticWithCookies() {
		IncomingRequest r = new IncomingRequest()
		.withParam("q", "How much is a + b")
		.withCookie("a", "10")
		.withCookie("b", "12");
	
		ArithmeticWithParamsCase c = new ArithmeticWithParamsCase();
		assertTrue(c.matches(r));
		assertEquals("22", c.solve(r).getContent());
	}
	
	@Test
	public void testRememberName() {
		IncomingRequest r = puzzle("My name is Dennis. What is my name");
		Result result1 = s.solve(r);
		assertEquals("Dennis", result1.getContent());
		Result result = s.solve(r.nextRequest("q", "What is my name").withCookies(result1.cookiesHeaders()));
		assertEquals("Dennis", result.getContent());
	}
	
	@Test
	public void testRememberNumbers() {
		IncomingRequest r = puzzle("Let x be 4. What is x");
		assertTrue(new RememberNumbersCase().matches(r));
		assertEquals("4", new RememberNumbersCase().solve(r).getContent());
	}
}
