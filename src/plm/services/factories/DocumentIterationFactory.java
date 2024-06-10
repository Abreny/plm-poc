package plm.services.factories;

import org.springframework.stereotype.Service;

import plm.services.contracts.DocumentAdapter;

@Service
public class DocumentIterationFactory extends DefaultNextFactory<DocumentAdapter> {
	@Override
	protected DocumentAdapter createNext(DocumentAdapter target) {
		return new DocumentAdapter(target.getReference(), target.getVersion(), target.getIteration() + 1);
	}

	@Override
	protected void setAdditionalAttribute(DocumentAdapter result, DocumentAdapter target) {
		result.setDocumentAttribute1(target.getDocumentAttribute1());
		result.setDocumentAttribute2(target.getDocumentAttribute2());
	}
}
