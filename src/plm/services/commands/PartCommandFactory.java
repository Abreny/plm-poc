package plm.services.commands;

import org.springframework.stereotype.Service;

import plm.services.contracts.PartAdapter;
import plm.services.rules.Rules;

@Service
public class PartCommandFactory {
	public ServiceCommandBuilder<PartAdapter> changeState(String reference, String state) {
		return new ChangeStateCommandBuilder<PartAdapter>(new Rules("part", "change_state", reference), state);
	}

	public ServiceCommandBuilder<PartAdapter> free(String reference, String userId) {
		return new FreeCommandBuilder<PartAdapter>(new Rules("part", "free", reference), userId);
	}

	public ServiceCommandBuilder<PartAdapter> reserve(String reference) {
		return new ReserveCommandBuilder<PartAdapter>(new Rules("part", "reserve", reference));
	}

	public ServiceCommandBuilder<PartAdapter> revise(String reference) {
		return new ReviseCommandBuilder<PartAdapter>(new Rules("part", "revise", reference));
	}

	public ServiceCommandBuilder<PartAdapter> update(String reference, String userId) {
		return new FreeCommandBuilder<PartAdapter>(new Rules("part", "update", reference), userId);
	}
}
