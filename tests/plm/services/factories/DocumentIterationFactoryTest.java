package plm.services.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import plm.model.Document;
import plm.model.LifeCycleTemplate;
import plm.model.VersionSchema;
import plm.services.contracts.DocumentAdapter;

class DocumentIterationFactoryTest {

	@Test
	void testGetNextIteration() {
		Document document = new Document("DOC01", "V1", 1);
		document.setLifeCycleState("BETA");
		document.setLifeCycleTemplate(new LifeCycleTemplate());
		document.setVersionSchema(new VersionSchema());
		document.setReserved(false);
		document.setReservedBy(null);
		document.setDocumentAttribute1("ATTRIBUTE 1");
		document.setDocumentAttribute2("ATTRIBUTE 2");

		Document nextDocumentIteration = new DocumentIterationFactory()
				.next(DocumentAdapter.createFromDocument(document)).getDocument();

		assertNotSame(document, nextDocumentIteration);
		assertEquals("DOC01", nextDocumentIteration.getReference());
		assertEquals("V1", nextDocumentIteration.getVersion());
		assertEquals(2, nextDocumentIteration.getIteration());
		assertSame(document.getLifeCycleTemplate(), nextDocumentIteration.getLifeCycleTemplate());
		assertSame(document.getVersionSchema(), nextDocumentIteration.getVersionSchema());
		assertEquals(document.isReserved(), nextDocumentIteration.isReserved());
		assertEquals(document.getReservedBy(), nextDocumentIteration.getReservedBy());
		assertEquals(document.getDocumentAttribute1(), nextDocumentIteration.getDocumentAttribute1());
		assertEquals(document.getDocumentAttribute2(), nextDocumentIteration.getDocumentAttribute2());
	}

}
