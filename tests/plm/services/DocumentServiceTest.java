package plm.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import plm.dao.DocumentDao;
import plm.model.Document;
import plm.model.LifeCycleTemplate;
import plm.model.Part;
import plm.model.VersionSchema;
import plm.repositories.DocumentRepository;
import plm.services.commands.DocumentCommandFactory;
import plm.services.contracts.DocumentAdapter;
import plm.services.factories.NextFactory;
import plm.services.rules.RuleViolationException;

@SpringJUnitConfig
class DocumentServiceTest {
	@Configuration
	@ComponentScan(basePackages = { "plm.services.factories", "plm.services.commands" })
	static class DocumentServiceConfig {

	}

	@Autowired
	private DocumentCommandFactory documentCommandFactory;

	private DocumentDao documentDao;
	private DocumentRepository documentRepository;

	private DocumentService documentService;

	@Autowired
	@Qualifier("documentIterationFactory")
	private NextFactory<DocumentAdapter> iterationFactory;

	private LifeCycleTemplate lifeCycleTemplate;

	@Autowired
	@Qualifier("documentVersionFactory")
	private NextFactory<DocumentAdapter> versionFactory;

	private VersionSchema versionSchema;

	private Document createDocumentTest(String reference, String version, int iteration) {
		Document document = new Document(reference, version, iteration);
		document.setLifeCycleState("BETA");
		document.setLifeCycleTemplate(lifeCycleTemplate);
		document.setDocumentAttribute1("ATTRIBUTE 1");
		document.setDocumentAttribute2("ATTRIBUTE 2");
		document.setReserved(false);
		document.setReservedBy(null);
		document.setVersionSchema(versionSchema);
		return document;
	}

	@BeforeEach
	void setUp() {
		this.documentDao = mock(DocumentDao.class);
		this.documentRepository = mock(DocumentRepository.class);

		this.lifeCycleTemplate = mock(LifeCycleTemplate.class);
		this.versionSchema = mock(VersionSchema.class);

		this.documentService = new DocumentService(documentDao, documentRepository, iterationFactory, versionFactory,
				documentCommandFactory);
	}

	@Test
	void testFree() {
		Document doc = new Document();
		doc.setReservedBy("TEST");
		doc.setReserved(true);

		when(documentDao.get(anyString(), anyString(), anyInt())).thenReturn(doc);

		documentService.free("TEST", "DOC01", "V1", 1);

		ArgumentCaptor<Document> freedDocumentArg = ArgumentCaptor.forClass(Document.class);
		verify(documentDao, times(1)).update(freedDocumentArg.capture());

		Document freedDoc = freedDocumentArg.getValue();

		assertNull(freedDoc.getReservedBy());
		assertFalse(freedDoc.isReserved());
	}

