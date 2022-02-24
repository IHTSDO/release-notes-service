package org.snomed.release.note.rest;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriUtil;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.service.LineItemService;
import org.snomed.release.note.rest.pojo.LineItemCreateRequest;
import org.snomed.release.note.rest.pojo.LineItemUpdateRequest;
import org.snomed.release.note.rest.pojo.VersionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class LineItemController {

	@Autowired
	private LineItemService lineItemService;

	@PostMapping(value = "/{path}/lineitems")
	@PreAuthorize("hasPermission('AUTHOR', #path)")
	public LineItem createLineItem(
			@PathVariable String path,
			@RequestBody LineItemCreateRequest lineItemCreateRequest) throws BusinessServiceException {
		return lineItemService.create(lineItemCreateRequest, BranchPathUriUtil.decodePath(path));
	}

	@GetMapping(value = "/{path}/lineitems/{id}")
	public LineItem findLineItem(
			@PathVariable String path,
			@PathVariable String id) {
		return lineItemService.find(id, BranchPathUriUtil.decodePath(path));
	}

	@GetMapping(value = "/{path}/lineitems")
	public List<LineItem> findLineItems(
			@PathVariable String path) {
		return lineItemService.findOrderedLineItems(BranchPathUriUtil.decodePath(path));
	}

	@PutMapping(value = "/{path}/lineitems/{id}")
	@PreAuthorize("hasPermission('AUTHOR', #path)")
	public LineItem updateLineItem(
			@PathVariable String path,
			@PathVariable String id,
			@RequestBody LineItemUpdateRequest lineItemUpdateRequest) throws BusinessServiceException {
		if (!id.equals(lineItemUpdateRequest.getId())) {
			throw new BadRequestException("'id' in the request body: '" + lineItemUpdateRequest.getId() + "' does not match the one in the path: '" + id + "'");
		}
		return lineItemService.update(lineItemUpdateRequest, BranchPathUriUtil.decodePath(path));
	}

	@PostMapping(value = "/{path}/lineitems/{id}/promote")
	@PreAuthorize("hasPermission('AUTHOR', #path)")
	public ResponseEntity<String> promoteTaskLineItem(
			@PathVariable String path,
			@PathVariable String id) throws BusinessServiceException {
		String branchPath = BranchPathUriUtil.decodePath(path);
		lineItemService.promote(id, BranchPathUriUtil.decodePath(path));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "/{path}/lineitems/promote")
	@PreAuthorize("hasPermission('AUTHOR', #path)")
	public ResponseEntity<String> promoteProjectLineItems(
			@PathVariable String path) throws BusinessServiceException {
		String branchPath = BranchPathUriUtil.decodePath(path);
		lineItemService.promote(branchPath);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "/{path}/lineitems/version")
	@PreAuthorize("hasPermission('RELEASE_ADMIN', #path) || hasPermission('RELEASE_MANAGER', #path)")
	public ResponseEntity<String> versionLineItem(
			@PathVariable String path,
			@RequestBody VersionRequest versionRequest) throws BusinessServiceException {
		lineItemService.version(path, versionRequest);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "/{path}/lineitems/publish")
	@PreAuthorize("hasPermission('RELEASE_ADMIN', #path) || hasPermission('RELEASE_MANAGER', #path)")
	public ResponseEntity<String> publishLineItems(
			@PathVariable String path) throws BusinessServiceException {
		lineItemService.publish(path);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/{path}/published-lineitems")
	public List<LineItem> getPublishedLineItems(
			@PathVariable String path,
			@RequestParam(defaultValue = "true") boolean ordered ) {
		return lineItemService.findPublished(path, ordered);
	}

	@DeleteMapping(value = "/{path}/lineitems/{id}")
	@PreAuthorize("hasPermission('RELEASE_ADMIN', #path) || hasPermission('RELEASE_LEAD', #path)")
	public void deleteLineItem(
			@PathVariable String path,
			@PathVariable String id) {
		lineItemService.delete(id, BranchPathUriUtil.decodePath(path));
	}

}
