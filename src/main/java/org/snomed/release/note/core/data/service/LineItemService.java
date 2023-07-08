package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.ihtsdo.otf.rest.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.domain.LineItemComparator;
import org.snomed.release.note.core.data.repository.LineItemRepository;
import org.snomed.release.note.core.util.BranchUtil;
import org.snomed.release.note.core.util.ContentUtil;
import org.snomed.release.note.rest.pojo.LineItemCreateRequest;
import org.snomed.release.note.rest.pojo.LineItemUpdateRequest;
import org.snomed.release.note.rest.pojo.VersionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.clients.elasticsearch7.ElasticsearchAggregations;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.regexpQuery;

@Service
public class LineItemService {

	@Autowired
	private LineItemRepository lineItemRepository;

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	public static final String CONTENT_DEVELOPMENT_ACTIVITY = "Content Development Activity";

	public static final int AGGREGATION_SEARCH_SIZE = 200;

	public static final DateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

	private static final Logger LOGGER = LoggerFactory.getLogger(LineItemService.class);

	public LineItem create(final LineItemCreateRequest createRequest, final String path, final boolean skipGeneratingContent) throws BusinessServiceException {
		String title = createRequest.getTitle();

		if (Strings.isNullOrEmpty(title)) {
			throw new BadRequestException("'title' is required");
		}
		if (findOpenLineItem(createRequest.getParentId(), title, path) != null) {
			throw new EntityAlreadyExistsException("Line item with title '" + title + "' already exists on path '" + path +"'");
		}

		validateParentIdAndLevel(createRequest.getParentId(), createRequest.getLevel());

		LineItem lineItem = createFromRequest(createRequest, path);
		if (!skipGeneratingContent) {
			lineItem.generateContent();
		}

		return lineItemRepository.save(lineItem);
	}

	public LineItem update(final LineItemUpdateRequest updateRequest, final String path) throws BusinessServiceException {
		LineItem existing = find(updateRequest.getId(), path);

		if (!Strings.isNullOrEmpty(existing.getPromotedBranch())) {
			throw new BadConfigurationException("Line item with id '" + existing.getId() + "' is already promoted to branch '" + existing.getPromotedBranch() + "' and cannot be changed");
		}

		validateParentIdAndLevel(updateRequest.getParentId(), updateRequest.getLevel() == null ? existing.getLevel() : updateRequest.getLevel());

		existing.setParentId(updateRequest.getParentId());
		if (updateRequest.getLevel() != null) {
			existing.setLevel(updateRequest.getLevel());
		}
		if (updateRequest.getSequence() != null) {
			existing.setSequence(updateRequest.getSequence());
		}

		if (updateRequest.getContent() != null) {
			existing.setContent(updateRequest.getContent());
		} else {
			if (updateRequest.getChangeType() != null) {
				existing.setChangeType(updateRequest.getChangeType());
			}
			if (updateRequest.getAdditionalChangeTypes() != null) {
				existing.setAdditionalChangeTypes(updateRequest.getAdditionalChangeTypes());
			}
			if (updateRequest.getHierarchy() != null) {
				existing.setHierarchy(updateRequest.getHierarchy());
			}
			if (updateRequest.getChangedInAdditionalHierarchy() != null) {
				existing.setChangedInAdditionalHierarchy(updateRequest.getChangedInAdditionalHierarchy());
			}
			if (updateRequest.getNumberEditedConcepts() != null) {
				existing.setNumberEditedConcepts(updateRequest.getNumberEditedConcepts());
			}
			if (updateRequest.getFutureChangesPlanned() != null) {
				existing.setFutureChangesPlanned(updateRequest.getFutureChangesPlanned());
			}
			if (updateRequest.getLinkContentTracker() != null) {
				existing.setLinkContentTracker(updateRequest.getLinkContentTracker());
			}
			if (updateRequest.getConceptInactivations() != null) {
				existing.setConceptInactivations(updateRequest.getConceptInactivations());
			}
			if (updateRequest.getLinkBriefingNote() != null) {
				existing.setLinkBriefingNote(updateRequest.getLinkBriefingNote());
			}
			if (updateRequest.getLinkToTemplate() != null) {
				existing.setLinkToTemplate(updateRequest.getLinkToTemplate());
			}
			if (updateRequest.getDescriptionChanges() != null) {
				existing.setDescriptionChanges(updateRequest.getDescriptionChanges());
			}
			if (updateRequest.getNotes() != null) {
				existing.setNotes(updateRequest.getNotes());
			}
			existing.generateContent();
		}

		return lineItemRepository.save(existing);
	}

