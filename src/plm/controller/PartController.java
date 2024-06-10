package plm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import plm.services.PartService;

public class PartController {

	@Autowired
	private PartService partService;

	@GetMapping(value = "/Part/free")
	public void free(@RequestHeader("userId") String userId, @RequestParam("reference") String reference,
			@RequestParam("version") String version, @RequestParam("iteration") int iteration) {

		partService.free(userId, reference, version, iteration);
	}

	@GetMapping(value = "/Part/reserve")
	public void reserve(@RequestHeader("userId") String userId, @RequestParam("reference") String reference,
			@RequestParam("version") String version, @RequestParam("iteration") int iteration) {

		partService.reserve(userId, reference, version, iteration);
	}

	@GetMapping(value = "/Part/revise")
	public void revise(@RequestHeader("userId") String userId, @RequestParam("reference") String reference,
			@RequestParam("version") String version, @RequestParam("iteration") int iteration) {

		partService.revise(userId, reference, version, iteration);
	}

	@GetMapping(value = "/Part/free")
	public void setState(@RequestHeader("userId") String userId, @RequestParam("reference") String reference,
			@RequestParam("version") String version, @RequestParam("iteration") int iteration,
			@RequestParam("state") String state) {

		partService.setState(userId, reference, version, iteration, state);
	}

	@GetMapping(value = "/Part/update")
	public void update(@RequestHeader("userId") String userId, @RequestParam("reference") String reference,
			@RequestParam("version") String version, @RequestParam("iteration") int iteration,
			@RequestParam("partAttribute1") String partAttribute1,
			@RequestParam("partAttribute2") String partAttribute2) {
		//
		partService.update(userId, reference, version, iteration, partAttribute1, partAttribute2);
	}

}
