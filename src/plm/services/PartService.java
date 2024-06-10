package plm.services;

import java.util.Set;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import plm.dao.DocumentDao;
import plm.dao.PartDao;
import plm.model.Document;
import plm.model.Part;
import plm.repositories.DocumentRepository;
import plm.services.commands.PartCommandFactory;
import plm.services.contracts.DocumentAdapter;
import plm.services.contracts.PartAdapter;
import plm.services.factories.NextFactory;

@Service
@Transactional
public class PartService {
	private final DocumentDao documentDao;
	private final NextFactory<DocumentAdapter> documentIterationFactory;
	private final DocumentRepository documentRepository;

	private final NextFactory<DocumentAdapter> documentVersionFactory;
	private final PartCommandFactory partCommandFactory;

	private final PartDao partDao;
	private final NextFactory<PartAdapter> partIterationFactory;

	private final NextFactory<PartAdapter> partVersionFactory;

	public PartService(PartDao partDao, DocumentDao documentDao, DocumentRepository documentRepository,
			@Qualifier("partIterationFactory") NextFactory<PartAdapter> partIterationFactory,
			@Qualifier("partVersionFactory") NextFactory<PartAdapter> partVersionFactory,
			@Qualifier("documentIterationFactory") NextFactory<DocumentAdapter> documentIterationFactory,
			@Qualifier("documentVersionFactory") NextFactory<DocumentAdapter> documentVersionFactory,
			PartCommandFactory partCommandFactory) {
		this.partDao = partDao;
		this.documentDao = documentDao;
		this.documentRepository = documentRepository;
		this.partIterationFactory = partIterationFactory;
		this.partVersionFactory = partVersionFactory;
		this.documentIterationFactory = documentIterationFactory;
		this.documentVersionFactory = documentVersionFactory;
		this.partCommandFactory = partCommandFactory;
	}

	private DocumentAdapter createDocumentAdapter(Document doc) {
		return DocumentAdapter.createFromDocument(doc);
	}

	public void free(String userId, String reference, String version, int iteration) {

		PartAdapter part = getPart(reference, version, iteration);
		partCommandFactory.free(reference, userId).doExecute(partAdapter -> {
			Part updatedPart = partAdapter.getPart();
			updatedPart.setReserved(false);
			updatedPart.setReservedBy(null);

			partDao.update(updatedPart);

			for (Document document : getLinkedDocuments(partAdapter.getPart())) {

				document.setReserved(false);
				document.setReservedBy(null);

				documentDao.update(document);
			}
		}).build().execute(part);
	}

	private Set<Document> getLinkedDocuments(Part part) {
		//
		// Implementation and returned value are not relevant for this exercise
		//
		return documentRepository.findByPart(part);
	}

	private PartAdapter getPart(String reference, String version, int iteration) {
		Part part = partDao.get(reference, version, iteration);
		if (part == null) {
			throw new RuntimeException("Part not found");
		}
		return PartAdapter.createFromPart(part);
	}

	public void reserve(String userId, String reference, String version, int iteration) {
		PartAdapter part = getPart(reference, version, iteration);
		partCommandFactory.reserve(reference).doExecute(partAdapter -> {
			Part nextPartIteration = partIterationFactory.next(partAdapter).getPart();
			nextPartIteration.setReserved(true);
			nextPartIteration.setReservedBy(userId);

			partDao.create(nextPartIteration);

			for (Document document : getLinkedDocuments(partAdapter.getPart())) {
				Document nextIteration = documentIterationFactory.next(createDocumentAdapter(document)).getDocument();

				nextIteration.setReserved(true);
				nextIteration.setReservedBy(userId);

				documentDao.create(nextIteration);
			}
		}).build().execute(part);
	}

	public void revise(String userId, String reference, String version, int iteration) {
		PartAdapter part = getPart(reference, version, iteration);
		partCommandFactory.revise(reference).doExecute(partAdapter -> {
			Part nextPartVersion = partVersionFactory.next(partAdapter).getPart();
			nextPartVersion.setReserved(false);
			nextPartVersion.setReservedBy(null);

			partDao.create(nextPartVersion);

			for (Document document : getLinkedDocuments(partAdapter.getPart())) {
				Document nextVersion = documentVersionFactory.next(createDocumentAdapter(document)).getDocument();

				nextVersion.setReserved(false);
				nextVersion.setReservedBy(null);

				documentDao.create(nextVersion);
			}
		}).build().execute(part);
	}

	public void setState(String userId, String reference, String version, int iteration, String state) {

		PartAdapter part = getPart(reference, version, iteration);
		partCommandFactory.changeState(reference, state).doExecute(partAdapter -> {
			Part updatedPart = partAdapter.getPart();
			updatedPart.setLifeCycleState(state);

			partDao.update(updatedPart);

			for (Document document : getLinkedDocuments(partAdapter.getPart())) {

				document.setLifeCycleState(state);

				documentDao.update(document);
			}
		}).build().execute(part);
	}

	public void update(String userId, String reference, String version, int iteration, String partAttribute1,
			String partAttribute2) {
		PartAdapter part = getPart(reference, version, iteration);
		partCommandFactory.update(reference, userId).doExecute(partAdapter -> {
			Part updatedPart = partAdapter.getPart();
			updatedPart.setPartAttribute1(partAttribute1);
			updatedPart.setPartAttribute2(partAttribute2);

			partDao.update(updatedPart);
		}).build().execute(part);
	}
}
