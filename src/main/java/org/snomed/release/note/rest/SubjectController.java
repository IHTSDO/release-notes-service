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
	public String createSubject(
			@RequestBody Subject subject) {
		return subjectService.create(subject);
	}

	@GetMapping(value = "/subjects", produces = "application/json")
	public List<Subject> findSubjects() {
		return subjectService.findAll();
	}

	@GetMapping(value = "/subjects/{id}", produces = "application/json")
	public Subject findSubject(
			@PathVariable("id") String id) {
		return subjectService.find(id);
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
