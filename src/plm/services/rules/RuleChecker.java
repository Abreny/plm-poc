package plm.services.rules;

import java.util.List;

public class RuleChecker<T> implements IRuleChecker<T> {
	private final IRule<T> rule;

	public RuleChecker(IRule<T> rule) {
		this.rule = rule;
	}

	public List<IRule<T>> check(T value) {
		this.rule.isValid(value);

		return this.rule.getAllFailed();
	}
}
