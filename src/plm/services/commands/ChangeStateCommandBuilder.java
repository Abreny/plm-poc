package plm.services.commands;

import plm.services.contracts.HasLifeCycleState;
import plm.services.contracts.HasReservedState;
import plm.services.rules.Rules;

public class ChangeStateCommandBuilder<T extends HasReservedState & HasLifeCycleState>
		extends ServiceCommandBuilder<T> {
	public ChangeStateCommandBuilder(Rules rules, String lifeCycleState) {
		super();
		this.addRule(rules.notReserved()).addRule(rules.notFinalState()).addRule(rules.knownState(lifeCycleState));
	}
}
