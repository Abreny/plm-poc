package plm.services.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import plm.model.LifeCycleTemplate;
import plm.model.Part;
import plm.model.VersionSchema;
import plm.services.contracts.PartAdapter;

class PartVersionFactoryTest {

	@Test
	void testGetNextIteration() {
		VersionSchema versionSchema = mock(VersionSchema.class);
		when(versionSchema.getNextVersionLabel(eq("V1"))).thenReturn("V2");

		Part part = new Part("PART01", "V1", 10);
		part.setLifeCycleState("BETA");
		part.setLifeCycleTemplate(new LifeCycleTemplate());
		part.setVersionSchema(versionSchema);
		part.setReserved(false);
		part.setReservedBy(null);
		part.setPartAttribute1("ATTRIBUTE 1");
		part.setPartAttribute2("ATTRIBUTE 2");

		Part nextPartVersion = new PartVersionFactory().next(PartAdapter.createFromPart(part)).getPart();

		assertNotSame(part, nextPartVersion);
		assertEquals("PART01", nextPartVersion.getReference());
		assertEquals("V2", nextPartVersion.getVersion());
		assertEquals(1, nextPartVersion.getIteration());
		assertSame(part.getLifeCycleTemplate(), nextPartVersion.getLifeCycleTemplate());
		assertSame(part.getVersionSchema(), nextPartVersion.getVersionSchema());
		assertEquals(part.isReserved(), nextPartVersion.isReserved());
		assertEquals(part.getReservedBy(), nextPartVersion.getReservedBy());
		assertEquals(part.getPartAttribute1(), nextPartVersion.getPartAttribute1());
		assertEquals(part.getPartAttribute2(), nextPartVersion.getPartAttribute2());
	}

}
