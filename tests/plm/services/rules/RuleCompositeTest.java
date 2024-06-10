package plm.services.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class RuleCompositeTest {
	@Test
	void testIsValid() {
		Rule<Integer> divisibleByTwo = new Rule<>(i -> i % 2 == 0, "number.divisible.by.two",
				"number not divisible by 2");
		Rule<Integer> divisibleByThree = new Rule<>(i -> i % 3 == 0, "number.divisible.by.three",
				"number not divisible by 3");
		Rule<Integer> divisibleByFive = new Rule<>(i -> i % 5 == 0, "number.divisible.by.five",
				"number not divisible by 5");

		IRule<Integer> rules = new RuleComposite<>(Arrays.asList(divisibleByTwo, divisibleByThree, divisibleByFive));

		assertFalse(rules.isValid(2));
		assertFalse(rules.isValid(3));
		assertFalse(rules.isValid(5));
		assertFalse(rules.isValid(6));
		assertFalse(rules.isValid(10));
		assertFalse(rules.isValid(15));
		assertTrue(rules.isValid(30));
	}

	@Test
	void testSetPrefixCode() {
		Rule<Integer> divisibleByTwo = new Rule<>(i -> i % 2 == 0, "two", "number not divisible by 2");
		Rule<Integer> divisibleByThree = new Rule<>(i -> i % 3 == 0, "three", "number not divisible by 3");
		Rule<Integer> divisibleByFive = new Rule<>(i -> i % 5 == 0, "five", "number not divisible by 5");

		IRule<Integer> rules = new RuleComposite<>(Arrays.asList(divisibleByTwo, divisibleByThree, divisibleByFive));
		rules.setPrefixCode("number.divisible.by");

		assertEquals("number.divisible.by.two", divisibleByTwo.getCode());
		assertEquals("number.divisible.by.three", divisibleByThree.getCode());
		assertEquals("number.divisible.by.five", divisibleByFive.getCode());
	}
}
