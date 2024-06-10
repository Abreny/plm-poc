package plm.services.factories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import plm.model.LifeCycleTemplate;
import plm.model.Part;
import plm.model.VersionSchema;
import plm.services.contracts.PartAdapter;

class PartIterationFactoryTest {

	@Test
	void testGetNextIteration() {
		Part part = new Part("PART01", "V1", 1);
		part.setLifeCycleState("BETA");
		part.setLifeCycleTemplate(new LifeCycleTemplate());
		part.setVersionSchema(new VersionSchema());
		part.setReserved(false);
		part.setReservedBy(null);
		part.setPartAttribute1("ATTRIBUTE 1");
		part.setPartAttribute2("ATTRIBUTE 2");

		Part nextPartIteration = new PartIterationFactory().next(PartAdapter.createFromPart(part)).getPart();

		assertNotSame(part, nextPartIteration);
		assertEquals("PART01", nextPartIteration.getReference());
		assertEquals("V1", nextPartIteration.getVersion());
		assertEquals(2, nextPartIteration.getIteration());
		assertSame(part.getLifeCycleTemplate(), nextPartIteration.getLifeCycleTemplate());
		assertSame(part.getVersionSchema(), nextPartIteration.getVersionSchema());
		assertEquals(part.isReserved(), nextPartIteration.isReserved());
		assertEquals(part.getReservedBy(), nextPartIteration.getReservedBy());
		assertEquals(part.getPartAttribute1(), nextPartIteration.getPartAttribute1());
		assertEquals(part.getPartAttribute2(), nextPartIteration.getPartAttribute2());
	}

}
