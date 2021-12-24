package org.snomed.release.note.rest;

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
			@RequestBody Subject subject) {
		return subjectService.create(subject);
	}

	@GetMapping
	public List<Subject> findSubjects() {
		return subjectService.findAll();
	}

	@GetMapping(value = "/{id}")
	public Subject findSubject(
			@PathVariable("id") String id) {
		return subjectService.find(id);
	}

	@PutMapping(value = "/{id}")
	public Subject updateSubject(
			@RequestBody Subject subject) throws BusinessServiceException {
		return subjectService.update(subject);
	}

	@DeleteMapping(value = "/{id}")
	public void deleteSubject(
			@PathVariable("id") String id) {
		subjectService.delete(id);
	}

	@DeleteMapping()
	public void deleteSubjects() {
		subjectService.deleteAll();
	}

}