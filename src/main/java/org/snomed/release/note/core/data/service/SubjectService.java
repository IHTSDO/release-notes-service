package org.snomed.release.note.core.data.service;

import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class SubjectService {

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	public String create(Subject subject) {
		subject.setCreatedDate(LocalDate.now());

		IndexQuery indexQuery = new IndexQueryBuilder()
				.withId(UUID.randomUUID().toString())
				.withObject(subject)
				.build();

		return elasticsearchOperations.index(indexQuery, elasticsearchOperations.getIndexCoordinatesFor(Subject.class));
	}

	public Subject find(String id) {
		return elasticsearchOperations.get(id, Subject.class);
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

	public void update(Subject subject) {

	}

	public void delete(String id) {
		final Subject subject = elasticsearchOperations.get(id, Subject.class);
		if (subject == null) {
			throw new IllegalArgumentException("Subject with id = " + id + " does not exist");
		}
		subjectRepository.delete(subject);
	}

	public void deleteAll() {
		subjectRepository.deleteAll();
	}

}
