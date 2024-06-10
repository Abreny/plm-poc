package plm.services.rules;

import java.util.Collections;
import java.util.List;

public interface IRule<T> {
	default List<IRule<T>> getAllFailed() {
		return Collections.emptyList();
	}

	String getCode();

	default String getDefaultMessage() {
		return getCode();
	}

	boolean isValid(T value);

	default void setPrefixCode(String prefix) {

	}
}
