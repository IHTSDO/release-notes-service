package org.snomed.release.note.rest;

import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.service.SubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(produces = "application/json")
public class SubjectController {

	@Autowired
	private SubjectService subjectService;

	@PostMapping(value = "/subjects")
	public Subject createSubject(
			@RequestBody Subject subject) {
		return subjectService.create(subject);
	}

	@GetMapping(value = "/subjects")
	public List<Subject> findSubjects() {
		return subjectService.findAll();
	}

	@GetMapping(value = "/subjects/{id}")
	public Subject findSubject(
			@PathVariable("id") String id) {
		return subjectService.find(id);
	}

	@PutMapping(value = "/subjects/{id}")
	public Subject updateSubject(
			@RequestBody Subject subject) {
		return subjectService.update(subject);
	}

	@DeleteMapping("/subjects/{id}")
	public void deleteSubject(
			@PathVariable("id") String id) {
		subjectService.delete(id);
	}

	@DeleteMapping(value = "/subjects")
	public void deleteSubjects() {
		subjectService.deleteAll();
	}

}