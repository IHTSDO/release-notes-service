package org.snomed.release.note.rest;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriUtil;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class SubjectController {

	@Autowired
	private SubjectService subjectService;

	@PostMapping(value = "/{path}/subjects")
	public ResponseEntity<Subject> createSubject(
			@PathVariable String path,
			@RequestBody Subject subject) throws BusinessServiceException {
		return new ResponseEntity<>(subjectService.create(subject, BranchPathUriUtil.decodePath(path)), HttpStatus.CREATED);
	}

	@GetMapping(value = "/{path}/subjects/{id}")
	public ResponseEntity<Subject> findSubject(
			@PathVariable String path,
			@PathVariable String id) {
		return new ResponseEntity<>(subjectService.find(id, BranchPathUriUtil.decodePath(path)), HttpStatus.OK);
	}

	@GetMapping(value = "/{path}/subjects")
	public List<Subject> findSubjects(
			@PathVariable String path) {
		return subjectService.findByPath(BranchPathUriUtil.decodePath(path));
	}

	@PutMapping(value = "/{path}/subjects/{id}")
	public Subject updateSubject(
			@PathVariable String path,
			@PathVariable String id,
			@RequestBody Subject subject) throws BusinessServiceException {
		if (!id.equals(subject.getId())) {
			throw new BadRequestException("'id' in the request body: '" + subject.getId() + "' does not match the one in the path: '" + id + "'");
		}
		return subjectService.update(subject, BranchPathUriUtil.decodePath(path));
	}

}