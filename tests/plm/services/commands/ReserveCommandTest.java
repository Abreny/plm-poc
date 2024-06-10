package plm.services.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import plm.model.Document;
import plm.model.LifeCycleTemplate;
import plm.model.Part;
import plm.services.contracts.DocumentAdapter;
import plm.services.contracts.PartAdapter;
import plm.services.rules.Rule;
import plm.services.rules.RuleViolationException;
import plm.services.rules.Rules;

class ReserveCommandTest {
	private ReserveCommandBuilder<DocumentAdapter> documentCommand;
	private LifeCycleTemplate lifeCycleTemplate;

	private ReserveCommandBuilder<PartAdapter> partCommand;

	@SuppressWarnings("unchecked")
	private <T> Consumer<T> createFakeConsumer() {
		return (Consumer<T>) mock(Consumer.class);
	}

	private LifeCycleTemplate createFakeLifeCycleTenplate() {
		LifeCycleTemplate lifeCycleTemplate = mock(LifeCycleTemplate.class);
		when(lifeCycleTemplate.isFinal(eq("RELEASE"))).thenReturn(true);
		when(lifeCycleTemplate.isKnown(any())).thenReturn(true);
		return lifeCycleTemplate;
	}

	private boolean isLinkedToPart(Document doc) {
		return true;
	}

	@BeforeEach
	void setUp() {
		partCommand = new ReserveCommandBuilder<>(new Rules("part", "reserve", "PART01"));
		documentCommand = new ReserveCommandBuilder<>(new Rules("part", "reserve", "PART01"));

		lifeCycleTemplate = createFakeLifeCycleTenplate();
	}

	@Test
	void testReservedDocument() {
		Document document = new Document();
		document.setLifeCycleTemplate(lifeCycleTemplate);

		Consumer<DocumentAdapter> consumer = createFakeConsumer();

		ServiceCommand<DocumentAdapter> reserve = documentCommand.doExecute(consumer).disableErrorOnFail().build();

		reserve.execute(DocumentAdapter.createFromDocument(document));

		verify(consumer, times(1)).accept(any());
	}

	@Test
	void testReservedDocumentNotAllowed() {
		Document document = new Document();
		document.setReserved(true);
		document.setReservedBy("TEST");
		document.setLifeCycleState("RELEASE");
		document.setLifeCycleTemplate(lifeCycleTemplate);

		Consumer<DocumentAdapter> consumer = createFakeConsumer();

		ServiceCommand<DocumentAdapter> reserve = documentCommand
				.addRule(new Rule<>(doc -> !isLinkedToPart(doc.getDocument()), "document.reserve.linked.to.part",
						"Document linked to a part"))
				.doExecute(consumer).build();

		RuleViolationException exception = assertThrows(RuleViolationException.class, () -> {
			reserve.execute(DocumentAdapter.createFromDocument(document));
		});

		assertEquals(3, exception.getErrors().size());
		verify(consumer, never()).accept(any());
	}

	@Test
	void testReservedPart() {
		Part part = new Part();
		part.setLifeCycleTemplate(lifeCycleTemplate);

		Consumer<PartAdapter> consumer = createFakeConsumer();

		ServiceCommand<PartAdapter> reserve = partCommand.doExecute(consumer).disableErrorOnFail().build();

		reserve.execute(PartAdapter.createFromPart(part));

		verify(consumer, times(1)).accept(any());
	}

	@Test
	void testReservedPartNotAllowed() {
		Part part = new Part();
		part.setReserved(true);
		part.setReservedBy("TEST");
		part.setLifeCycleState("RELEASE");
		part.setLifeCycleTemplate(lifeCycleTemplate);

		Consumer<PartAdapter> consumer = createFakeConsumer();

		ServiceCommand<PartAdapter> reserve = partCommand.doExecute(consumer).build();

		RuleViolationException exception = assertThrows(RuleViolationException.class, () -> {
			reserve.execute(PartAdapter.createFromPart(part));
		});

		assertEquals(2, exception.getErrors().size());
		verify(consumer, never()).accept(any());
	}
}
