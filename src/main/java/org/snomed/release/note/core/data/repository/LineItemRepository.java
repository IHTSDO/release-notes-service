package org.snomed.release.note.core.data.repository;

import org.snomed.release.note.core.data.domain.LineItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface LineItemRepository extends ElasticsearchRepository<LineItem, Long> {
}
