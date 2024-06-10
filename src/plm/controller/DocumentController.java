package plm.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import plm.services.DocumentService;

public class DocumentController {

	@Autowired
	private DocumentService documentService;

	@GetMapping(value = "/Document/free")
	public void free(@RequestHeader("userId") String userId, @RequestParam("reference") String reference,
			@RequestParam("version") String version, @RequestParam("iteration") int iteration) {

		documentService.free(userId, reference, version, iteration);
	}

	@GetMapping(value = "/Document/reserve")
	public void reserve(@RequestHeader("userId") String userId, @RequestParam("reference") String reference,
			@RequestParam("version") String version, @RequestParam("iteration") int iteration) {

		documentService.reserve(userId, reference, version, iteration);
	}

	@GetMapping(value = "/Document/revise")
	public void revise(@RequestHeader("userId") String userId, @RequestParam("reference") String reference,
			@RequestParam("version") String version, @RequestParam("iteration") int iteration) {

		documentService.revise(userId, reference, version, iteration);
	}

	@GetMapping(value = "/Document/free")
	public void setState(@RequestHeader("userId") String userId, @RequestParam("reference") String reference,
			@RequestParam("version") String version, @RequestParam("iteration") int iteration,
			@RequestParam("state") String state) {

		documentService.setState(userId, reference, version, iteration, state);
	}

	@GetMapping(value = "/Document/update")
	public void update(@RequestHeader("userId") String userId, @RequestParam("reference") String reference,
			@RequestParam("version") String version, @RequestParam("iteration") int iteration,
			@RequestParam("documentAttribute1") String documentAttribute1,
			@RequestParam("documentAttribute2") String documentAttribute2) {
		//
		documentService.update(userId, reference, version, iteration, documentAttribute1, documentAttribute2);
	}

}
