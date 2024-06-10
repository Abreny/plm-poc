package plm.services.contracts;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import plm.model.LifeCycleTemplate;
import plm.model.Part;
import plm.model.VersionSchema;

class PartAdapterTest {

	@Test
	void testCreateFromPart() {
		Part part = new Part("PART01", "V1", 1);
		part.setLifeCycleState("BETA");
		part.setLifeCycleTemplate(new LifeCycleTemplate());
		part.setVersionSchema(new VersionSchema());
		part.setReserved(false);
		part.setReservedBy(null);
		part.setPartAttribute1("ATTRIBUTE 1");
		part.setPartAttribute2("ATTRIBUTE 2");

		Part partAdapter = PartAdapter.createFromPart(part);

		assertInstanceOf(PartAdapter.class, partAdapter);

		assertNotSame(part, partAdapter);
		assertEquals("PART01", partAdapter.getReference());
		assertEquals("V1", partAdapter.getVersion());
		assertEquals(1, partAdapter.getIteration());
		assertSame(part.getLifeCycleTemplate(), partAdapter.getLifeCycleTemplate());
		assertSame(part.getVersionSchema(), partAdapter.getVersionSchema());
		assertEquals(part.isReserved(), partAdapter.isReserved());
		assertEquals(part.getReservedBy(), partAdapter.getReservedBy());
		assertEquals(part.getPartAttribute1(), partAdapter.getPartAttribute1());
		assertEquals(part.getPartAttribute2(), partAdapter.getPartAttribute2());
	}

	@Test
	void testGetPart() {
		PartAdapter partAdapter = new PartAdapter("DOC01", "V1", 1);
		partAdapter.setLifeCycleState("BETA");
		partAdapter.setLifeCycleTemplate(new LifeCycleTemplate());
		partAdapter.setVersionSchema(new VersionSchema());
		partAdapter.setReserved(false);
		partAdapter.setReservedBy(null);
		partAdapter.setPartAttribute1("ATTRIBUTE 1");
		partAdapter.setPartAttribute2("ATTRIBUTE 2");

		Part part = partAdapter.getPart();

		assertInstanceOf(Part.class, part);
		assertFalse(part instanceof PartAdapter);

		assertNotSame(partAdapter, part);
		assertEquals("DOC01", part.getReference());
		assertEquals("V1", part.getVersion());
		assertEquals(1, part.getIteration());
		assertSame(partAdapter.getLifeCycleTemplate(), part.getLifeCycleTemplate());
		assertSame(partAdapter.getVersionSchema(), part.getVersionSchema());
		assertEquals(partAdapter.isReserved(), part.isReserved());
		assertEquals(partAdapter.getReservedBy(), part.getReservedBy());
		assertEquals(partAdapter.getPartAttribute1(), part.getPartAttribute1());
		assertEquals(partAdapter.getPartAttribute2(), part.getPartAttribute2());
	}
}
