package plm.services.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import plm.services.rules.IRule;
import plm.services.rules.Rule;
import plm.services.rules.RuleViolationException;
import plm.services.rules.RuleViolationException.RuleError;

class ServiceCommandTest {
	private ServiceCommand<Integer> cmd;
	private Consumer<Integer> print;

	@BeforeEach
	@SuppressWarnings("unchecked")
	void setUp() {
		IRule<Integer> even = new Rule<>(i -> i % 2 == 0, "number.even", "Your number is not even");
		print = (Consumer<Integer>) mock(Consumer.class);
		cmd = new ServiceCommand<>(even, print);
	}

	@Test
	void testExecute() {
		cmd.execute(2);

		verify(print, times(1)).accept(eq(2));
	}

	@Test
	void testExecuteRuleFailed() {
		cmd.setErrorHandler((errors) -> {
		});

		cmd.execute(3);

		verify(print, never()).accept(any());
	}

	@Test
	void testExecuteThrowExceptionOnError() {
		RuleViolationException exception = assertThrows(RuleViolationException.class, () -> {
			cmd.execute(3);
		});

		verify(print, never()).accept(any());

		List<RuleError> errors = exception.getErrors();

		assertEquals(1, errors.size());
		assertEquals("number.even", errors.get(0).getCode());
		assertEquals("Your number is not even", errors.get(0).getDefaultMessage());
	}
}
