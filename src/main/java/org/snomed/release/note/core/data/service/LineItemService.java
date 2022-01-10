package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.repository.SubjectRepository;
import org.snomed.release.note.rest.request.MergeRequest;
import org.snomed.release.note.core.data.repository.LineItemRepository;
import org.snomed.release.note.rest.request.PromoteRequest;
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
public class LineItemService {

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private LineItemRepository lineItemRepository;

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	public LineItem create(LineItem lineItem) throws BusinessServiceException {
		if (Strings.isNullOrEmpty(lineItem.getSubjectId())) {
			throw new BadRequestException("subjectId is required");
		}
		if (Strings.isNullOrEmpty(lineItem.getSourceBranchPath())) {
			throw new BadRequestException("sourceBranchPath is required");
		}
		lineItem.setStart(LocalDate.now());
		return lineItemRepository.save(lineItem);
	}

	public LineItem update(final LineItem lineItem) {
		LineItem existingLineItem = find(lineItem.getId());
		existingLineItem.setSubjectId(lineItem.getSubjectId());
		existingLineItem.setParentId(lineItem.getParentId());
		existingLineItem.setLevel(lineItem.getLevel());
		existingLineItem.setContent(lineItem.getContent());
		existingLineItem.setSequence(lineItem.getSequence());
		existingLineItem.setEnd(lineItem.getEnd());
		existingLineItem.setReleased(lineItem.getReleased());
		return lineItemRepository.save(existingLineItem);
	}

	public LineItem promote(final String id, PromoteRequest promoteRequest) throws BusinessServiceException {
		if (Strings.isNullOrEmpty(promoteRequest.getBranchPath())) {
			throw new BadRequestException("branchPath is required");
		}
		LineItem lineItem = find(id);
		lineItem.setPromotedBranchPath(promoteRequest.getBranchPath());
		return lineItemRepository.save(lineItem);
	}

	public LineItem release(final String id) throws BusinessServiceException {
		LineItem lineItem = find(id);
		lineItem.setReleased(true);
		return lineItemRepository.save(lineItem);
	}

	public LineItem find(final String id) {
		return lineItemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No line item found for id " + id));
	}

	public List<LineItem> find(final String subjectTitle,
							   final String subjectId,
							   final String sourceBranchPath,
							   final String promotedBranchPath,
							   final String content,
							   final LocalDate start,
							   final LocalDate end) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
		if (subjectTitle != null) {
			List<Subject> subjects = subjectRepository.findByTitle(subjectTitle);
			String[] subjectIds = subjects.stream().map(Subject::getId).toArray(String[]::new);
			boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.termsQuery("subjectId", subjectIds));
		}
		if (subjectId != null) {
			boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.termQuery("subjectId", subjectId));
		}
		if (sourceBranchPath != null) {
			boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.termQuery("sourceBranchPath", sourceBranchPath));
		}
		if (promotedBranchPath != null) {
			boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.termQuery("promotedBranchPath", promotedBranchPath));
		}
		if (content != null) {
			boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.matchQuery("content", content));
		}
		if (start != null) {
			boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.matchQuery("start", start));
		}
		if (end != null) {
			boolQueryBuilder = boolQueryBuilder.must(QueryBuilders.matchQuery("end", end));
		}
		Query query = new NativeSearchQueryBuilder()
				.withQuery(boolQueryBuilder)
				.build();
		SearchHits<LineItem> searchHits = elasticsearchOperations.search(query, LineItem.class);
		return searchHits.get().map(SearchHit::getContent).collect(Collectors.toList());
	}

	public List<LineItem> findAll() {
		List<LineItem> result = new ArrayList<>();
		Iterable<LineItem> foundLineItems = lineItemRepository.findAll();
		foundLineItems.forEach(result::add);
		return result;
	}

	public void delete(final String id) {
		if (!lineItemRepository.existsById(id)) {
			throw new ResourceNotFoundException("No line item found for id " + id);
		}
		lineItemRepository.deleteById(id);
	}

	public void deleteAll() {
		lineItemRepository.deleteAll();
	}

	public LineItem merge(MergeRequest mergeRequest) throws BusinessServiceException {
		final String subjectId = mergeRequest.getSubjectId();
		final String branchPath = mergeRequest.getBranchPath();
		// look for open line items to merge
		List<LineItem> lineItems = getOpenLineItems(subjectId, branchPath);
		// create new line item
		final String content = lineItems.stream().map(LineItem::getContent).collect(Collectors.joining("\n"));
		LineItem mergedLineItem = create(new LineItem(subjectId, content, branchPath));
		// close merged line items
		final LocalDate end = mergedLineItem.getStart();
		lineItems.forEach(lineItem -> {
			lineItem.setEnd(end);
			lineItemRepository.save(lineItem);
		});
		return mergedLineItem;
	}

	public List<LineItem> getOpenLineItems(final String subjectId, final String promotedBranchPath) throws BusinessServiceException {
		QueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery("subjectId", subjectId))
				.must(QueryBuilders.matchQuery("promotedBranchPath", promotedBranchPath))
				.mustNot(QueryBuilders.existsQuery("end"));
		Query query = new NativeSearchQueryBuilder()
				.withQuery(boolQueryBuilder)
				.build();
		SearchHits<LineItem> searchHits = elasticsearchOperations.search(query, LineItem.class);
		if (searchHits.isEmpty()) {
			throw new BusinessServiceException("No open line items found for subjectId '" + subjectId + "' and promotedBranchPath '" + promotedBranchPath +"'");
		}
		return searchHits.get().map(SearchHit::getContent).collect(Collectors.toList());
	}

}
