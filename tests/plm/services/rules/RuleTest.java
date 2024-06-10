package plm.services.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RuleTest {
	@Test
	void testIsValid() {
		Rule<Integer> even = new Rule<>(i -> i % 2 == 0, "number.even", "Your number is odd");
		assertTrue(even.isValid(2));
		assertFalse(even.isValid(3));

		assertEquals("number.even", even.getCode());
		assertEquals("Your number is odd", even.getDefaultMessage());
	}
}
