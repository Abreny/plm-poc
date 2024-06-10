package plm.services.factories;

import org.springframework.stereotype.Service;

import plm.services.contracts.PartAdapter;

@Service
public class PartVersionFactory extends DefaultNextFactory<PartAdapter> {
	@Override
	protected PartAdapter createNext(PartAdapter target) {
		return new PartAdapter(target.getReference(),
				target.getVersionSchema().getNextVersionLabel(target.getVersion()), 1);
	}

	@Override
	protected void setAdditionalAttribute(PartAdapter result, PartAdapter target) {
		result.setLifeCycleState(target.getLifeCycleTemplate().getInitialState());

		result.setPartAttribute1(target.getPartAttribute1());
		result.setPartAttribute2(target.getPartAttribute2());
	}
}
