package org.snomed.release.note.core.data.repository;

import org.snomed.release.note.core.data.domain.Attachment;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface AttachmentRepository extends ElasticsearchRepository<Attachment, String> {

	List<Attachment> findBySourceBranch(String sourceBranch);

	List<Attachment> findAllBySourceBranchAndReportType(String sourceBranch, String reportType);
}
