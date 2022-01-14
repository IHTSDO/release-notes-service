package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.EntityAlreadyExistsException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
				.filter(QueryBuilders.termQuery("path.keyword", path));
		Query query = new NativeSearchQueryBuilder()
				.withQuery(boolQueryBuilder)
				.build();
		SearchHits<Subject> searchHits = elasticsearchOperations.search(query, Subject.class);
		return searchHits.get().map(SearchHit::getContent).collect(Collectors.toList());
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

}