	@Test
	void testFreeLinkedToPart() {
		Document doc = new Document();
		doc.setReservedBy("TEST");
		doc.setReserved(true);

		when(documentRepository.findPartLinkedTo(same(doc))).thenReturn(Optional.of(new Part()));
		when(documentDao.get(anyString(), anyString(), anyInt())).thenReturn(doc);

		when(documentRepository.findPartLinkedTo(any())).thenReturn(Optional.of(new Part()));

		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			documentService.free("TEST", "DOC01", "V2", 1);
		});

		assertEquals("Document DOC01 is linked to a Part. Document linked to a Part is controlled by this Part.",
				error.getMessage());
	}

	@Test
	void testFreeNotReserved() {
		Document doc = new Document();

		when(documentDao.get(anyString(), anyString(), anyInt())).thenReturn(doc);

		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			documentService.free("TEST", "DOC01", "V2", 1);
		});

		assertEquals("Document DOC01 is not reserved.", error.getMessage());
	}

	@Test
	void testFreeWithDifferentUser() {
		Document doc = new Document();
		doc.setReservedBy("TEST");
		doc.setReserved(true);

		when(documentDao.get(anyString(), anyString(), anyInt())).thenReturn(doc);

		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			documentService.free("ANOTHER_USER_TEST", "DOC01", "V2", 1);
		});

		assertEquals("Document DOC01 is reserved by other user. Only user who reserve the Document can free it.",
				error.getMessage());
	}

	@Test
	void testReserve() {
		// arrange
		Document document = createDocumentTest("DOC01", "V1", 1);

		when(lifeCycleTemplate.isFinal(anyString())).thenReturn(false); // ensure that it is not a final state
		when(documentDao.get(eq("DOC01"), eq("V1"), eq(1))).thenReturn(document);
		when(documentRepository.findPartLinkedTo(any())).thenReturn(Optional.empty()); // there is no part linked to the
																						// document

		// act
		documentService.reserve("TEST", "DOC01", "V1", 1);

		// assert
		ArgumentCaptor<Document> nextDocumentIterationArg = ArgumentCaptor.forClass(Document.class);
		verify(documentDao, times(1)).create(nextDocumentIterationArg.capture());
		verify(lifeCycleTemplate, times(1)).isFinal(eq("BETA"));

		Document nextDocumentIteration = nextDocumentIterationArg.getValue();
		assertEquals("V1", nextDocumentIteration.getVersion());
		assertEquals("DOC01", nextDocumentIteration.getReference());
		assertEquals(2, nextDocumentIteration.getIteration());
		assertEquals("BETA", nextDocumentIteration.getLifeCycleState());
		assertEquals("TEST", nextDocumentIteration.getReservedBy());
		assertTrue(nextDocumentIteration.isReserved());
		assertEquals("ATTRIBUTE 1", nextDocumentIteration.getDocumentAttribute1());
		assertEquals("ATTRIBUTE 2", nextDocumentIteration.getDocumentAttribute2());
		assertEquals(document.getLifeCycleTemplate(), nextDocumentIteration.getLifeCycleTemplate());
		assertEquals(document.getVersionSchema(), nextDocumentIteration.getVersionSchema());
	}

	@Test
	void testReserveADocumentAlreadyReservedShouldThrowAnException() {
		// arrange
		Document document = createDocumentTest("DOC01", "V1", 1);
		document.setReserved(true);
		document.setReservedBy("TEST");

		when(documentDao.get(eq("DOC01"), eq("V1"), eq(1))).thenReturn(document);

		// act & assert
		RuleViolationException error = assertThrows(RuleViolationException.class, () -> {
			documentService.reserve("TEST", "DOC01", "V1", 1);
		});

		assertEquals("Document DOC01 is reserved. Reserved Document is not allowed to reserve.",
				error.getErrors().get(0).getDefaultMessage());
	}

	@Test
	void testReserveFinalState() {
		// arrange
		Document document = createDocumentTest("DOC01", "V1", 1);

		when(documentDao.get(eq("DOC01"), eq("V1"), eq(1))).thenReturn(document);
		when(lifeCycleTemplate.isFinal(anyString())).thenReturn(true);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			documentService.reserve("TEST", "DOC01", "V1", 1);
		});

		assertEquals(
				"Document DOC01 is in final lifecycle state. Document in final lifecycle is not allowed to reserve.",
				error.getMessage());
	}

	@Test
	void testReserveLinkedToPart() {
		// arrange
		Document document = createDocumentTest("DOC01", "V1", 1);

		when(documentDao.get(eq("DOC01"), eq("V1"), eq(1))).thenReturn(document);
		when(lifeCycleTemplate.isFinal(anyString())).thenReturn(false);

		when(documentRepository.findPartLinkedTo(any())).thenReturn(Optional.of(new Part()));

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			documentService.reserve("TEST", "DOC01", "V1", 1);
		});

		assertEquals("Document DOC01 is linked to a Part. Document linked to a Part is controlled by this Part.",
				error.getMessage());
	}

	@Test
	void testRevise() {
		Document document = createDocumentTest("DOC01", "V2", 1);

		when(documentDao.get(eq("DOC01"), eq("V2"), eq(1))).thenReturn(document);
		when(lifeCycleTemplate.isFinal(any())).thenReturn(true);
		when(lifeCycleTemplate.getInitialState()).thenReturn("BETA");
		when(versionSchema.getNextVersionLabel(any())).thenReturn("V2 PHASE 1");

		// act
		documentService.revise("TEST", "DOC01", "V2", 1);

		// assert
		ArgumentCaptor<Document> nextDocumentVersionArg = ArgumentCaptor.forClass(Document.class);
		verify(documentDao, times(1)).create(nextDocumentVersionArg.capture());

		Document nextDocumentVersion = nextDocumentVersionArg.getValue();
		assertEquals("V2 PHASE 1", nextDocumentVersion.getVersion());
		assertEquals("DOC01", nextDocumentVersion.getReference());
		assertEquals(1, nextDocumentVersion.getIteration());
		assertEquals("BETA", nextDocumentVersion.getLifeCycleState());
		assertNull(nextDocumentVersion.getReservedBy());
		assertFalse(nextDocumentVersion.isReserved());
		assertEquals("ATTRIBUTE 1", nextDocumentVersion.getDocumentAttribute1());
		assertEquals("ATTRIBUTE 2", nextDocumentVersion.getDocumentAttribute2());
		assertEquals(document.getLifeCycleTemplate(), nextDocumentVersion.getLifeCycleTemplate());
		assertEquals(document.getVersionSchema(), nextDocumentVersion.getVersionSchema());
	}

	@Test
	void testReviseLinkedToPart() {
		// arrange
		Document document = createDocumentTest("DOC01", "V2", 1);

		when(documentDao.get(eq("DOC01"), eq("V2"), eq(1))).thenReturn(document);
		when(lifeCycleTemplate.isFinal(any())).thenReturn(true);
		when(lifeCycleTemplate.getInitialState()).thenReturn("BETA");
		when(versionSchema.getNextVersionLabel(any())).thenReturn("V2 PHASE 1");
		when(documentRepository.findPartLinkedTo(any())).thenReturn(Optional.of(new Part()));

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			documentService.revise("TEST", "DOC01", "V2", 1);
		});

		assertEquals("Document DOC01 is linked to a Part. Document linked to a Part is controlled by this Part.",
				error.getMessage());
	}

	@Test
	void testReviseWithoutFinalState() {
		// arrange
		Document document = createDocumentTest("DOC01", "V2", 1);

		when(documentDao.get(eq("DOC01"), eq("V2"), eq(1))).thenReturn(document);
		when(lifeCycleTemplate.isFinal(any())).thenReturn(false);
		when(lifeCycleTemplate.getInitialState()).thenReturn("BETA");
		when(versionSchema.getNextVersionLabel(any())).thenReturn("V2 PHASE 1");

		// act & assert
		RuleViolationException error = assertThrows(RuleViolationException.class, () -> {
			documentService.revise("TEST", "DOC01", "V2", 1);
		});

		assertEquals(
				"Document DOC01 is not in final lifecycle state. Only Document in final state is allowed to revise.",
				error.getErrors().get(0).getDefaultMessage());
	}

	@Test
	void testSetState() {
		// arrange
		Document document = createDocumentTest("DOC01", "V1", 1);

		when(documentDao.get(eq("DOC01"), eq("V1"), eq(1))).thenReturn(document);
		when(lifeCycleTemplate.isKnown(any())).thenReturn(true);

		// act
		documentService.setState("TEST", "DOC01", "V1", 1, "INITIAL");

		// assert
		ArgumentCaptor<Document> updatedDocumentArg = ArgumentCaptor.forClass(Document.class);
		verify(documentDao, times(1)).update(updatedDocumentArg.capture());

		Document updatedDocument = updatedDocumentArg.getValue();
		assertEquals("V1", updatedDocument.getVersion());
		assertEquals("DOC01", updatedDocument.getReference());
		assertEquals(1, updatedDocument.getIteration());
		assertEquals("INITIAL", updatedDocument.getLifeCycleState());
		assertNull(updatedDocument.getReservedBy());
		assertFalse(updatedDocument.isReserved());
		assertEquals("ATTRIBUTE 1", updatedDocument.getDocumentAttribute1());
		assertEquals("ATTRIBUTE 2", updatedDocument.getDocumentAttribute2());
		assertEquals(document.getLifeCycleTemplate(), updatedDocument.getLifeCycleTemplate());
		assertEquals(document.getVersionSchema(), updatedDocument.getVersionSchema());
	}

	@Test
	void testSetStateFinalState() {
		// arrange
		Document document = createDocumentTest("DOC01", "V1", 1);

		when(documentDao.get(eq("DOC01"), eq("V1"), eq(1))).thenReturn(document);
		when(lifeCycleTemplate.isKnown(any())).thenReturn(true);
		when(lifeCycleTemplate.isFinal(any())).thenReturn(true);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			documentService.setState("TEST", "DOC01", "V1", 1, "INITIAL");
		});

		assertEquals(
				"Document DOC01 is in final lifecycle state. Document in final lifecycle is not allowed to change_state.",
				error.getMessage());
	}

	@Test
	void testSetStateLinkedToPart() {
		// arrange
		Document document = createDocumentTest("DOC01", "V1", 1);

		when(documentDao.get(eq("DOC01"), eq("V1"), eq(1))).thenReturn(document);
		when(lifeCycleTemplate.isKnown(any())).thenReturn(true);
		when(documentRepository.findPartLinkedTo(any())).thenReturn(Optional.of(new Part()));

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			documentService.setState("TEST", "DOC01", "V1", 1, "INITIAL");
		});

		assertEquals("Document DOC01 is linked to a Part. Document linked to a Part is controlled by this Part.",
				error.getMessage());
	}

	@Test
	void testSetStateReserved() {
		// arrange
		Document document = createDocumentTest("DOC01", "V1", 1);
		document.setReserved(true);
		document.setReservedBy("TEST");

		when(documentDao.get(eq("DOC01"), eq("V1"), eq(1))).thenReturn(document);
		when(lifeCycleTemplate.isKnown(any())).thenReturn(true);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			documentService.setState("TEST", "DOC01", "V1", 1, "INITIAL");
		});

		assertEquals("Document DOC01 is reserved. Reserved Document is not allowed to change_state.",
				error.getMessage());
	}

	@Test
	void testSetStateUnknownState() {
		// arrange
		Document document = createDocumentTest("DOC01", "V1", 1);

		when(documentDao.get(eq("DOC01"), eq("V1"), eq(1))).thenReturn(document);
		when(lifeCycleTemplate.isKnown(any())).thenReturn(false);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			documentService.setState("TEST", "DOC01", "V1", 1, "UNKNOWN");
		});

		assertEquals("State UNKNOWN is not allowed lifecycle state for Document.", error.getMessage());
	}

	@Test
	void testUpdate() {
		// arrange
		Document document = createDocumentTest("DOC01", "V1", 1);
		document.setReserved(true);
		document.setReservedBy("TEST");

		when(documentDao.get(eq("DOC01"), eq("V1"), eq(1))).thenReturn(document);

		// act
		documentService.update("TEST", "DOC01", "V1", 1, "UPDATED ATTRIBUTE 1", "UPDATED ATTRIBUTE 2");

		// assert
		ArgumentCaptor<Document> updatedDocumentArg = ArgumentCaptor.forClass(Document.class);
		verify(documentDao, times(1)).update(updatedDocumentArg.capture());

		Document updatedDocument = updatedDocumentArg.getValue();
		assertEquals("V1", updatedDocument.getVersion());
		assertEquals("DOC01", updatedDocument.getReference());
		assertEquals(1, updatedDocument.getIteration());
		assertEquals("BETA", updatedDocument.getLifeCycleState());
		assertEquals("TEST", updatedDocument.getReservedBy());
		assertTrue(updatedDocument.isReserved());
		assertEquals("UPDATED ATTRIBUTE 1", updatedDocument.getDocumentAttribute1());
		assertEquals("UPDATED ATTRIBUTE 2", updatedDocument.getDocumentAttribute2());
		assertEquals(document.getLifeCycleTemplate(), updatedDocument.getLifeCycleTemplate());
		assertEquals(document.getVersionSchema(), updatedDocument.getVersionSchema());
	}

	@Test
	void testUpdateLinkedToPart() {
		// arrange
		Document document = createDocumentTest("DOC01", "V1", 1);
		document.setReserved(true);
		document.setReservedBy("TEST");

		when(documentDao.get(eq("DOC01"), eq("V1"), eq(1))).thenReturn(document);
		when(documentRepository.findPartLinkedTo(any())).thenReturn(Optional.of(new Part()));
		// act
		documentService.update("TEST", "DOC01", "V1", 1, "UPDATED ATTRIBUTE 1", "UPDATED ATTRIBUTE 2");

		// assert
		ArgumentCaptor<Document> updatedDocumentArg = ArgumentCaptor.forClass(Document.class);
		verify(documentDao, times(1)).update(updatedDocumentArg.capture());

		Document updatedDocument = updatedDocumentArg.getValue();
		assertEquals("UPDATED ATTRIBUTE 1", updatedDocument.getDocumentAttribute1());
		assertEquals("UPDATED ATTRIBUTE 2", updatedDocument.getDocumentAttribute2());
	}

	@Test
	void testUpdateNotReserved() {
		// arrange
		Document document = createDocumentTest("DOC01", "V1", 1);

		when(documentDao.get(eq("DOC01"), eq("V1"), eq(1))).thenReturn(document);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			documentService.update("TEST", "DOC01", "V1", 1, "UPDATED ATTRIBUTE 1", "UPDATED ATTRIBUTE 2");
		});

		assertEquals("Document DOC01 is not reserved.", error.getMessage());
	}

	@Test
	void testUpdateWithDifferentUser() {
		// arrange
		Document document = createDocumentTest("DOC01", "V1", 1);
		document.setReserved(true);
		document.setReservedBy("TEST");

		when(documentDao.get(eq("DOC01"), eq("V1"), eq(1))).thenReturn(document);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			documentService.update("ANOTHER_USER_TEST", "DOC01", "V1", 1, "UPDATED ATTRIBUTE 1", "UPDATED ATTRIBUTE 2");
		});

		assertEquals("Document DOC01 is reserved by other user. Only user who reserve the Document can update it.",
				error.getMessage());
	}
}
