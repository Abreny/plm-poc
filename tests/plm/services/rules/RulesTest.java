package plm.services.rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import plm.model.LifeCycleTemplate;
import plm.services.contracts.HasLifeCycleState;
import plm.services.contracts.HasReservedState;

class RulesTest {
	private Rules rules;

	@BeforeEach
	void setUp() {
		this.rules = new Rules("test", "reserve", "TEST01");
	}

	@Test
	void testFinalState() {
		HasLifeCycleState lifeCycleState = mock(HasLifeCycleState.class);
		LifeCycleTemplate lifeCycleTemplate = mock(LifeCycleTemplate.class);

		when(lifeCycleState.getLifeCycleTemplate()).thenReturn(lifeCycleTemplate);
		when(lifeCycleState.getLifeCycleState()).thenReturn("RELEASE");
		when(lifeCycleTemplate.isFinal(eq("RELEASE"))).thenReturn(true);

		var finalState = rules.finalState();
		var notFinalState = rules.notFinalState();

		assertTrue(finalState.isValid(lifeCycleState));
		assertFalse(notFinalState.isValid(lifeCycleState));
		assertEquals("test.reserve.state.final.not", notFinalState.getAllFailed().get(0).getCode());
		assertEquals("Test TEST01 is in final lifecycle state. Test in final lifecycle is not allowed to reserve.",
				notFinalState.getAllFailed().get(0).getDefaultMessage());
	}

	@Test
	void testKnownState() {
		HasLifeCycleState lifeCycleState = mock(HasLifeCycleState.class);
		LifeCycleTemplate lifeCycleTemplate = mock(LifeCycleTemplate.class);

		when(lifeCycleState.getLifeCycleTemplate()).thenReturn(lifeCycleTemplate);
		when(lifeCycleState.getLifeCycleState()).thenReturn("BETA");
		when(lifeCycleTemplate.isKnown(anyString())).thenReturn(false);

		var unkownState = rules.unknownState("UNKOWN");
		var knownState = rules.knownState("BETA");

		assertTrue(unkownState.isValid(lifeCycleState));
		assertFalse(knownState.isValid(lifeCycleState));
		assertEquals("test.reserve.state.known", knownState.getAllFailed().get(0).getCode());
		assertEquals("State BETA is not allowed lifecycle state for Test.",
				knownState.getAllFailed().get(0).getDefaultMessage());
	}

	@Test
	void testNotFinalState() {
		HasLifeCycleState lifeCycleState = mock(HasLifeCycleState.class);
		LifeCycleTemplate lifeCycleTemplate = mock(LifeCycleTemplate.class);

		when(lifeCycleState.getLifeCycleTemplate()).thenReturn(lifeCycleTemplate);
		when(lifeCycleState.getLifeCycleState()).thenReturn("BETA");
		when(lifeCycleTemplate.isFinal(eq("RELEASE"))).thenReturn(true);

		var finalState = rules.finalState();
		var notFinalState = rules.notFinalState();

		assertFalse(finalState.isValid(lifeCycleState));
		assertTrue(notFinalState.isValid(lifeCycleState));
		assertEquals("test.reserve.state.final", finalState.getAllFailed().get(0).getCode());
		assertEquals("Test TEST01 is not in final lifecycle state. Only Test in final state is allowed to reserve.",
				finalState.getAllFailed().get(0).getDefaultMessage());
	}

	@Test
	void testNotReserved() {
		HasReservedState reservedState = mock(HasReservedState.class);

		var reserved = rules.reserved();
		var notReserved = rules.notReserved();

		assertFalse(reserved.isValid(reservedState));
		assertTrue(notReserved.isValid(reservedState));
		assertEquals("test.reserve.reserved", reserved.getAllFailed().get(0).getCode());
		assertEquals("Test TEST01 is not reserved.", reserved.getAllFailed().get(0).getDefaultMessage());
	}

	@Test
	void testReserved() {
		HasReservedState reservedState = mock(HasReservedState.class);

		when(reservedState.getReservedBy()).thenReturn("TEST");
		when(reservedState.isReserved()).thenReturn(true);

		var reserved = rules.reserved();
		var notReserved = rules.notReserved();

		assertTrue(reserved.isValid(reservedState));
		assertFalse(notReserved.isValid(reservedState));
		assertEquals("test.reserve.reserved.not", notReserved.getAllFailed().get(0).getCode());
		assertEquals("Test TEST01 is reserved. Reserved Test is not allowed to reserve.",
				notReserved.getAllFailed().get(0).getDefaultMessage());
	}

	@Test
	void testReservedBy() {
		HasReservedState reservedState = mock(HasReservedState.class);

		when(reservedState.getReservedBy()).thenReturn("TEST");
		when(reservedState.isReserved()).thenReturn(true);

		var reservedByTest = rules.reservedBy("TEST");
		var reservedByOther = rules.reservedBy("OTHER");

		assertTrue(reservedByTest.isValid(reservedState));
		assertFalse(reservedByOther.isValid(reservedState));
		assertEquals("test.reserve.reserved.not", reservedByOther.getAllFailed().get(0).getCode());
		assertEquals("Test TEST01 is reserved by other user. Only user who reserve the Test can reserve it.",
				reservedByOther.getAllFailed().get(0).getDefaultMessage());
	}

	@Test
	void testUnkownState() {
		HasLifeCycleState lifeCycleState = mock(HasLifeCycleState.class);
		LifeCycleTemplate lifeCycleTemplate = mock(LifeCycleTemplate.class);

		when(lifeCycleState.getLifeCycleTemplate()).thenReturn(lifeCycleTemplate);
		when(lifeCycleState.getLifeCycleState()).thenReturn("BETA");
		when(lifeCycleTemplate.isKnown(anyString())).thenReturn(true);

		var unkownState = rules.unknownState("UNKOWN");
		var knownState = rules.knownState("BETA");

		assertFalse(unkownState.isValid(lifeCycleState));
		assertTrue(knownState.isValid(lifeCycleState));
		assertEquals("test.reserve.state.known.not", unkownState.getAllFailed().get(0).getCode());
		assertEquals("State UNKOWN is allowed lifecycle state for Test.",
				unkownState.getAllFailed().get(0).getDefaultMessage());
	}
}