	public void promote(final String id, final String path) throws BusinessServiceException {
		if (BranchUtil.isCodeSystemBranch(path)) {
			throw new BadConfigurationException("Cannot promote line item with id '" + id + "' because it is already on a code system branch '" + path + "'");
		}

		LineItem lineItem = find(id, path);

		if (!Strings.isNullOrEmpty(lineItem.getPromotedBranch())) {
			throw new BadConfigurationException("Line item with id '" + id + "' is already promoted to branch '" + lineItem.getPromotedBranch() + "'");
		}

		List<LineItem> toSave = new ArrayList<>();
		doPromote(lineItem, getPromotedBranch(path), toSave);

		// batch update
		lineItemRepository.saveAll(toSave);
	}

	public void promote(final String path) throws BusinessServiceException {
		if (BranchUtil.isCodeSystemBranch(path)) {
			throw new BadConfigurationException("Cannot promote line items because they are already on a code system branch '" + path + "'");
		}

		List<LineItem> lineItems = find(path);

		final String branch = getPromotedBranch(path);
		final List<LineItem> toSave = new ArrayList<>();

		lineItems.stream()
				.filter(lineItem -> Strings.isNullOrEmpty(lineItem.getPromotedBranch()))
				.forEach(lineItem -> doPromote(lineItem, branch, toSave));

		// batch update
		lineItemRepository.saveAll(toSave);
	}

	public LineItem find(final String id, final String path) {
		LineItem lineItem = lineItemRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No line item found for id '" + id + "'"));

		String sourceBranch = lineItem.getSourceBranch();

		if (!path.equals(sourceBranch)) {
			throw new ResourceNotFoundException("No line item found for id '" + id + "' and source branch '" + path + "'");
		}

		return lineItem;
	}

	public List<LineItem> findByTitle(final String title, final String path) {
		Query query = new NativeSearchQueryBuilder().withQuery(boolQuery()
						.must(QueryBuilders.termQuery("title", title))
						.must(QueryBuilders.termQuery("sourceBranch", path))
						.mustNot(QueryBuilders.existsQuery("end")))
				.build();

		SearchHits<LineItem> searchHits = elasticsearchOperations.search(query, LineItem.class);

		List<LineItem> lineItems = searchHits.get().map(SearchHit::getContent).collect(toList());
		LOGGER.info("{} line items with title {} found on path {}", lineItems.size(), title, path);

		return lineItems;
	}

	public List<LineItem> find(final String path) {
		Query query = new NativeSearchQueryBuilder().withQuery(boolQuery()
						.must(QueryBuilders.termQuery("sourceBranch", path))
						.mustNot(QueryBuilders.existsQuery("end")))
				.build();

		SearchHits<LineItem> searchHits = elasticsearchOperations.search(query, LineItem.class);

		List<LineItem> lineItems = searchHits.get().map(SearchHit::getContent).collect(toList());
		LOGGER.info("{} line items found on path {}", lineItems.size(), path);

		return lineItems;
	}

	public List<LineItem> findOrderedLineItems(final String path) {
		List<LineItem> lineItems = find(path);
		return doOrder(lineItems);
	}

	public List<LineItem> findPublished(final String path, final boolean ordered) {
		Query query = new NativeSearchQueryBuilder().withQuery(boolQuery()
						.mustNot(QueryBuilders.existsQuery("end"))
						.must(QueryBuilders.termQuery("released", true))
						.should(QueryBuilders.termQuery("sourceBranch", path))
						.should(QueryBuilders.termQuery("promotedBranch", path)))
				.build();

		SearchHits<LineItem> searchHits = elasticsearchOperations.search(query, LineItem.class);

		List<LineItem> lineItems = searchHits.get().map(SearchHit::getContent).collect(toList());
		LOGGER.info("{} line items found on path {}", lineItems.size(), path);

		return ordered ? doOrder(lineItems) : lineItems;
	}

