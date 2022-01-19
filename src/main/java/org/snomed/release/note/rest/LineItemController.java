package org.snomed.release.note.rest;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriUtil;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.service.LineItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class LineItemController {

	@Autowired
	private LineItemService lineItemService;

	@PostMapping(value = "/{path}/lineitems")
	public LineItem createLineItem(
			@PathVariable String path,
			@RequestBody LineItem lineItem) throws BusinessServiceException {
		return lineItemService.create(lineItem, BranchPathUriUtil.decodePath(path));
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
	public LineItem updateLineItem(
			@PathVariable String path,
			@PathVariable String id,
			@RequestBody LineItem lineItem) throws BusinessServiceException {
		if (!id.equals(lineItem.getId())) {
			throw new BadRequestException("'id' in the request body: '" + lineItem.getId() + "' does not match the one in the path: '" + id + "'");
		}
		return lineItemService.update(lineItem, BranchPathUriUtil.decodePath(path));
	}

	@PostMapping(value = "/{path}/lineitems/{id}/promote")
	public LineItem promoteLineItem(
			@PathVariable String path,
			@PathVariable String id) throws BusinessServiceException {
		return lineItemService.promote(id, BranchPathUriUtil.decodePath(path));
	}

	@PostMapping(value = "/{path}/lineitems/promote")
	public List<LineItem> promoteLineItems(
			@PathVariable String path) throws BusinessServiceException {
		return lineItemService.promote(BranchPathUriUtil.decodePath(path));
	}

	/*@PostMapping(value = "/{path}/lineitems/publish")
	public void publishLineItems(
			@PathVariable String path,
			@RequestBody PublishRequest publishRequest) throws BusinessServiceException {
		if (Strings.isNullOrEmpty(publishRequest.getVersion())) {
			throw new BadRequestException("'version' is required");
		}
		lineItemService.publish(path, publishRequest.getVersion());
	}*/

	@DeleteMapping(value = "/{path}/lineitems/{id}")
	public void deleteLineItem(
			@PathVariable String path,
			@PathVariable String id) {
		lineItemService.delete(id, BranchPathUriUtil.decodePath(path));
	}

}
