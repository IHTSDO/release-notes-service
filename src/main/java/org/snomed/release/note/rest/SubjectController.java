package org.snomed.release.note.rest;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriUtil;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class SubjectController {

	@Autowired
	private SubjectService subjectService;

	@PostMapping(value = "/{path}/subjects")
	public Subject createSubject(
			@PathVariable String path,
			@RequestBody Subject subject) throws BusinessServiceException {
		return subjectService.create(subject, BranchPathUriUtil.decodePath(path));
	}

	@GetMapping(value = "/{path}/subjects/{id}")
	public Subject findSubject(
			@PathVariable String path,
			@PathVariable String id) {
		return subjectService.find(id, BranchPathUriUtil.decodePath(path));
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

	@DeleteMapping(value = "/{path}/subjects/{id}")
	public void deleteSubject(
			@PathVariable String path,
			@PathVariable String id) {
		subjectService.delete(id, BranchPathUriUtil.decodePath(path));
	}

}