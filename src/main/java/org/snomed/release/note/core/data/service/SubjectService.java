package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class SubjectService {

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	public Subject create(Subject subject) throws BusinessServiceException {
		if (Strings.isNullOrEmpty(subject.getTitle())) {
			throw new BadRequestException("title is required");
		}
		if (Strings.isNullOrEmpty(subject.getPath())) {
			throw new BadRequestException("path is required");
		}
		subject.setCreatedDate(LocalDate.now());
		return subjectRepository.save(subject);
	}

	public Subject update(final Subject subject) {
		Subject existingSubject = find(subject.getId());
		existingSubject.setTitle(subject.getTitle());
		existingSubject.setPath(subject.getPath());
		existingSubject.setLastModifiedDate(LocalDate.now());
		return subjectRepository.save(existingSubject);
	}

	public Subject find(final String id) {
		return subjectRepository.findById(id).orElseThrow(() ->	new ResourceNotFoundException("No subject found for id '" + id +"'"));
	}

	public List<Subject> findByPath(final String path) {
		return subjectRepository.findByPath(path);
	}

	public List<Subject> findByTitle(final String title) {
		return subjectRepository.findByTitle(title);
	}

	public List<Subject> findAll() {
		List<Subject> result = new ArrayList<>();
		Iterable<Subject> foundSubjects = subjectRepository.findAll();
		foundSubjects.forEach(result::add);
		return result;
	}

	public void delete(final String id) {
		if (!subjectRepository.existsById(id)) {
			throw new ResourceNotFoundException("No subject found for id '" + id +"'");
		}
		subjectRepository.deleteById(id);
	}

	public void deleteAll() {
		subjectRepository.deleteAll();
	}

}
