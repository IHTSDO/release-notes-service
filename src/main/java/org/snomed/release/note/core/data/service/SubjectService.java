package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.ihtsdo.otf.rest.exception.*;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
		if (subject.getId() != null && exists(subject.getId())) {
			throw new EntityAlreadyExistsException("Subject with id '" + subject.getId() + "' already exists");
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

	public List<Subject> find(final String path) {
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

	public boolean exists(final String id) {
		return subjectRepository.existsById(id);
	}

	public void joinSubjects(List<LineItem> lineItems) {
		Set<String> subjectIds = lineItems.stream().map(LineItem::getSubjectId).collect(Collectors.toSet());
		Iterable<Subject> results = subjectRepository.findAllById(subjectIds);
		Map<String, Subject> subjectIdMap = new HashMap<>();
		if (results != null) {
			results.forEach(subject -> {
				subjectIdMap.put(subject.getId(), subject);
			});
		}
		lineItems.stream().forEach(lineItem -> {
			lineItem.setSubject(subjectIdMap.get(lineItem.getSubjectId()));
		});
	}

	public void joinSubject(LineItem lineItem) {
		Optional<Subject> subject = subjectRepository.findById(lineItem.getSubjectId());
		if (subject.isPresent()) {
			lineItem.setSubject(subject.get());
		}
	}
}
