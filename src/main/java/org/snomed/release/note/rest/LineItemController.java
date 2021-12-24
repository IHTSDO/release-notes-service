package org.snomed.release.note.rest;

import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.snomed.release.note.core.data.domain.LineItem;
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
			@RequestBody LineItem lineItem) throws BusinessServiceException {
		return lineItemService.create(lineItem);
	}

	@GetMapping
	public List<LineItem> findLineItems() {
		return lineItemService.findAll();
	}

	@GetMapping(value = "/{id}")
	public LineItem findLineItem(
			@PathVariable final String id) {
		return lineItemService.find(id);
	}

	// TODO: what method to use for update and promote, PUT or POST? Can we use PATCH?
	@PutMapping(value = "/{id}")
	public LineItem updateLineItem(
			@PathVariable final String id,
			@RequestBody LineItem lineItemDetails) {
		return lineItemService.update(id, lineItemDetails);
	}

	@PatchMapping(value = "/{id}/promote")
	public LineItem promoteLineItem(
			@PathVariable final String id,
			@RequestBody LineItem lineItemDetails) throws BusinessServiceException {
		return lineItemService.promote(id, lineItemDetails.getPromotedBranch());
	}

	@DeleteMapping(value = "/{id}")
	public void deleteLineItem(
			@PathVariable final String id) {
		lineItemService.delete(id);
	}

	@DeleteMapping()
	public void deleteLineItems() {
		lineItemService.deleteAll();
	}

}
