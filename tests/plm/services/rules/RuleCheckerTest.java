package plm.services.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class RuleCheckerTest {
	@Test
	void testCheckRules() {
		Rule<Integer> divisibleByTwo = new Rule<>(i -> i % 2 == 0, "two", "number not divisible by 2");
		Rule<Integer> divisibleByThree = new Rule<>(i -> i % 3 == 0, "three", "number not divisible by 3");
		Rule<Integer> divisibleByFive = new Rule<>(i -> i % 5 == 0, "five", "number not divisible by 5");

		IRule<Integer> rules = new RuleComposite<>(Arrays.asList(divisibleByTwo, divisibleByThree, divisibleByFive));
		rules.setPrefixCode("number.divisible.by");

		RuleChecker<Integer> ruleChecker = new RuleChecker<>(rules);

		List<IRule<Integer>> rejectedRules = ruleChecker.check(1);
		assertEquals(3, rejectedRules.size());
		assertEquals("number.divisible.by.two", rejectedRules.get(0).getCode());
		assertEquals("number.divisible.by.three", rejectedRules.get(1).getCode());
		assertEquals("number.divisible.by.five", rejectedRules.get(2).getCode());
	}

	@Test
	void testCheckSingle() {
		Rule<Integer> divisibleByTwo = new Rule<>(i -> i % 2 == 0, "number.divisible.by.two",
				"number not divisible by 2");

		RuleChecker<Integer> ruleChecker = new RuleChecker<>(divisibleByTwo);

		List<IRule<Integer>> rejectedRules = ruleChecker.check(1);
		assertEquals(1, rejectedRules.size());
		assertEquals("number.divisible.by.two", rejectedRules.get(0).getCode());
	}
}
