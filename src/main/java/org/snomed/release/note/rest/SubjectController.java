package org.snomed.release.note.rest;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriUtil;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.service.SubjectService;
import org.snomed.release.note.rest.request.SubjectCreateRequest;
import org.snomed.release.note.rest.request.SubjectUpdateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class SubjectController {

	@Autowired
	private SubjectService subjectService;

	@PostMapping(value = "/{path}/subjects")
	@PreAuthorize("hasPermission('RELEASE_LEAD', #path)")
	public Subject createSubject(
			@PathVariable String path,
			@RequestBody SubjectCreateRequest subjectCreateRequest) throws BusinessServiceException {
		return subjectService.create(subjectCreateRequest, BranchPathUriUtil.decodePath(path));
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
		return subjectService.find(BranchPathUriUtil.decodePath(path));
	}

	@PutMapping(value = "/{path}/subjects/{id}")
	@PreAuthorize("hasPermission('RELEASE_LEAD', #path)")
	public Subject updateSubject(
			@PathVariable String path,
			@PathVariable String id,
			@RequestBody SubjectUpdateRequest subjectUpdateRequest) throws BusinessServiceException {
		if (!id.equals(subjectUpdateRequest.getId())) {
			throw new BadRequestException("'id' in the request body: '" + subjectUpdateRequest.getId() + "' does not match the one in the path: '" + id + "'");
		}
		return subjectService.update(subjectUpdateRequest, BranchPathUriUtil.decodePath(path));
	}

	@DeleteMapping(value = "/{path}/subjects/{id}")
	@PreAuthorize("hasPermission('RELEASE_LEAD', #path)")
	public void deleteSubject(
			@PathVariable String path,
			@PathVariable String id) {
		subjectService.delete(id, BranchPathUriUtil.decodePath(path));
	}

}