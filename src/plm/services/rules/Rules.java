package plm.services.rules;

import java.util.function.Predicate;

import plm.services.contracts.HasLifeCycleState;
import plm.services.contracts.HasReservedState;

public class Rules {
	private final String entityName;
	private final String reference;
	private final String serviceName;
	private final String upperEntityName;

	public Rules(String entityName, String serviceName, String reference) {
		this.entityName = entityName;
		this.upperEntityName = entityName.substring(0, 1).toUpperCase() + entityName.substring(1);
		this.serviceName = serviceName;
		this.reference = reference;
	}

	protected <T> IRule<T> createRule(Predicate<T> predicate, String code, String defaultMessage) {
		return new Rule<T>(predicate, code, defaultMessage);
	}

	public <T extends HasLifeCycleState> IRule<T> finalState() {
		String msg = getDefaultMessage(
				"%s %s is not in final lifecycle state. Only %s in final state is allowed to %s.", this.upperEntityName,
				reference, this.upperEntityName, this.serviceName);
		String code = getCode("state.final");

		return createRule(v -> v.getLifeCycleTemplate().isFinal(v.getLifeCycleState()), code, msg);
	}

	protected String getCode(String code) {
		return String.format("%s.%s.%s", entityName, serviceName, code);
	}

	protected String getDefaultMessage(String format, Object... args) {
		return String.format(format, args);
	}

	public <T extends HasLifeCycleState> IRule<T> knownState(String lifeCycleState) {
		String msg = getDefaultMessage("State %s is not allowed lifecycle state for %s.", lifeCycleState,
				this.upperEntityName);
		String code = getCode("state.known");

		return createRule(v -> v.getLifeCycleTemplate().isKnown(lifeCycleState), code, msg);
	}

	public <T extends HasLifeCycleState> IRule<T> notFinalState() {
		String msg = getDefaultMessage("%s %s is in final lifecycle state. %s in final lifecycle is not allowed to %s.",
				this.upperEntityName, reference, this.upperEntityName, this.serviceName);
		String code = getCode("state.final.not");

		return createRule(v -> !v.getLifeCycleTemplate().isFinal(v.getLifeCycleState()), code, msg);
	}

	public <T extends HasReservedState> IRule<T> notReserved() {
		String msg = getDefaultMessage("%s %s is reserved. Reserved %s is not allowed to %s.", this.upperEntityName,
				reference, this.upperEntityName, this.serviceName);
		String code = getCode("reserved.not");

		return createRule(v -> !v.isReserved(), code, msg);
	}

	public <T extends HasReservedState> IRule<T> reserved() {
		String msg = getDefaultMessage("%s %s is not reserved.", this.upperEntityName, reference);
		String code = getCode("reserved");

		return createRule(v -> v.isReserved(), code, msg);
	}

	public <T extends HasReservedState> IRule<T> reservedBy(String userId) {
		String msg = getDefaultMessage("%s %s is reserved by other user. Only user who reserve the %s can %s it.",
				this.upperEntityName, reference, this.upperEntityName, this.serviceName);
		String code = getCode("reserved.not");

		return createRule(v -> v.getReservedBy() != null && v.getReservedBy().equals(userId), code, msg);
	}

	public <T extends HasLifeCycleState> IRule<T> unknownState(String lifeCycleState) {
		String msg = getDefaultMessage("State %s is allowed lifecycle state for %s.", lifeCycleState,
				this.upperEntityName);
		String code = getCode("state.known.not");

		return createRule(v -> !v.getLifeCycleTemplate().isKnown(lifeCycleState), code, msg);
	}
}
