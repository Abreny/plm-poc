package plm.services.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import plm.model.Document;
import plm.model.LifeCycleTemplate;
import plm.model.VersionSchema;
import plm.services.contracts.DocumentAdapter;

class DocumentVersionFactoryTest {

	@Test
	void testGetNextIteration() {
		VersionSchema versionSchema = mock(VersionSchema.class);
		when(versionSchema.getNextVersionLabel(eq("BETA"))).thenReturn("STABLE");

		Document document = new Document("DOC01", "BETA", 10);
		document.setLifeCycleState("BETA");
		document.setLifeCycleTemplate(new LifeCycleTemplate());
		document.setVersionSchema(versionSchema);
		document.setReserved(false);
		document.setReservedBy(null);
		document.setDocumentAttribute1("ATTRIBUTE 1");
		document.setDocumentAttribute2("ATTRIBUTE 2");

		Document nextDocumentVersion = new DocumentVersionFactory().next(DocumentAdapter.createFromDocument(document))
				.getDocument();

		assertNotSame(document, nextDocumentVersion);
		assertEquals("DOC01", nextDocumentVersion.getReference());
		assertEquals("STABLE", nextDocumentVersion.getVersion());
		assertEquals(1, nextDocumentVersion.getIteration());
		assertSame(document.getLifeCycleTemplate(), nextDocumentVersion.getLifeCycleTemplate());
		assertSame(document.getVersionSchema(), nextDocumentVersion.getVersionSchema());
		assertEquals(document.isReserved(), nextDocumentVersion.isReserved());
		assertEquals(document.getReservedBy(), nextDocumentVersion.getReservedBy());
		assertEquals(document.getDocumentAttribute1(), nextDocumentVersion.getDocumentAttribute1());
		assertEquals(document.getDocumentAttribute2(), nextDocumentVersion.getDocumentAttribute2());
	}

}
