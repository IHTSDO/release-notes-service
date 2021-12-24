package org.snomed.release.note.rest;

import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.service.LineItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/lineitems", produces = MediaType.APPLICATION_JSON_VALUE)
public class LineItemController {

	@Autowired
	private LineItemService lineItemService;

	@PostMapping
	public LineItem createLineItem(
			@RequestBody LineItem lineItem) {
		return lineItemService.create(lineItem);
	}

	@GetMapping
	public List<LineItem> findLineItems() {
		return lineItemService.findAll();
	}

	@GetMapping(value = "/{id}")
	public LineItem findLineItem(
			@PathVariable("id") String id) {
		return lineItemService.find(id);
	}

	@PutMapping(value = "/{id}")
	public LineItem updateLineItem(
			@RequestBody LineItem lineItem) throws BusinessServiceException {
		return lineItemService.update(lineItem);
	}

	@DeleteMapping(value = "/{id}")
	public void deleteLineItem(
			@PathVariable("id") String id) {
		lineItemService.delete(id);
	}

	@DeleteMapping()
	public void deleteLineItems() {
		lineItemService.deleteAll();
	}

}
