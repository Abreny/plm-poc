package plm.services;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import plm.dao.DocumentDao;
import plm.model.Document;
import plm.repositories.DocumentRepository;
import plm.services.commands.DocumentCommandFactory;
import plm.services.contracts.DocumentAdapter;
import plm.services.factories.NextFactory;
import plm.services.rules.IRule;
import plm.services.rules.Rule;

@Service
@Transactional
public class DocumentService {
	private final DocumentCommandFactory documentCommandFactory;
	private final DocumentDao documentDao;

	private final DocumentRepository documentRepository;
	private final NextFactory<DocumentAdapter> iterationFactory;

	private final NextFactory<DocumentAdapter> versionFactory;

	public DocumentService(DocumentDao documentDao, DocumentRepository documentRepository,
			@Qualifier("documentIterationFactory") NextFactory<DocumentAdapter> iterationFactory,
			@Qualifier("documentVersionFactory") NextFactory<DocumentAdapter> versionFactory,
			DocumentCommandFactory documentCommandFactory) {
		this.documentDao = documentDao;
		this.documentRepository = documentRepository;
		this.iterationFactory = iterationFactory;
		this.versionFactory = versionFactory;
		this.documentCommandFactory = documentCommandFactory;
	}

	private DocumentAdapter createCommandArg(String reference, String version, int iteration) {
		Document document = documentDao.get(reference, version, iteration);
		if (document == null) {
			throw new RuntimeException("Document not found");
		}
		return DocumentAdapter.createFromDocument(document);
	}

	public void free(String userId, String reference, String version, int iteration) {
		DocumentAdapter document = createCommandArg(reference, version, iteration);
		documentCommandFactory.free(reference, userId).addRule(notLinkedToPar(reference)).doExecute(doc -> {
			Document updatedDocument = doc.getDocument();
			updatedDocument.setReserved(false);
			updatedDocument.setReservedBy(null);
			documentDao.update(updatedDocument);
		}).build().execute(document);
	}

	private boolean isNotLinkedToPart(Document document) {
		//
		// Implementation and returned value are not relevant for this exercise
		//
		return documentRepository.findPartLinkedTo(document).isEmpty();
	}

	private IRule<DocumentAdapter> notLinkedToPar(String reference) {
		return new Rule<>(
				doc -> (doc instanceof DocumentAdapter) && isNotLinkedToPart(((DocumentAdapter) doc).getDocument()),
				"document.reserve.linked.to.part",
				String.format("Document %s is linked to a Part. Document linked to a Part is controlled by this Part.",
						reference));
	}

	public void reserve(String userId, String reference, String version, int iteration) {
		DocumentAdapter document = createCommandArg(reference, version, iteration);
		documentCommandFactory.reserve(reference).addRule(notLinkedToPar(reference)).doExecute(doc -> {
			Document nextIteration = iterationFactory.next(doc).getDocument();
			nextIteration.setReserved(true);
			nextIteration.setReservedBy(userId);
			documentDao.create(nextIteration);
		}).build().execute(document);
	}

	public void revise(String userId, String reference, String version, int iteration) throws RuntimeException {
		DocumentAdapter document = createCommandArg(reference, version, iteration);
		documentCommandFactory.revise(reference).addRule(notLinkedToPar(reference)).doExecute(doc -> {
			Document nextVersion = versionFactory.next(doc).getDocument();
			nextVersion.setReserved(false);
			nextVersion.setReservedBy(null);
			documentDao.create(nextVersion);
		}).build().execute(document);
	}

	public void setState(String userId, String reference, String version, int iteration, String state)
			throws RuntimeException {
		DocumentAdapter document = createCommandArg(reference, version, iteration);
		documentCommandFactory.changeState(reference, state).addRule(notLinkedToPar(reference)).doExecute(doc -> {
			Document updatedDocument = doc.getDocument();
			updatedDocument.setLifeCycleState(state);
			documentDao.update(updatedDocument);
		}).build().execute(document);
	}

	public void update(String userId, String reference, String version, int iteration, String documentAttribute1,
			String documentAttribute2) {
		DocumentAdapter document = createCommandArg(reference, version, iteration);
		documentCommandFactory.update(reference, userId)
//			.addRule(notLinkedToPar(reference))
				.doExecute(doc -> {
					Document updatedDocument = doc.getDocument();
					updatedDocument.setDocumentAttribute1(documentAttribute1);
					updatedDocument.setDocumentAttribute2(documentAttribute2);
					documentDao.update(updatedDocument);
				}).build().execute(document);
	}
}
