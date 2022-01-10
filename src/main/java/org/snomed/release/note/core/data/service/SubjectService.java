package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
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

	public Subject create(Subject subject) throws BusinessServiceException {
		if (Strings.isNullOrEmpty(subject.getTitle())) {
			throw new BadRequestException("title is required");
		}
		if (Strings.isNullOrEmpty(subject.getBranchPath())) {
			throw new BadRequestException("branchPath is required");
		}
		subject.setCreatedDate(LocalDate.now());
		return subjectRepository.save(subject);
	}

	public Subject update(final Subject subject) {
		Subject existingSubject = find(subject.getId());
		existingSubject.setTitle(subject.getTitle());
		existingSubject.setBranchPath(subject.getBranchPath());
		existingSubject.setLastModifiedDate(LocalDate.now());
		return subjectRepository.save(existingSubject);
	}

	public Subject find(final String id) {
		return subjectRepository.findById(id).orElseThrow(() ->	new ResourceNotFoundException("No subject found for id " + id));
	}

	public List<Subject> find(final String title, final String branchPath) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		if (title != null) {
			boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.termQuery("title", title));
		}
		if (branchPath != null) {
			boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.termQuery("branchPath", branchPath));
		}
		Query query = new NativeSearchQueryBuilder()
				.withQuery(QueryBuilders.matchQuery("branchPath", branchPath))//boolQueryBuilder)
				.build();
		SearchHits<Subject> searchHits = elasticsearchOperations.search(query, Subject.class);
		return searchHits.get().map(SearchHit::getContent).collect(Collectors.toList());
	}

	public List<Subject> findAll() {
		List<Subject> result = new ArrayList<>();
		Iterable<Subject> foundSubjects = subjectRepository.findAll();
		foundSubjects.forEach(result::add);
		return result;
	}

	public void delete(final String id) {
		if (!subjectRepository.existsById(id)) {
			throw new ResourceNotFoundException("No subject found for id " + id);
		}
		subjectRepository.deleteById(id);
	}

	public void deleteAll() {
		subjectRepository.deleteAll();
	}

}
