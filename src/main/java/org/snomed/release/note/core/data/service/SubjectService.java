package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubjectService {

	@Autowired
	private SubjectRepository subjectRepository;

	public Subject create(Subject subject) {
		subject.setCreatedDate(LocalDate.now());
		return subjectRepository.save(subject);
	}

	public Subject update(Subject subject) throws BusinessServiceException {
		String subjectId = subject.getId();
		if (Strings.isNullOrEmpty(subjectId)) {
			throw new BadRequestException("Subject id is required");
		}
		if (!subjectRepository.existsById(subjectId)) {
			throw new ResourceNotFoundException("No subject found for id " + subjectId);
		}
		subject.setLastModifiedDate(LocalDate.now());
		return subjectRepository.save(subject);
	}

	public Subject find(String id) {
		return subjectRepository.findById(id).orElseThrow(() ->	new ResourceNotFoundException("No subject found for id " + id));
	}

	public List<Subject> findByTitle(String title) {
		return subjectRepository.findByTitle(title);
	}

	public List<Subject> findAll() {
		List<Subject> result = new ArrayList<>();
		Iterable<Subject> foundSubjects = subjectRepository.findAll();
		foundSubjects.forEach(result::add);
		return result;
	}

	public void delete(String id) {
		if (!subjectRepository.existsById(id)) {
			throw new ResourceNotFoundException("No subject found for id " + id);
		}
		subjectRepository.deleteById(id);
	}

	public void deleteAll() {
		subjectRepository.deleteAll();
	}

}
