package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class SubjectService {

	@Autowired
	private SubjectRepository subjectRepository;

	public Subject create(Subject subject) {
		subject.setId(UUID.randomUUID().toString());
		subject.setCreatedDate(LocalDate.now());
		return subjectRepository.save(subject);
	}

	public Subject update(Subject subject) {
		String subjectId = subject.getId();

		if (Strings.isNullOrEmpty(subjectId)) {
			throw new IllegalArgumentException("id is required");
		}
		if (!subjectRepository.existsById(subjectId)) {
			throw new NoSuchElementException("Subject '" + subjectId + "' does not exist");
		}
		subject.setLastModifiedDate(LocalDate.now());
		return subjectRepository.save(subject);
	}

	public Subject find(String id) {
		return subjectRepository.findById(id).orElseThrow(() ->
				new NoSuchElementException("Subject '" + id + "' is not found"));
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
			throw new NoSuchElementException("Subject '" + id + "' does not exist");
		}
		subjectRepository.deleteById(id);
	}

	public void deleteAll() {
		subjectRepository.deleteAll();
	}

}