	public List<LineItem> findUnpublished(final String path, final boolean ordered) {
		Query query = new NativeSearchQueryBuilder().withQuery(boolQuery()
						.mustNot(QueryBuilders.existsQuery("end"))
						.must(QueryBuilders.termQuery("released", false))
						.must(QueryBuilders.termQuery("sourceBranch", path)))
				.build();

		SearchHits<LineItem> searchHits = elasticsearchOperations.search(query, LineItem.class);

		List<LineItem> lineItems = searchHits.get().map(SearchHit::getContent).collect(toList());
		LOGGER.info("{} line items found on path {}", lineItems.size(), path);

		return ordered ? doOrder(lineItems) : lineItems;
	}

	public List<String> findCategories(final String path) {
		List<String> categories = new ArrayList<>();

		LineItem contentDevelopmentActivity = getContentDevelopmentActivity(path);

		if (contentDevelopmentActivity != null) {
			getChildren(contentDevelopmentActivity.getId(), path).forEach(child -> categories.add(child.getTitle()));
		}
		return categories;
	}

	public List<String> getVersions(final String path) throws BusinessServiceException {
		if (!BranchUtil.isCodeSystemBranch(path)) {
			throw new BadRequestException("Path '" + path + "' must be a code system branch");
		}

		List<String> versions = new ArrayList<>();

		final String regexp = path + BranchUtil.SEPARATOR + "[0-9]{4}-[0-9]{2}-[0-9]{2}";
		final String aggregationName = "versions";

		Query aggregateQuery = new NativeSearchQueryBuilder()
				.withQuery(regexpQuery("promotedBranch", regexp))
				.withAggregations(AggregationBuilders
						.terms(aggregationName)
						.field("promotedBranch")
						.size(AGGREGATION_SEARCH_SIZE))
				.withMaxResults(0)
				.build();

		SearchHits<LineItem> searchHits = elasticsearchOperations.search(aggregateQuery, LineItem.class);

		ElasticsearchAggregations elasticsearchAggregations = (ElasticsearchAggregations) searchHits.getAggregations();

		if (elasticsearchAggregations != null) {
			ParsedStringTerms terms = elasticsearchAggregations.aggregations().get(aggregationName);
			terms.getBuckets().forEach(bucket -> versions.add(bucket.getKeyAsString()));
		}

		return versions;
	}

