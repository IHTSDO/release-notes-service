package org.snomed.release.note.core.data.repository;

import org.snomed.release.note.core.data.domain.LineItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface LineItemRepository extends ElasticsearchRepository<LineItem, String> {

	List<LineItem> findByTitle(String title);

	List<LineItem> findBySourceBranch(String sourceBranch);

}
