package plm.services.commands;

import org.springframework.stereotype.Service;

import plm.services.contracts.DocumentAdapter;
import plm.services.rules.Rules;

@Service
public class DocumentCommandFactory {
	public ServiceCommandBuilder<DocumentAdapter> changeState(String reference, String state) {
		return new ChangeStateCommandBuilder<DocumentAdapter>(new Rules("document", "change_state", reference), state);
	}

	public ServiceCommandBuilder<DocumentAdapter> free(String reference, String userId) {
		return new FreeCommandBuilder<DocumentAdapter>(new Rules("document", "free", reference), userId);
	}

	public ServiceCommandBuilder<DocumentAdapter> reserve(String reference) {
		return new ReserveCommandBuilder<DocumentAdapter>(new Rules("document", "reserve", reference));
	}

	public ServiceCommandBuilder<DocumentAdapter> revise(String reference) {
		return new ReviseCommandBuilder<DocumentAdapter>(new Rules("document", "revise", reference));
	}

	public ServiceCommandBuilder<DocumentAdapter> update(String reference, String userId) {
		return new FreeCommandBuilder<DocumentAdapter>(new Rules("document", "update", reference), userId);
	}
}
