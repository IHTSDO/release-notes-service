package org.snomed.release.note.core.data.repository;

import org.snomed.release.note.core.data.domain.LineItem;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.time.LocalDate;
import java.util.List;

public interface LineItemRepository extends ElasticsearchRepository<LineItem, String> {

	List<LineItem> findBySubjectId(String subjectId);

	List<LineItem> findBySourceBranch(String sourceBranch);
}
