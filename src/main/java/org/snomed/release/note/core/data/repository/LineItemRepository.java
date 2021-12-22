package org.snomed.release.note.core.data.repository;

import org.snomed.release.note.core.data.domain.LineItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface LineItemRepository extends ElasticsearchRepository<LineItem, Long> {

    List<LineItem> findBySubjectId(String subjectId);

}
