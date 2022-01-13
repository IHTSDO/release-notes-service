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

	public Subject create(Subject subject, final String path) throws BusinessServiceException {
		if (Strings.isNullOrEmpty(subject.getTitle())) {
			throw new BadRequestException("'title' is required");
		}
		subject.setPath(path);
		subject.setCreatedDate(LocalDate.now());
		return subjectRepository.save(subject);
	}

	public Subject update(final Subject subject, final String path) {
		Subject existing = find(subject.getId(), path);
		existing.setTitle(subject.getTitle());
		existing.setLastModifiedDate(LocalDate.now());
		return subjectRepository.save(existing);
	}

	public Subject find(final String id, final String path) {
		final Subject subject = subjectRepository.findById(id).orElseThrow(() ->
				new ResourceNotFoundException("No subject found for id '" + id +"'"));
		if (!path.equals(subject.getPath())) {
			throw new ResourceNotFoundException("No subject found for id '" + id + "' and path '" + path + "'");
		}
		return subject;
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

	public void delete(final String id, final String path) {
		Subject subject = find(id, path);
		subjectRepository.delete(subject);
	}

	public void deleteAll() {
		subjectRepository.deleteAll();
	}

}
