package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.ihtsdo.otf.rest.exception.*;
import org.snomed.release.note.core.data.domain.LineItem;
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

	public LineItem create(LineItem lineItem, final String path) throws BusinessServiceException {
		if (Strings.isNullOrEmpty(lineItem.getSubjectId())) {
			throw new BadRequestException("'subjectId' is required");
		}
		if (lineItem.getId() != null && exists(lineItem.getId())) {
			throw new EntityAlreadyExistsException("Line item with id '" + lineItem.getId() + "' already exists");
		}
		lineItem.setSourceBranch(path);
		lineItem.setStart(LocalDate.now());
		return lineItemRepository.save(lineItem);
	}

	public LineItem update(final LineItem lineItem, final String path) {
		LineItem existing = find(lineItem.getId(), path);
		existing.setLevel(lineItem.getLevel());
		existing.setContent(lineItem.getContent());
		existing.setSequence(lineItem.getSequence());
		existing.setEnd(lineItem.getEnd());
		existing.setReleased(lineItem.getReleased());
		return lineItemRepository.save(existing);
	}

	public LineItem promote(final String id, final String path, final PromoteRequest promoteRequest) throws BusinessServiceException {
		if (Strings.isNullOrEmpty(promoteRequest.getPromotedBranch())) {
			throw new BadRequestException("'promotedBranch' is required");
		}
		LineItem lineItem = find(id, path);
		if (!Strings.isNullOrEmpty(lineItem.getPromotedBranch())) {
			throw new ProcessingException("Line item with id '" + id + "' is already promoted to branch '" + lineItem.getPromotedBranch() + "'");
		}
		lineItem.setPromotedBranch(promoteRequest.getPromotedBranch());
		return lineItemRepository.save(lineItem);
	}

	public List<LineItem> promote(final String path, final PromoteRequest promoteRequest) throws BusinessServiceException {
		if (Strings.isNullOrEmpty(promoteRequest.getPromotedBranch())) {
			throw new BadRequestException("'promotedBranch' is required");
		}
		List<LineItem> lineItems = find(path);
		lineItems.forEach(lineItem -> lineItem.setPromotedBranch(promoteRequest.getPromotedBranch()));
		lineItemRepository.saveAll(lineItems);
		return lineItems;
	}
/*	public LineItem release(final String id) throws BusinessServiceException {
		LineItem lineItem = find(id);
		lineItem.setReleased(true);
		return lineItemRepository.save(lineItem);
	}*/

	public LineItem find(final String id, final String path) {
		final LineItem lineItem = lineItemRepository.findById(id).orElseThrow(() ->
				new ResourceNotFoundException("No line item found for id '" + id + "'"));
		String sourceBranch = lineItem.getSourceBranch();
		if (!path.equals(sourceBranch)) {
			throw new ResourceNotFoundException("No line item found for id '" + id + "' and source branch '" + path + "'");
		}
		return lineItem;
	}

	public List<LineItem> find(final String path) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
				.should(QueryBuilders.termQuery("sourceBranch", path))
				.should(QueryBuilders.termQuery("promotedBranch", path))
				.mustNot(QueryBuilders.existsQuery("end"));
		Query query = new NativeSearchQueryBuilder()
				.withQuery(boolQueryBuilder)
				.build();
		SearchHits<LineItem> searchHits = elasticsearchOperations.search(query, LineItem.class);
		return searchHits.get().map(SearchHit::getContent).collect(Collectors.toList());
	}

	public List<LineItem> findBySubjectId(final String subjectId) {
		return lineItemRepository.findBySubjectId(subjectId);
	}

	public List<LineItem> findAll() {
		List<LineItem> result = new ArrayList<>();
		Iterable<LineItem> foundLineItems = lineItemRepository.findAll();
		foundLineItems.forEach(result::add);
		return result;
	}

	public void delete(final String id, final String path) {
		LineItem lineItem = find(id, path);
		lineItemRepository.delete(lineItem);
	}

	public void deleteAll() {
		lineItemRepository.deleteAll();
	}

	public boolean exists(final String id) {
		return lineItemRepository.existsById(id);
	}

	/*public LineItem merge(MergeRequest mergeRequest) throws BusinessServiceException {
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
	}*/

}
