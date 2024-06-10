package plm.services.commands;

import plm.services.contracts.HasLifeCycleState;
import plm.services.contracts.HasReservedState;
import plm.services.rules.Rules;

public class ReviseCommandBuilder<T extends HasReservedState & HasLifeCycleState> extends ServiceCommandBuilder<T> {
	public ReviseCommandBuilder(Rules rules) {
		super();
		this.addRule(rules.notReserved()).addRule(rules.finalState());
	}
}