	public LineItem getContentDevelopmentActivity(String path) {
		List<LineItem> contentDevelopmentActivity = findByTitle(CONTENT_DEVELOPMENT_ACTIVITY, path);

		if (contentDevelopmentActivity.isEmpty()) {
			LOGGER.warn("Line item with title {} is not found on path {}", CONTENT_DEVELOPMENT_ACTIVITY, path);
			return null;
		}
		if (contentDevelopmentActivity.size() > 1) {
			LOGGER.warn("There are multiple line items with title {} found on path {}", CONTENT_DEVELOPMENT_ACTIVITY, path);
			return null;
		}
		return contentDevelopmentActivity.get(0);
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

	public void version(final String path, final VersionRequest versionRequest) throws BusinessServiceException {
		final Date effectiveTime = versionRequest.getEffectiveTime();
		final String releaseBranch = path + BranchUtil.SEPARATOR + DATE_FORMATTER.format(effectiveTime);

		if (effectiveTime == null) {
			throw new BadRequestException("'effectiveTime' is required");
		}
		if (!BranchUtil.isCodeSystemBranch(path)) {
			throw new BadRequestException("Branch '" + path + "' must be a code system branch");
		}
		if (!find(releaseBranch).isEmpty()) {
			throw new BadConfigurationException("Line items already exist on branch '" + releaseBranch + "'");
		}

		List<LineItem> lineItems = findOrderedLineItems(path);

		List<LineItem> lineItemsToSave = new ArrayList<>();

		lineItems.forEach(lineItem -> {
			LineItemCreateRequest createParentRequest = new LineItemCreateRequest(
					lineItem.getParentId(),
					lineItem.getTitle(),
					lineItem.getContent(),
					lineItem.getLevel(),
					lineItem.getSequence());

			LineItem clonedParent = lineItemRepository.save(createFromRequest(createParentRequest, releaseBranch, releaseBranch));

			boolean shouldClearContent = CONTENT_DEVELOPMENT_ACTIVITY.equals(lineItem.getTitle());

			for (LineItem child : lineItem.getChildren()) {
				LineItemCreateRequest createChildRequest = new LineItemCreateRequest(
						clonedParent.getId(),
						child.getTitle(),
						child.getContent(),
						child.getLevel(),
						child.getSequence());
				LineItem clonedChild = createFromRequest(createChildRequest, releaseBranch, releaseBranch);

				lineItemsToSave.add(clonedChild);

				if (shouldClearContent) {
					child.setContent(null);
					lineItemsToSave.add(child);
				}
			}
		});

		lineItemRepository.saveAll(lineItemsToSave);
	}

	public void publish(final String path) throws BusinessServiceException {
		if (!BranchUtil.isReleaseBranch(path)) {
			throw new BadRequestException("Branch '" + path + "' must be a release branch");
		}

		List<LineItem> lineItems = find(path);

		lineItems.forEach(lineItem -> lineItem.setReleased(true));

		lineItemRepository.saveAll(lineItems);
	}

	public List<LineItem> getChildren(String parentId, String path) {
		BoolQueryBuilder queryBuilder = boolQuery()
				.must(QueryBuilders.termQuery("sourceBranch", path))
				.mustNot(QueryBuilders.existsQuery("end"));

		if (parentId == null) {
			queryBuilder.mustNot(QueryBuilders.existsQuery("parentId"));
		} else {
			queryBuilder.must(QueryBuilders.termQuery("parentId", parentId));
		}

		Query query = new NativeSearchQueryBuilder().withQuery(queryBuilder).build();
		SearchHits<LineItem> searchHits = elasticsearchOperations.search(query, LineItem.class);

		return searchHits.get().map(SearchHit::getContent).collect(toList());
	}

	public void updateSequence(String path) {
		updateSequence(findOrderedLineItems(path));
	}

	private void updateSequence(List<LineItem> lineItems) {
		int sequence = 0;
		for (LineItem lineItem : lineItems) {
			lineItem.setSequence(++sequence);
			updateSequence(lineItem.getChildren());
		}
		lineItemRepository.saveAll(lineItems);
	}

	private String getPromotedBranch(final String sourceBranch) throws BusinessServiceException {
		String promotedBranch = BranchUtil.getParentBranch(sourceBranch);

		if (promotedBranch == null) {
			throw new BadConfigurationException("Line item on source branch '" + sourceBranch + "' cannot be promoted");
		}

		return promotedBranch;
	}

	private void doPromote(LineItem lineItem, String path, List<LineItem> toSave) {
		// Find or create an open line item on the given branch to merge the content to
		LineItem openLineItem = findOpenLineItem(lineItem.getParentId(), lineItem.getTitle(), path);

		if (openLineItem == null) {
			openLineItem = createFromRequest(new LineItemCreateRequest(
					lineItem.getParentId(),
					lineItem.getTitle(),
					lineItem.getContent()), path);
		} else {
			openLineItem.setContent(ContentUtil.merge(openLineItem.getContent(), lineItem.getContent()));
		}
		toSave.add(openLineItem);

		// Set promotedBranch and end
		lineItem.setPromotedBranch(path);
		lineItem.setEnd(new Date());
		toSave.add(lineItem);
	}

	private List<LineItem> doOrder(List<LineItem> lineItems) {
		LineItemComparator lineItemComparator = new LineItemComparator();

		List<LineItem> topLevelItems = lineItems.stream()
				.filter(lineItem -> lineItem.getLevel() == 1)
				.sorted(lineItemComparator)
				.collect(toList());

		Map<String, List<LineItem>> subItemsMappedByParent = new HashMap<>();
		List<LineItem> subItemsWithoutParent = new ArrayList<>();

		lineItems.stream().filter(lineItem -> lineItem.getLevel() == 2).forEach(lineItem -> {
			if (lineItem.getParentId() == null) {
				subItemsWithoutParent.add(lineItem);
			} else {
				subItemsMappedByParent.computeIfAbsent(lineItem.getParentId(), items -> new ArrayList<>()).add(lineItem);
			}
		});
		subItemsMappedByParent.values().forEach(items -> items.sort(lineItemComparator));

		topLevelItems.forEach(topLevelItem -> {
			String parentId = topLevelItem.getId();
			if (subItemsMappedByParent.containsKey(parentId)) {
				topLevelItem.setChildren(subItemsMappedByParent.get(parentId));
				subItemsMappedByParent.remove(parentId);
			}
		});

		subItemsMappedByParent.forEach((parentId, subItems) -> {
			LOGGER.warn("{} sub line items with parent missing, parent id: {}", subItems.size(), parentId);
			// Return these sub items without parent as well
			topLevelItems.addAll(subItems);
		});

		if (!subItemsWithoutParent.isEmpty()) {
			LOGGER.warn("{} sub line items without parent id", subItemsWithoutParent.size());
			// Return these sub items without parent as well
			topLevelItems.addAll(subItemsWithoutParent);
		}

		return topLevelItems;
	}

	private LineItem findOpenLineItem(final String parentId, final String title, final String sourceBranch) {
		BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
				.must(QueryBuilders.termQuery("title", title))
				.must(QueryBuilders.termQuery("sourceBranch", sourceBranch))
				.mustNot(QueryBuilders.existsQuery("end"));

		if (parentId == null) {
			boolQueryBuilder.mustNot(QueryBuilders.existsQuery("parentId"));
		} else {
			boolQueryBuilder.must(QueryBuilders.termQuery("parentId", parentId));
		}

		Query query = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder).build();

		// Should always be just one open line item per subject, per branch
		SearchHit<LineItem> searchHit = elasticsearchOperations.searchOne(query, LineItem.class);

		return searchHit == null ? null : searchHit.getContent();
	}

