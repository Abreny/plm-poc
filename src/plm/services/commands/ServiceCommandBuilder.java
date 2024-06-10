package plm.services.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import plm.services.rules.IRule;
import plm.services.rules.RuleComposite;

public class ServiceCommandBuilder<T> {
	private Consumer<T> body;
	private Consumer<IRule<T>> errorHandler;
	private List<IRule<T>> rules;

	public ServiceCommandBuilder<T> addRule(IRule<T> rule) {
		if (rules == null) {
			rules = new ArrayList<>();
		}

		this.rules.add(rule);

		return this;
	}

	public ServiceCommand<T> build() {
		ServiceCommand<T> result = new ServiceCommand<>(rules == null ? null : new RuleComposite<T>(rules), body);

		if (errorHandler != null) {
			result.setErrorHandler(errorHandler);
		}

		return result;
	}

	public ServiceCommandBuilder<T> disableErrorOnFail() {
		this.errorHandler = (rule) -> {
		};
		return this;
	}

	public ServiceCommandBuilder<T> doExecute(Consumer<T> body) {
		this.body = body;
		return this;
	}

	public ServiceCommandBuilder<T> errorHandler(Consumer<IRule<T>> errorHandler) {
		this.errorHandler = errorHandler;

		return this;
	}
}
