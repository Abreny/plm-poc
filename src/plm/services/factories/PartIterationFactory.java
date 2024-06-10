package plm.services.factories;

import org.springframework.stereotype.Service;

import plm.services.contracts.PartAdapter;

@Service
public class PartIterationFactory extends DefaultNextFactory<PartAdapter> {
	@Override
	protected PartAdapter createNext(PartAdapter target) {
		return new PartAdapter(target.getReference(), target.getVersion(), target.getIteration() + 1);
	}

	@Override
	protected void setAdditionalAttribute(PartAdapter result, PartAdapter target) {
		result.setPartAttribute1(target.getPartAttribute1());
		result.setPartAttribute2(target.getPartAttribute2());
	}
}
