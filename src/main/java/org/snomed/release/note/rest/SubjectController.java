package org.snomed.release.note.rest;

import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/subjects", produces = MediaType.APPLICATION_JSON_VALUE)
public class SubjectController {

	@Autowired
	private SubjectService subjectService;

	@PostMapping
	public Subject createSubject(
			@RequestBody Subject subject) throws BusinessServiceException {
		return subjectService.create(subject);
	}

	@GetMapping(value = "/{id}")
	public Subject findSubject(
			@PathVariable final String id) {
		return subjectService.find(id);
	}

	@GetMapping
	public List<Subject> findSubjects(
			@RequestParam(required = false) String title,
			@RequestParam(required = false) String branchPath) {
		return subjectService.find(title, branchPath);
	}

	@PutMapping(value = "/{id}")
	public Subject updateSubject(
			@PathVariable final String id,
			@RequestBody Subject subject) throws BusinessServiceException {
		if (!id.equals(subject.getId())) {
			throw new BadRequestException("Subject id '" + subject.getId() + "' does not match path id '" + id + "'");
		}
		return subjectService.update(subject);
	}

	@DeleteMapping(value = "/{id}")
	public void deleteSubject(
			@PathVariable final String id) {
		subjectService.delete(id);
	}

	@DeleteMapping()
	public void deleteSubjects() {
		subjectService.deleteAll();
	}

}