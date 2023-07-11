package org.snomed.release.note.rest;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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
@Api(tags = "Line Items")
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
			@PathVariable String path,
			@RequestParam(required = false) String title) {
		String branch = BranchPathUriUtil.decodePath(path);
		return (title != null) ? lineItemService.findByTitle(title, branch) : lineItemService.findOrderedLineItems(branch);
	}

	@GetMapping(value = "/{path}/lineitems/{id}/children")
	public List<LineItem> getChildren(
			@PathVariable String path,
			@PathVariable String id) {
		return lineItemService.getChildren(id, BranchPathUriUtil.decodePath(path));
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
	@ApiOperation(value = "Promote task line item", notes = "'id' - line item id, 'path' - task branch")
	public ResponseEntity<String> promoteTaskLineItem(
			@PathVariable String path,
			@PathVariable String id) throws BusinessServiceException {
		lineItemService.promote(id, BranchPathUriUtil.decodePath(path));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "/{path}/lineitems/promote")
	@PreAuthorize("hasPermission('AUTHOR', #path)")
	@ApiOperation(value = "Promote project line item(s)", notes = "'path' is a project branch")
	public ResponseEntity<String> promoteProjectLineItems(
			@PathVariable String path) throws BusinessServiceException {
		lineItemService.promote(BranchPathUriUtil.decodePath(path));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "/{path}/lineitems/version")
	@PreAuthorize("hasPermission('PROJECT_MANAGER', #path) || hasPermission('RELEASE_ADMIN', #path) || hasPermission('RELEASE_MANAGER', #path)")
	@ApiOperation(value = "Version line items", notes = "Move all line items from the code system branch 'path' to a release branch 'path/effectiveTime', e.g. 'MAIN/2022-01-31'")
	public ResponseEntity<String> versionLineItem(
			@PathVariable String path,
			@RequestBody VersionRequest versionRequest) throws BusinessServiceException {
		lineItemService.version(BranchPathUriUtil.decodePath(path), versionRequest);
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@PostMapping(value = "/{path}/lineitems/publish")
	@PreAuthorize("hasPermission('RELEASE_ADMIN', #path) || hasPermission('RELEASE_MANAGER', #path)")
	@ApiOperation(value = "Publish versioned line items", notes = "Set 'released' flag to 'true' for versioned line items on the release branch 'path'")
	public ResponseEntity<String> publishLineItems(
			@PathVariable String path) throws BusinessServiceException {
		lineItemService.publish(BranchPathUriUtil.decodePath(path));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@GetMapping(value = "/{path}/lineitems/published")
	public List<LineItem> getPublishedLineItems(
			@PathVariable String path,
			@RequestParam(defaultValue = "true") boolean ordered) {
		return lineItemService.findPublished(BranchPathUriUtil.decodePath(path), ordered);
	}

	@GetMapping(value = "/{path}/lineitems/unpublished")
	public List<LineItem> getUnpublishedLineItems(
			@PathVariable String path,
			@RequestParam(defaultValue = "true") boolean ordered) {
		return lineItemService.findUnpublished(BranchPathUriUtil.decodePath(path), ordered);
	}

	@GetMapping(value = "/{path}/lineitems/categories")
	@ApiOperation("Retrieve available categories for content development.")
	public List<String> getCategories(
			@PathVariable String path) {
		return lineItemService.findCategories(BranchPathUriUtil.decodePath(path));
	}

	@GetMapping(value = "/{path}/lineitems/versions")
	@ApiOperation("Retrieve existing versions for a given code system branch 'path'.")
	public List<String> getVersions(
			@PathVariable String path) throws BusinessServiceException {
		return lineItemService.getVersions(BranchPathUriUtil.decodePath(path));
	}

	@DeleteMapping(value = "/{path}/lineitems/{id}")
	@PreAuthorize("hasPermission('AUTHOR', #path) || hasPermission('RELEASE_ADMIN', #path) || hasPermission('RELEASE_LEAD', #path) || hasPermission('PROJECT_LEAD', #path)")
	public void deleteLineItem(
			@PathVariable String path,
			@PathVariable String id) {
		lineItemService.delete(id, BranchPathUriUtil.decodePath(path));
	}
}
