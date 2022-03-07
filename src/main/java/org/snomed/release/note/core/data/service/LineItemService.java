package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.ihtsdo.otf.rest.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.repository.LineItemRepository;
import org.snomed.release.note.core.util.BranchUtil;
import org.snomed.release.note.rest.pojo.CloneRequest;
import org.snomed.release.note.rest.pojo.LineItemCreateRequest;
import org.snomed.release.note.rest.pojo.LineItemUpdateRequest;
import org.snomed.release.note.rest.pojo.VersionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

@Service
public class LineItemService {

	@Autowired
	private LineItemRepository lineItemRepository;

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	private static final Logger LOGGER = LoggerFactory.getLogger(LineItemService.class);

	public LineItem create(final LineItemCreateRequest lineItemCreateRequest, final String path) throws BusinessServiceException {
		String title = lineItemCreateRequest.getTitle();

		if (Strings.isNullOrEmpty(title)) {
			throw new BadRequestException("'title' is required");
		}
		if (findOpenLineItem(lineItemCreateRequest.getParentId(), title, path) != null) {
			throw new EntityAlreadyExistsException("Line item with title '" + title + "' already exists on path '" + path +"'");
		}

		validateParentIdAndLevel(lineItemCreateRequest.getParentId(), lineItemCreateRequest.getLevel());

		LineItem lineItem = new LineItem();
		lineItem.setSourceBranch(path);
		lineItem.setParentId(lineItemCreateRequest.getParentId());
		lineItem.setTitle(title);
		lineItem.setContent(lineItemCreateRequest.getContent());

		if (lineItemCreateRequest.getLevel() == null) {
			lineItem.setLevel(lineItemCreateRequest.getParentId() == null ? 1 : 2);
		} else {
			lineItem.setLevel(lineItemCreateRequest.getLevel());
		}
		lineItem.setSequence(lineItemCreateRequest.getSequence() == null ? 1 : lineItemCreateRequest.getSequence());
		lineItem.setStart(new Date());

		return lineItemRepository.save(lineItem);
	}

