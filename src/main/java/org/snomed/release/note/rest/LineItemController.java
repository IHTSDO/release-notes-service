package org.snomed.release.note.rest;

import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.rest.request.MergeRequest;
import org.snomed.release.note.rest.request.PromoteRequest;
import org.snomed.release.note.core.data.service.LineItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

	@GetMapping(value = "/{id}")
	public LineItem findLineItem(
			@PathVariable final String id) {
		return lineItemService.find(id);
	}

	@GetMapping
	public List<LineItem> findLineItems(
			@RequestParam(required = false) String subjectTitle,
			@RequestParam(required = false) String subjectId,
			@RequestParam(required = false) String sourceBranchPath,
			@RequestParam(required = false) String promotedBranchPath,
			@RequestParam(required = false) String content,
			@RequestParam(required = false) LocalDate start,
			@RequestParam(required = false) LocalDate end) {
		return lineItemService.find(subjectTitle, subjectId, sourceBranchPath, promotedBranchPath, content, start, end);
	}

	@PutMapping(value = "/{id}")
	public LineItem updateLineItem(
			@PathVariable final String id,
			@RequestBody LineItem lineItem) throws BusinessServiceException {
		if (!id.equals(lineItem.getId())) {
			throw new BadRequestException("Line item id '" + lineItem.getId() + "' does not match path id '" + id + "'");
		}
		return lineItemService.update(lineItem);
	}

	@PatchMapping(value = "/{id}/promote")
	public LineItem promoteLineItem(
			@PathVariable final String id,
			@RequestBody PromoteRequest promoteRequest) throws BusinessServiceException {
		return lineItemService.promote(id, promoteRequest);
	}

	@PostMapping(value = "{id}/release")
	public LineItem publishLineItems(
			@PathVariable final String id) throws BusinessServiceException {
		return lineItemService.release(id);
	}

	@PostMapping(value = "/merge")
	public LineItem mergeLineItems(
			@RequestBody MergeRequest mergeRequest) throws BusinessServiceException {
		return lineItemService.merge(mergeRequest);
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
