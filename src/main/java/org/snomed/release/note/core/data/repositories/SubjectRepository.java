package org.snomed.release.note.core.data.repositories;

import org.snomed.release.note.core.data.domain.Subject;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SubjectRepository extends ElasticsearchRepository<Subject, String> {
}
