package org.snomed.release.note.rest;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriUtil;
import io.swagger.annotations.Api;
import org.snomed.release.note.core.data.service.LineItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@Api(tags = "Administration")
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class AdminController {

	@Autowired
	private LineItemService lineItemService;

	@PostMapping(value = "/{path}/lineitems/updateSequence")
	@PreAuthorize("hasPermission('RELEASE_ADMIN', #path)")
	public ResponseEntity<String> updateSequence(
			@PathVariable String path) {
		lineItemService.updateSequence(BranchPathUriUtil.decodePath(path));
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
