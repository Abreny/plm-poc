package plm.services.commands;

import plm.services.contracts.HasLifeCycleState;
import plm.services.contracts.HasReservedState;
import plm.services.rules.Rules;

public class FreeCommandBuilder<T extends HasReservedState & HasLifeCycleState> extends ServiceCommandBuilder<T> {
	public FreeCommandBuilder(Rules rules, String userId) {
		super();
		this.addRule(rules.reserved()).addRule(rules.reservedBy(userId));
	}
}
