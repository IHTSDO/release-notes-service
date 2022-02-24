package org.snomed.release.note.core.data.repository;

import org.snomed.release.note.core.data.domain.Subject;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface SubjectRepository extends ElasticsearchRepository<Subject, String> {

	List<Subject> findByPath(String path);
}
