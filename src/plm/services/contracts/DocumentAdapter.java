package plm.services.contracts;

import plm.model.Document;

public class DocumentAdapter extends Document implements HasReservedState, HasLifeCycleState, HasVersion {
	private static void copyFrom(Document result, Document target) {
		result.setReserved(target.isReserved());
		result.setReservedBy(target.getReservedBy());

		result.setLifeCycleState(target.getLifeCycleState());
		result.setLifeCycleTemplate(target.getLifeCycleTemplate());

		result.setVersionSchema(target.getVersionSchema());

		result.setDocumentAttribute1(target.getDocumentAttribute1());
		result.setDocumentAttribute2(target.getDocumentAttribute2());
	}

	public static DocumentAdapter createFromDocument(Document target) {
		DocumentAdapter result = target == null ? new DocumentAdapter()
				: new DocumentAdapter(target.getReference(), target.getVersion(), target.getIteration());
		if (target != null) {
			copyFrom(result, target);
		}
		return result;
	}

	public DocumentAdapter() {
		super();
	}

	public DocumentAdapter(String reference, String version, int iteration) {
		super(reference, version, iteration);
	}

	public Document getDocument() {
		Document result = new Document(getReference(), getVersion(), getIteration());

		copyFrom(result, this);

		return result;
	}
}
