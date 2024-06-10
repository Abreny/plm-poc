package plm.services.rules;

import java.util.List;

public interface IRuleChecker<T> {
	List<IRule<T>> check(T value);
}
