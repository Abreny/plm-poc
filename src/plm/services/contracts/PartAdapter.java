package plm.services.contracts;

import plm.model.Part;

public class PartAdapter extends Part implements HasReservedState, HasLifeCycleState, HasVersion {
	private static void copyFrom(Part result, Part target) {
		result.setReserved(target.isReserved());
		result.setReservedBy(target.getReservedBy());

		result.setLifeCycleState(target.getLifeCycleState());
		result.setLifeCycleTemplate(target.getLifeCycleTemplate());

		result.setVersionSchema(target.getVersionSchema());

		result.setPartAttribute1(target.getPartAttribute1());
		result.setPartAttribute2(target.getPartAttribute2());
	}

	public static PartAdapter createFromPart(Part target) {
		PartAdapter result = target == null ? new PartAdapter()
				: new PartAdapter(target.getReference(), target.getVersion(), target.getIteration());
		if (target != null) {
			copyFrom(result, target);
		}
		return result;
	}

	public PartAdapter() {
		super();
	}

	public PartAdapter(String reference, String version, int iteration) {
		super(reference, version, iteration);
	}

	public Part getPart() {
		Part result = new Part(getReference(), getVersion(), getIteration());

		copyFrom(result, this);

		return result;
	}
}
