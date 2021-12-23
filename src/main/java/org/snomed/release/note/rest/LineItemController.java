package org.snomed.release.note.rest;

import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.service.LineItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(produces = "application/json")
public class LineItemController {

	@Autowired
	private LineItemService lineItemService;

	@PostMapping(value = "/lineitems")
	public String createLineItem(
			@RequestBody LineItem lineItem) {
		return lineItemService.create(lineItem);
	}

	@GetMapping(value = "/lineitems", produces = "application/json")
	public List<LineItem> findLineItems() {
		return lineItemService.findAll();
	}

	@GetMapping(value = "/lineitems/{id}", produces = "application/json")
	public LineItem findLineItem(
			@PathVariable("id") String id) {
		return lineItemService.find(id);
	}

}
