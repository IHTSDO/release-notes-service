package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.repository.LineItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
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
public class LineItemService {

	@Autowired
	private LineItemRepository lineItemRepository;

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	public LineItem create(LineItem lineItem) throws BusinessServiceException {
		if (Strings.isNullOrEmpty(lineItem.getSubjectId())) {
			throw new BadRequestException("subjectId is required");
		}
		if (Strings.isNullOrEmpty(lineItem.getSourceBranch())) {
			throw new BadRequestException("sourceBranch is required");
		}
		lineItem.setStartDate(LocalDate.now());
		return lineItemRepository.save(lineItem);
	}

	public LineItem update(final String id, final LineItem lineItemDetails) {
		LineItem lineItem = find(id);
		lineItem.setSubjectId(lineItemDetails.getSubjectId());
		lineItem.setParentId(lineItemDetails.getParentId());
		lineItem.setLevel(lineItemDetails.getLevel());
		lineItem.setContent(lineItemDetails.getContent());
		lineItem.setSequence(lineItemDetails.getSequence());
		lineItem.setSourceBranch(lineItemDetails.getSourceBranch());
		lineItem.setPromotedBranch(lineItemDetails.getPromotedBranch());
		lineItem.setStartDate(lineItemDetails.getStartDate());
		lineItem.setEndDate(lineItemDetails.getEndDate());
		lineItem.setReleased(lineItemDetails.getReleased());
		return lineItemRepository.save(lineItem);
	}

	public LineItem promote(final String id, final String promotedBranch) throws BusinessServiceException {
		if (Strings.isNullOrEmpty(promotedBranch)) {
			throw new BadRequestException("promotedBranch is required");
		}
		LineItem lineItem = find(id);
		lineItem.setPromotedBranch(promotedBranch);
		return lineItemRepository.save(lineItem);
	}

	public LineItem find(final String id) {
		return lineItemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No line item found for id " + id));
	}

	public List<LineItem> find(final String sourceBranch, final String promotedBranch, final LocalDate startDate, final LocalDate endDate) {
		if (sourceBranch == null && promotedBranch == null && startDate == null && endDate == null) {
			return findAll();
		}
		Criteria criteria = new Criteria();
		if (sourceBranch != null) {
			criteria = criteria.and("sourceBranch").is(sourceBranch);
		}
		if (promotedBranch != null) {
			criteria = criteria.and("promotedBranch").is(promotedBranch);
		}
		if (startDate != null) {
			criteria = criteria.and("startDate").is(startDate);
		}
		if (endDate != null) {
			criteria = criteria.and("endDate").is(endDate);
		}
		Query query = new CriteriaQuery(criteria);
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

	public void delete(final String id) {
		if (!lineItemRepository.existsById(id)) {
			throw new ResourceNotFoundException("No line item found for id " + id);
		}
		lineItemRepository.deleteById(id);
	}

	public void deleteAll() {
		lineItemRepository.deleteAll();
	}

	public LineItem merge(final String subjectId, final String sourceBranch) throws BusinessServiceException {
		// look for open line items to merge
		List<LineItem> lineItems = getOpenLineItems(subjectId, sourceBranch);
		// create new line item
		final String content = lineItems.stream().map(LineItem::getContent).collect(Collectors.joining("\n"));
		LineItem mergedLineItem = create(new LineItem(subjectId, content, sourceBranch));
		// close merged line items
		final LocalDate endDate = mergedLineItem.getStartDate();
		lineItems.forEach(lineItem -> {
			lineItem.setEndDate(endDate);
			lineItemRepository.save(lineItem);
		});
		return mergedLineItem;
	}

	public List<LineItem> getOpenLineItems(final String subjectId, final String promotedBranch) throws BusinessServiceException {
		QueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.matchQuery("subjectId", subjectId))
				.must(QueryBuilders.matchQuery("promotedBranch", promotedBranch))
				.mustNot(QueryBuilders.existsQuery("endDate"));
		Query query = new NativeSearchQueryBuilder()
				.withQuery(boolQueryBuilder)
				.build();
		SearchHits<LineItem> searchHits = elasticsearchOperations.search(query, LineItem.class);
		if (searchHits.isEmpty()) {
			throw new BusinessServiceException("No open line items found for subjectId " + subjectId + " and promotedBranch " + promotedBranch);
		}
		return searchHits.get().map(SearchHit::getContent).collect(Collectors.toList());
	}
}
