package org.snomed.release.note.rest;

import com.google.api.client.json.JsonString;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.service.LineItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

	@GetMapping(value = "/{id}")
	public LineItem findLineItem(
			@PathVariable final String id) {
		return lineItemService.find(id);
	}

	@GetMapping
	public List<LineItem> findLineItems(
			@RequestParam(required = false) String sourceBranch,
			@RequestParam(required = false) String promotedBranch,
			@RequestParam(required = false) LocalDate startDate,
			@RequestParam(required = false) LocalDate endDate) {
		return lineItemService.find(sourceBranch, promotedBranch, startDate, endDate);
	}

	@PutMapping(value = "/{id}")
	public LineItem updateLineItem(
			@PathVariable final String id,
			@RequestBody LineItem lineItemDetails) {
		return lineItemService.update(id, lineItemDetails);
	}

	@PatchMapping(value = "/{id}/promote")
	public LineItem promoteLineItem(
			@PathVariable final String id,
			@RequestBody Map<String, String> request) throws BusinessServiceException {
		String promotedBranch = request.get("promotedBranch");
		return lineItemService.promote(id, promotedBranch);
	}

	@PostMapping(value = "/merge")
	public LineItem mergeLineItems(
			@RequestBody Map<String, String> request) throws BusinessServiceException {
		String subjectId = request.get("subjectId");
		String sourceBranch = request.get("sourceBranch");
		return lineItemService.merge(subjectId, sourceBranch);
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
