package plm.services.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plm.services.rules.Rule;

public class ServiceCommandBuilderTest {
	@Test
	void testBuild() {
		StringBuilder out = new StringBuilder();
		ServiceCommand<Integer> cmd = new ServiceCommandBuilder<Integer>()
				.addRule(new Rule<Integer>(i -> i % 2 == 0, "number.even", "not even number"))
				.addRule(new Rule<Integer>(i -> i % 3 == 0, "number.divisible.three", "not divisible by three"))
				.disableErrorOnFail().doExecute(i -> {
					out.append(i);
				}).build();
		cmd.execute(6);

		assertEquals("6", out.toString());
	}
}
