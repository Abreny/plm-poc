package plm.services.commands;

import java.util.function.Consumer;

import plm.services.rules.IRule;
import plm.services.rules.RuleViolationException;

public class ServiceCommand<T> {
	private Consumer<T> body;
	private Consumer<IRule<T>> errorHandler;

	private IRule<T> rule;

	public ServiceCommand(IRule<T> rule, Consumer<T> body) {
		this.rule = rule;
		this.body = body;
		this.errorHandler = (error) -> {
			throw new RuleViolationException(error);
		};
	}

	public void execute(T value) {
		if (rule.isValid(value)) {
			this.body.accept(value);
			return;
		}
		errorHandler.accept(rule);
	}

	public void setErrorHandler(Consumer<IRule<T>> errorHandler) {
		this.errorHandler = errorHandler;
	}
}
