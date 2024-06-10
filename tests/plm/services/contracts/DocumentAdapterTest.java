package plm.services.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import plm.model.Document;
import plm.model.LifeCycleTemplate;
import plm.model.VersionSchema;

class DocumentAdapterTest {
	@Test
	void testCreateFromPart() {
		Document document = new Document("DOC01", "V1", 1);
		document.setLifeCycleState("BETA");
		document.setLifeCycleTemplate(new LifeCycleTemplate());
		document.setVersionSchema(new VersionSchema());
		document.setReserved(false);
		document.setReservedBy(null);
		document.setDocumentAttribute1("ATTRIBUTE 1");
		document.setDocumentAttribute2("ATTRIBUTE 2");

		Document documentAdapter = DocumentAdapter.createFromDocument(document);

		assertInstanceOf(DocumentAdapter.class, documentAdapter);

		assertNotSame(document, documentAdapter);
		assertEquals("DOC01", documentAdapter.getReference());
		assertEquals("V1", documentAdapter.getVersion());
		assertEquals(1, documentAdapter.getIteration());
		assertSame(document.getLifeCycleTemplate(), documentAdapter.getLifeCycleTemplate());
		assertSame(document.getVersionSchema(), documentAdapter.getVersionSchema());
		assertEquals(document.isReserved(), documentAdapter.isReserved());
		assertEquals(document.getReservedBy(), documentAdapter.getReservedBy());
		assertEquals(document.getDocumentAttribute1(), documentAdapter.getDocumentAttribute1());
		assertEquals(document.getDocumentAttribute2(), documentAdapter.getDocumentAttribute2());
	}

	@Test
	void testGetPart() {
		DocumentAdapter documentAdapter = new DocumentAdapter("DOC01", "V1", 1);
		documentAdapter.setLifeCycleState("BETA");
		documentAdapter.setLifeCycleTemplate(new LifeCycleTemplate());
		documentAdapter.setVersionSchema(new VersionSchema());
		documentAdapter.setReserved(false);
		documentAdapter.setReservedBy(null);
		documentAdapter.setDocumentAttribute1("ATTRIBUTE 1");
		documentAdapter.setDocumentAttribute2("ATTRIBUTE 2");

		Document document = documentAdapter.getDocument();

		assertInstanceOf(Document.class, document);
		assertFalse(document instanceof DocumentAdapter);

		assertNotSame(documentAdapter, document);
		assertEquals("DOC01", document.getReference());
		assertEquals("V1", document.getVersion());
		assertEquals(1, document.getIteration());
		assertSame(documentAdapter.getLifeCycleTemplate(), document.getLifeCycleTemplate());
		assertSame(documentAdapter.getVersionSchema(), document.getVersionSchema());
		assertEquals(documentAdapter.isReserved(), document.isReserved());
		assertEquals(documentAdapter.getReservedBy(), document.getReservedBy());
		assertEquals(documentAdapter.getDocumentAttribute1(), document.getDocumentAttribute1());
		assertEquals(documentAdapter.getDocumentAttribute2(), document.getDocumentAttribute2());
	}
}
