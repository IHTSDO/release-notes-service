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
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class SubjectController {

	@Autowired
	private SubjectService subjectService;

	@PostMapping(value = "/{path}/subjects")
	public Subject createSubject(
			@PathVariable final String path,
			@RequestBody Subject subject) throws BusinessServiceException {
		if (!path.equals(subject.getPath())) {
			throw new BadRequestException("Subject path '" + subject.getPath() + "' does not match path '" + path + "'");
		}
		return subjectService.create(subject);
	}

	@GetMapping(value = "/subjects/{id}")
	public Subject findSubject(
			@PathVariable final String id) {
		return subjectService.find(id);
	}

	@GetMapping(value = "/{path}/subjects")
	public List<Subject> findSubjects(
			@PathVariable final String path) {
		return subjectService.findByPath(path);
	}

	@GetMapping(value = "/subjects")
	public List<Subject> findSubjects() {
		return subjectService.findAll();
	}

	@PutMapping(value = "/subjects/{id}")
	public Subject updateSubject(
			@PathVariable final String id,
			@RequestBody Subject subject) throws BusinessServiceException {
		if (!id.equals(subject.getId())) {
			throw new BadRequestException("Subject id '" + subject.getId() + "' does not match id '" + id + "'");
		}
		return subjectService.update(subject);
	}

}