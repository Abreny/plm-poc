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
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import plm.dao.DocumentDao;
import plm.dao.PartDao;
import plm.model.LifeCycleTemplate;
import plm.model.Part;
import plm.model.VersionSchema;
import plm.repositories.DocumentRepository;
import plm.services.commands.PartCommandFactory;
import plm.services.contracts.DocumentAdapter;
import plm.services.contracts.PartAdapter;
import plm.services.factories.NextFactory;

@SpringJUnitConfig
class PartServiceTest {
	@Configuration
	@ComponentScan(basePackages = { "plm.services.factories", "plm.services.commands" })
	static class PartServiceConfig {

	}

	private DocumentDao documentDao;
	@Autowired
	@Qualifier("documentIterationFactory")
	NextFactory<DocumentAdapter> documentIterationFactory;

	private DocumentRepository documentRepository;
	@Autowired
	@Qualifier("documentVersionFactory")
	NextFactory<DocumentAdapter> documentVersionFactory;

	private LifeCycleTemplate lifeCycleTemplate;

	@Autowired
	private PartCommandFactory partCommandFactory;

	private PartDao partDao;

	@Autowired
	@Qualifier("partIterationFactory")
	private NextFactory<PartAdapter> partIterationFactory;

	private PartService partService;

	@Autowired
	@Qualifier("partVersionFactory")
	NextFactory<PartAdapter> partVersionFactory;

	private VersionSchema versionSchema;

	private Part createTestPart(String reference, String version, int iteration) {
		Part part = new Part(reference, version, iteration);
		part.setLifeCycleState("BETA");
		part.setLifeCycleTemplate(lifeCycleTemplate);
		part.setPartAttribute1("ATTRIBUTE 1");
		part.setPartAttribute2("ATTRIBUTE 2");
		part.setReserved(false);
		part.setReservedBy(null);
		part.setVersionSchema(versionSchema);
		return part;
	}

	@BeforeEach
	void setUp() {
		this.partDao = mock(PartDao.class);
		this.documentDao = mock(DocumentDao.class);
		this.documentRepository = mock(DocumentRepository.class);

		this.lifeCycleTemplate = mock(LifeCycleTemplate.class);
		this.versionSchema = mock(VersionSchema.class);

		this.partService = new PartService(partDao, documentDao, documentRepository, partIterationFactory,
				partVersionFactory, documentIterationFactory, documentVersionFactory, partCommandFactory);
	}

	@Test
	void testFree() {
		Part part = new Part();
		part.setReservedBy("TEST");
		part.setReserved(true);

		when(partDao.get(anyString(), anyString(), anyInt())).thenReturn(part);

		partService.free("TEST", "PART01", "V1", 1);

		ArgumentCaptor<Part> freedPartArg = ArgumentCaptor.forClass(Part.class);
		verify(partDao, times(1)).update(freedPartArg.capture());

		Part freedPart = freedPartArg.getValue();
		assertNull(freedPart.getReservedBy());
		assertFalse(freedPart.isReserved());
	}

