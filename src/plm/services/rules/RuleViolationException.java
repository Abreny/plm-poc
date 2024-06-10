package plm.services.rules;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
public class RuleViolationException extends RuntimeException {
	public static class RuleError {
		private final String code;
		private final String defaultMessage;

		public RuleError(IRule<?> rule) {
			this.code = rule.getCode();
			this.defaultMessage = rule.getDefaultMessage();
		}

		public String getCode() {
			return code;
		}

		public String getDefaultMessage() {
			return defaultMessage;
		}
	}

	private final List<RuleError> errors;

	public RuleViolationException(IRule<?> rule) {
		errors = new ArrayList<RuleViolationException.RuleError>();
		for (IRule<?> failed : rule.getAllFailed()) {
			errors.add(new RuleError(failed));
		}
	}

	public List<RuleError> getErrors() {
		return errors;
	}

	@Override
	public String getMessage() {
		if (errors == null || errors.isEmpty()) {
			return null;
		}
		return errors.get(0).getDefaultMessage();
	}
}