	public LineItem update(final LineItemUpdateRequest lineItemUpdateRequest, final String path) throws BusinessServiceException {
		LineItem existing = find(lineItemUpdateRequest.getId(), path);

		if (!Strings.isNullOrEmpty(existing.getPromotedBranch())) {
			throw new BadConfigurationException("Line item with id '" + existing.getId() + "' is already promoted to branch '" + existing.getPromotedBranch() + "' and cannot be changed");
		}

		validateParentIdAndLevel(lineItemUpdateRequest.getParentId(), lineItemUpdateRequest.getLevel() == null ? existing.getLevel() : lineItemUpdateRequest.getLevel());

		existing.setParentId(lineItemUpdateRequest.getParentId());

		if (lineItemUpdateRequest.getContent() != null) {
			existing.setContent(lineItemUpdateRequest.getContent());
		}
		if (lineItemUpdateRequest.getLevel() != null) {
			existing.setLevel(lineItemUpdateRequest.getLevel());
		}
		if (lineItemUpdateRequest.getSequence() != null) {
			existing.setSequence(lineItemUpdateRequest.getSequence());
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
		final String releaseBranch = versionRequest.getReleaseBranch();

		if (Strings.isNullOrEmpty(releaseBranch)) {
			throw new BadRequestException("'releaseBranch' is required");
		}
		if (!BranchUtil.isReleaseBranch(releaseBranch)) {
			throw new BadRequestException("Branch '" + releaseBranch + "' is not a release branch");
		}
		if (!BranchUtil.isCodeSystemBranch(path)) {
			throw new BadRequestException("Branch '" + path + "' must be a code system branch");
		}
		if (!BranchUtil.extractCodeSystem(path).equals(BranchUtil.extractCodeSystem(releaseBranch))) {
			throw new BadRequestException("Branch '" + path + "' and release branch '" + releaseBranch + "' must be from the same code system");
		}

		List<LineItem> lineItems = find(path);

		lineItems.forEach(lineItem -> {
			lineItem.setSourceBranch(releaseBranch);
			lineItem.setPromotedBranch(releaseBranch);
		});

		lineItemRepository.saveAll(lineItems);
	}

	public void publish(final String path) throws BusinessServiceException {
		if (!BranchUtil.isReleaseBranch(path)) {
			throw new BadRequestException("Branch '" + path + "' must be a release branch");
		}

		List<LineItem> lineItems = find(path);

		lineItems.forEach(lineItem -> lineItem.setReleased(true));

		lineItemRepository.saveAll(lineItems);
	}

	public void clone(final String path, final CloneRequest cloneRequest) throws BusinessServiceException {
		if (!BranchUtil.isReleaseBranch(path)) {
			throw new BadRequestException("Branch '" + path + "' must be a release branch");
		}

		List<LineItem> lineItems = find(path);

		List<LineItem> clonedLineItems = new ArrayList<>();
		String destinationBranch = cloneRequest.getDestinationBranch();

		lineItems.forEach(lineItem -> {
			try {
				LineItem clonedLineItem = create(new LineItemCreateRequest(
						lineItem.getParentId(),
						lineItem.getTitle(),
						null,
						lineItem.getLevel(),
						lineItem.getSequence()), destinationBranch);
				clonedLineItems.add(clonedLineItem);
			} catch (BusinessServiceException e) {
				throw new BusinessServiceRuntimeException("An error occurred while cloning line items", e);
			}
		});

		lineItemRepository.saveAll(clonedLineItems);
	}

	public List<LineItem> getChildren(String id, String path) {
		Query query = new NativeSearchQueryBuilder().withQuery(boolQuery()
						.must(QueryBuilders.termQuery("parentId", id))
						.must(QueryBuilders.termQuery("sourceBranch", path))
						.mustNot(QueryBuilders.existsQuery("end")))
				.build();

		SearchHits<LineItem> searchHits = elasticsearchOperations.search(query, LineItem.class);

		return searchHits.get().map(SearchHit::getContent).collect(toList());
	}

	private String getPromotedBranch(final String sourceBranch) throws BusinessServiceException {
		String promotedBranch = BranchUtil.getParentBranch(sourceBranch);

		if (promotedBranch == null) {
			throw new BadConfigurationException("Line item on source branch '" + sourceBranch + "' cannot be promoted");
		}

		return promotedBranch;
	}

	private void doPromote(LineItem lineItem, String branch, List<LineItem> toSave) {
		// Find or create an open line item on the given branch to merge content to
		LineItem openLineItem = findOpenLineItem(lineItem.getParentId(), lineItem.getTitle(), branch);
		if (openLineItem == null) {
			openLineItem = createLineItem(lineItem, branch);
		} else {
			openLineItem.setContent(String.join(System.lineSeparator(), getContentNotNull(openLineItem), getContentNotNull(lineItem)));
		}
		toSave.add(openLineItem);

		// Set promotedBranch and end
		lineItem.setPromotedBranch(branch);
		lineItem.setEnd(new Date());
		toSave.add(lineItem);
	}

	private List<LineItem> doOrder(List<LineItem> lineItems) {
		List<LineItem> topLevelItems = lineItems.stream()
				.filter(lineItem -> lineItem.getLevel() == 1)
				.sorted(Comparator.comparing(LineItem::getSequence))
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
		subItemsMappedByParent.values().forEach(items -> items.sort(Comparator.comparing(LineItem::getSequence)));

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

	private LineItem createLineItem(LineItem lineItem, String path) {
		LineItem newLineItem = new LineItem();
		newLineItem.setSourceBranch(path);
		newLineItem.setParentId(lineItem.getParentId());
		newLineItem.setTitle(lineItem.getTitle());
		newLineItem.setContent(getContentNotNull(lineItem));
		newLineItem.setLevel(lineItem.getLevel());
		newLineItem.setSequence(lineItem.getSequence());
		return newLineItem;
	}

	private String getContentNotNull(LineItem lineItem) {
		return lineItem.getContent() == null ? "" : lineItem.getContent();
	}

}
