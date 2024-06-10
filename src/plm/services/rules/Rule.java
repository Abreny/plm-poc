package plm.services.rules;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class Rule<T> implements IRule<T> {
	private String code;
	private final Predicate<T> condition;
	private final String defaultMessage;

	private IRule<T> failed;

	public Rule(Predicate<T> condition, String code) {
		this(condition, code, code);
	}

	public Rule(Predicate<T> condition, String code, String defaultMessage) {
		this.condition = condition;
		this.code = code;
		this.defaultMessage = defaultMessage;
	}

	@Override
	public List<IRule<T>> getAllFailed() {
		return Arrays.asList(failed);
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getDefaultMessage() {
		return defaultMessage;
	}

	@Override
	public boolean isValid(T value) {
		failed = null;
		if (!condition.test(value)) {
			failed = this;
		}
		return failed == null;
	}

	@Override
	public void setPrefixCode(String prefix) {
		this.code = prefix + "." + code;
	}
}
