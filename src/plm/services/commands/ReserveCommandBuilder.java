package plm.services.commands;

import plm.services.contracts.HasLifeCycleState;
import plm.services.contracts.HasReservedState;
import plm.services.rules.Rules;

public class ReserveCommandBuilder<T extends HasReservedState & HasLifeCycleState> extends ServiceCommandBuilder<T> {
	public ReserveCommandBuilder(Rules rules) {
		super();
		this.addRule(rules.notReserved()).addRule(rules.notFinalState());
	}
}
