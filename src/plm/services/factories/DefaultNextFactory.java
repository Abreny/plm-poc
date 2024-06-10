package plm.services.factories;

import plm.services.contracts.HasLifeCycleState;
import plm.services.contracts.HasVersion;

public abstract class DefaultNextFactory<T extends HasLifeCycleState & HasVersion> implements NextFactory<T> {

	abstract protected T createNext(T target);

	@Override
	public T next(T target) {
		assert target != null;
		assert target.getLifeCycleTemplate() != null;
		assert target.getVersionSchema() != null;

		T result = createNext(target);

		result.setLifeCycleTemplate(target.getLifeCycleTemplate());
		result.setLifeCycleState(target.getLifeCycleState());

		result.setVersionSchema(target.getVersionSchema());

		setAdditionalAttribute(result, target);

		return result;
	}

	abstract protected void setAdditionalAttribute(T result, T target);
}
