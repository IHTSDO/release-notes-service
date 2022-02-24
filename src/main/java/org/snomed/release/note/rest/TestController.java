package org.snomed.release.note.rest;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriUtil;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.snomed.release.note.core.data.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/test")
public class TestController {

	@Autowired
	private TestService testService;

	@PostMapping(value = "/{path}/testData")
	public ResponseEntity<String> createData(
			@PathVariable String path) throws BusinessServiceException {
		testService.createData(BranchPathUriUtil.decodePath(path));
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@DeleteMapping(value = "/{path}/testData")
	public ResponseEntity<String> deleteData(
			@PathVariable String path) {
		testService.deleteData(BranchPathUriUtil.decodePath(path));
		return new ResponseEntity<>(HttpStatus.OK);
	}
}
