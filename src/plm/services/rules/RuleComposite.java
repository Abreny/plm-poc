package plm.services.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RuleComposite<T> implements IRule<T> {
	private List<IRule<T>> failed;

	private final List<IRule<T>> rules;

	public RuleComposite(Collection<IRule<T>> rules) {
		this.rules = new ArrayList<>(rules);
	}

	@Override
	public List<IRule<T>> getAllFailed() {
		return failed;
	}

	@Override
	public String getCode() {
		return "";
	}

	@Override
	public boolean isValid(T value) {
		this.failed = new ArrayList<>();
		for (IRule<T> rule : rules) {
			if (!rule.isValid(value)) {
				this.failed.add(rule);
			}
		}
		return this.failed.isEmpty();
	}

	@Override
	public void setPrefixCode(String prefix) {
		for (IRule<T> rule : rules) {
			rule.setPrefixCode(prefix);
		}
	}
}