	private void validateParentIdAndLevel(String parentId, Integer level) throws BusinessServiceException {
		if (parentId != null) {
			if (!exists(parentId)) {
				throw new BadRequestException("Parent line item with id '" + parentId + "' does not exist");
			}
			if (level != null && level != 2) {
				throw new BadRequestException("'level' must be equal to '2' when 'parentId' is not null");
			}
		} else if (level != null && level != 1) {
			throw new BadRequestException("'level' must be equal to '1' when 'parentId' is null");
		}
	}

	private LineItem createFromRequest(LineItemCreateRequest lineItemCreateRequest, String path) {
		return createFromRequest(lineItemCreateRequest, path, null);
	}

	private LineItem createFromRequest(LineItemCreateRequest lineItemCreateRequest, String sourceBranch, String promotedBranch) {
		LineItem lineItem = new LineItem();

		lineItem.setParentId(lineItemCreateRequest.getParentId());
		lineItem.setTitle(lineItemCreateRequest.getTitle());
		lineItem.setContent(lineItemCreateRequest.getContent());
		lineItem.setChangeType(lineItemCreateRequest.getChangeType());
		lineItem.setAdditionalChangeTypes(lineItemCreateRequest.getAdditionalChangeTypes());
		lineItem.setHierarchy(lineItemCreateRequest.getHierarchy());
		lineItem.setChangedInAdditionalHierarchy(lineItemCreateRequest.getChangedInAdditionalHierarchy());
		lineItem.setNumberEditedConcepts(lineItemCreateRequest.getNumberEditedConcepts());
		lineItem.setFutureChangesPlanned(lineItemCreateRequest.getFutureChangesPlanned());
		lineItem.setLinkContentTracker(lineItemCreateRequest.getLinkContentTracker());
		lineItem.setConceptInactivations(lineItemCreateRequest.getConceptInactivations());
		lineItem.setLinkBriefingNote(lineItemCreateRequest.getLinkBriefingNote());
		lineItem.setLinkToTemplate(lineItemCreateRequest.getLinkToTemplate());
		lineItem.setDescriptionChanges(lineItemCreateRequest.getDescriptionChanges());
		lineItem.setNotes(lineItemCreateRequest.getNotes());
		if (lineItemCreateRequest.getLevel() == null) {
			lineItem.setLevel(lineItemCreateRequest.getParentId() == null ? 1 : 2);
		} else {
			lineItem.setLevel(lineItemCreateRequest.getLevel());
		}

		if (lineItemCreateRequest.getSequence() == null) {
			lineItem.setSequence(getMaxSequence(lineItemCreateRequest.getParentId(), sourceBranch) + 1);
		} else {
			lineItem.setSequence(lineItemCreateRequest.getSequence());
		}

		lineItem.setStart(new Date());
		lineItem.setSourceBranch(sourceBranch);
		lineItem.setPromotedBranch(promotedBranch);

		return lineItem;
	}

	private int getMaxSequence(String parentId, String path) {
		List<LineItem> lineItems = getChildren(parentId, path);
		Optional<Integer> sequence = lineItems.stream().map(LineItem::getSequence).max(Comparator.naturalOrder());
		return sequence.orElse(0);
	}

}
