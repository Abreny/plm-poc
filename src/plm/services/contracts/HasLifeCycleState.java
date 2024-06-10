package plm.services.contracts;

import plm.model.LifeCycleTemplate;

public interface HasLifeCycleState {
	String getLifeCycleState();

	LifeCycleTemplate getLifeCycleTemplate();

	void setLifeCycleState(String lifecycle);

	void setLifeCycleTemplate(LifeCycleTemplate lifeCycleTemplate);
}