	@Test
	void testFreeNotReserved() {
		// arrange
		Part part = createTestPart("PART01", "V1", 1);

		when(partDao.get(eq("PART01"), eq("V1"), eq(1))).thenReturn(part);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			partService.free("TEST", "PART01", "V1", 1);
		});

		assertEquals("Part PART01 is not reserved.", error.getMessage());
	}

	@Test
	void testFreeWithDifferentUser() {
		Part part = new Part();
		part.setReservedBy("TEST");
		part.setReserved(true);

		when(partDao.get(anyString(), anyString(), anyInt())).thenReturn(part);

		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			partService.free("ANOTHER_USER_TEST", "PART01", "V2", 1);
		});

		assertEquals("Part PART01 is reserved by other user. Only user who reserve the Part can free it.",
				error.getMessage());
	}

	@Test
	void testReserve() {
		// arrange
		Part part = createTestPart("PART01", "V1", 1);
		part.setLifeCycleState("BETA");
		when(lifeCycleTemplate.isFinal(anyString())).thenReturn(false); // ensure that it is not a final state

		when(partDao.get(eq("PART01"), eq("V1"), eq(1))).thenReturn(part);
		when(documentRepository.findByPart(any())).thenReturn(Collections.emptySet());

		// act
		partService.reserve("TEST", "PART01", "V1", 1);

		// assert
		ArgumentCaptor<Part> nextPartIterationArg = ArgumentCaptor.forClass(Part.class);
		verify(partDao, atLeastOnce()).create(nextPartIterationArg.capture());
		verify(lifeCycleTemplate).isFinal(eq("BETA"));

		Part nextPartIteration = nextPartIterationArg.getValue();
		assertEquals("V1", nextPartIteration.getVersion());
		assertEquals("PART01", nextPartIteration.getReference());
		assertEquals(2, nextPartIteration.getIteration());
		assertEquals("BETA", nextPartIteration.getLifeCycleState());
		assertEquals("TEST", nextPartIteration.getReservedBy());
		assertTrue(nextPartIteration.isReserved());
		assertEquals("ATTRIBUTE 1", nextPartIteration.getPartAttribute1());
		assertEquals("ATTRIBUTE 2", nextPartIteration.getPartAttribute2());
		assertEquals(part.getLifeCycleTemplate(), nextPartIteration.getLifeCycleTemplate());
		assertEquals(part.getVersionSchema(), nextPartIteration.getVersionSchema());
	}

	@Test
	void testReserveAPartAlreadyReservedShouldThrowAnException() {
		// arrange
		Part part = createTestPart("PART01", "V1", 1);
		part.setReserved(true);
		part.setReservedBy("TEST");

		when(partDao.get(eq("PART01"), eq("V1"), eq(1))).thenReturn(part);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			partService.reserve("TEST", "PART01", "V1", 1);
		});

		assertEquals("Part PART01 is reserved. Reserved Part is not allowed to reserve.", error.getMessage());
	}

	@Test
	void testReserveFinalState() {
		// arrange
		Part part = createTestPart("PART01", "V1", 1);

		when(partDao.get(eq("PART01"), eq("V1"), eq(1))).thenReturn(part);
		when(lifeCycleTemplate.isFinal(anyString())).thenReturn(true);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			partService.reserve("TEST", "PART01", "V1", 1);
		});

		assertEquals("Part PART01 is in final lifecycle state. Part in final lifecycle is not allowed to reserve.",
				error.getMessage());
	}

	@Test
	void testRevise() {
		Part part = createTestPart("PART01", "V1", 1);
		part.setLifeCycleState("RELEASE");

		when(partDao.get(eq("PART01"), eq("V20"), eq(1))).thenReturn(part);
		when(lifeCycleTemplate.isFinal(any())).thenReturn(true);
		when(lifeCycleTemplate.getInitialState()).thenReturn("BETA");
		when(versionSchema.getNextVersionLabel(anyString())).thenReturn("PHASE II V0");

		partService.revise("TEST", "PART01", "V20", 1);

		ArgumentCaptor<Part> nextPartVersionArg = ArgumentCaptor.forClass(Part.class);
		verify(partDao, atLeastOnce()).create(nextPartVersionArg.capture());

		Part nextPartVersion = nextPartVersionArg.getValue();
		assertEquals("PHASE II V0", nextPartVersion.getVersion());
		assertEquals("PART01", nextPartVersion.getReference());
		assertEquals(1, nextPartVersion.getIteration());
		assertEquals("BETA", nextPartVersion.getLifeCycleState());
		assertNull(nextPartVersion.getReservedBy());
		assertFalse(nextPartVersion.isReserved());
		assertEquals("ATTRIBUTE 1", nextPartVersion.getPartAttribute1());
		assertEquals("ATTRIBUTE 2", nextPartVersion.getPartAttribute2());
		assertEquals(part.getLifeCycleTemplate(), nextPartVersion.getLifeCycleTemplate());
		assertEquals(part.getVersionSchema(), nextPartVersion.getVersionSchema());
	}

	@Test
	void testReviseAReservedPart() {
		// arrange
		Part part = createTestPart("PART01", "V1", 1);
		part.setReservedBy("TEST");
		part.setReserved(true);

		when(partDao.get(eq("PART01"), eq("V1"), eq(1))).thenReturn(part);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			partService.revise("TEST", "PART01", "V1", 1);
		});

		assertEquals("Part PART01 is reserved. Reserved Part is not allowed to revise.", error.getMessage());
	}

	@Test
	void testReviseWithoutFinalState() {
		// arrange
		Part part = createTestPart("PART01", "V1", 1);
		part.setLifeCycleState("RELEASE");

		when(partDao.get(eq("PART01"), eq("V20"), eq(1))).thenReturn(part);
		when(lifeCycleTemplate.isFinal(any())).thenReturn(false);
		when(lifeCycleTemplate.getInitialState()).thenReturn("BETA");

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			partService.revise("TEST", "PART01", "V20", 1);
		});

		assertEquals("Part PART01 is not in final lifecycle state. Only Part in final state is allowed to revise.",
				error.getMessage());
	}

	@Test
	void testSetState() {
		// arrange
		Part part = createTestPart("PART01", "V1", 1);

		when(lifeCycleTemplate.isKnown(any())).thenReturn(true);
		when(partDao.get(eq("PART01"), eq("V1"), eq(1))).thenReturn(part);

		partService.setState("TEST", "PART01", "V1", 1, "INITIAL");

		ArgumentCaptor<Part> updatedPartArg = ArgumentCaptor.forClass(Part.class);
		verify(partDao, atLeastOnce()).update(updatedPartArg.capture());

		Part updatedPart = updatedPartArg.getValue();
		assertEquals("V1", updatedPart.getVersion());
		assertEquals("PART01", updatedPart.getReference());
		assertEquals(1, updatedPart.getIteration());
		assertEquals("INITIAL", updatedPart.getLifeCycleState());
		assertNull(updatedPart.getReservedBy());
		assertFalse(updatedPart.isReserved());
		assertEquals("ATTRIBUTE 1", updatedPart.getPartAttribute1());
		assertEquals("ATTRIBUTE 2", updatedPart.getPartAttribute2());
		assertEquals(part.getLifeCycleTemplate(), updatedPart.getLifeCycleTemplate());
		assertEquals(part.getVersionSchema(), updatedPart.getVersionSchema());
	}

	@Test
	void testSetStateFinalState() {
		// arrange
		Part part = createTestPart("PART01", "V1", 1);

		when(partDao.get(eq("PART01"), eq("V1"), eq(1))).thenReturn(part);
		when(lifeCycleTemplate.isKnown(anyString())).thenReturn(true);
		when(lifeCycleTemplate.isFinal(anyString())).thenReturn(true);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			partService.setState("TEST", "PART01", "V1", 1, "INITIAL");
		});

		assertEquals("Part PART01 is in final lifecycle state. Part in final lifecycle is not allowed to change_state.",
				error.getMessage());
	}

	@Test
	void testSetStateReserved() {
		// arrange
		Part part = createTestPart("PART01", "V1", 1);
		part.setReserved(true);
		part.setReservedBy("TEST");

		when(partDao.get(eq("PART01"), eq("V1"), eq(1))).thenReturn(part);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			partService.setState("TEST", "PART01", "V1", 1, "INITIAL");
		});

		assertEquals("Part PART01 is reserved. Reserved Part is not allowed to change_state.", error.getMessage());
	}

	@Test
	void testSetStateWithUnkownState() {
		// arrange
		Part part = createTestPart("PART01", "V1", 1);

		when(partDao.get(eq("PART01"), eq("V1"), eq(1))).thenReturn(part);
		when(lifeCycleTemplate.isKnown(anyString())).thenReturn(false);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			partService.setState("TEST", "PART01", "V1", 1, "UNKNOWN");
		});

		assertEquals("State UNKNOWN is not allowed lifecycle state for Part.", error.getMessage());
	}

	@Test
	void testUpdate() {
		// arrange
		Part part = createTestPart("PART01", "V1", 1);
		part.setReserved(true);
		part.setReservedBy("TEST");

		when(partDao.get(eq("PART01"), eq("V1"), eq(1))).thenReturn(part);

		// act
		partService.update("TEST", "PART01", "V1", 1, "UPDATED ATTRIBUTE 1", "UPDATED ATTRIBUTE 2");

		// assert
		ArgumentCaptor<Part> updatedPartArg = ArgumentCaptor.forClass(Part.class);
		verify(partDao, atLeastOnce()).update(updatedPartArg.capture());

		Part updatedPart = updatedPartArg.getValue();
		assertEquals("V1", updatedPart.getVersion());
		assertEquals("PART01", updatedPart.getReference());
		assertEquals(1, updatedPart.getIteration());
		assertEquals("BETA", updatedPart.getLifeCycleState());
		assertEquals("TEST", updatedPart.getReservedBy());
		assertTrue(updatedPart.isReserved());
		assertEquals("UPDATED ATTRIBUTE 1", updatedPart.getPartAttribute1());
		assertEquals("UPDATED ATTRIBUTE 2", updatedPart.getPartAttribute2());
		assertEquals(part.getLifeCycleTemplate(), updatedPart.getLifeCycleTemplate());
		assertEquals(part.getVersionSchema(), updatedPart.getVersionSchema());
	}

	@Test
	void testUpdateNotReserved() {
		// arrange
		Part part = createTestPart("PART01", "V1", 1);

		when(partDao.get(eq("PART01"), eq("V1"), eq(1))).thenReturn(part);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			partService.update("TEST", "PART01", "V1", 1, "UPDATED ATTRIBUTE 1", "UPDATED ATTRIBUTE 2");
		});

		assertEquals("Part PART01 is not reserved.", error.getMessage());
	}

	@Test
	void testUpdateWithDifferentUser() {
		// arrange
		Part part = createTestPart("PART01", "V1", 1);
		part.setReserved(true);
		part.setReservedBy("TEST");

		when(partDao.get(eq("PART01"), eq("V1"), eq(1))).thenReturn(part);

		// act & assert
		RuntimeException error = assertThrows(RuntimeException.class, () -> {
			partService.update("ANOTHER_USUER_TEST", "PART01", "V1", 1, "UPDATED ATTRIBUTE 1", "UPDATED ATTRIBUTE 2");
		});

		assertEquals("Part PART01 is reserved by other user. Only user who reserve the Part can update it.",
				error.getMessage());
	}
}
