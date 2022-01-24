package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.elasticsearch.index.query.QueryBuilders;
import org.ihtsdo.otf.rest.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.repository.LineItemRepository;
import org.snomed.release.note.core.util.BranchUtil;
import org.snomed.release.note.rest.request.VersionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static org.elasticsearch.index.query.QueryBuilders.boolQuery;

@Service
public class LineItemService {

	@Autowired
	private SubjectService subjectService;

	@Autowired
	private LineItemRepository lineItemRepository;

	@Autowired
	private ElasticsearchOperations elasticsearchOperations;

	private static final Logger LOGGER = LoggerFactory.getLogger(LineItemService.class);

	public LineItem create(LineItem lineItem, final String path) throws BusinessServiceException {
		if (Strings.isNullOrEmpty(lineItem.getSubjectId())) {
			throw new BadRequestException("'subjectId' is required");
		}
		if (!subjectService.exists(lineItem.getSubjectId())) {
			throw new ResourceNotFoundException("No subject found for id '" + lineItem.getSubjectId() + "'");
		}
		if (lineItem.getId() != null && exists(lineItem.getId())) {
			throw new EntityAlreadyExistsException("Line item with id '" + lineItem.getId() + "' already exists");
		}
		if (findOpenLineItem(lineItem.getSubjectId(), path) != null) {
			throw new EntityAlreadyExistsException("Line item with subjectId '" + lineItem.getSubjectId() + "' already exists on path '" + path +"'");
		}
		lineItem.setSourceBranch(path);
		lineItem.setStart(LocalDate.now());
		return lineItemRepository.save(lineItem);
	}

	public LineItem update(final LineItem lineItem, final String path) throws BusinessServiceException {
		LineItem existing = find(lineItem.getId(), path);
		if (!Strings.isNullOrEmpty(existing.getPromotedBranch())) {
			throw new BadConfigurationException("Line item with id '" + existing.getId() + "' is already promoted to branch '" + existing.getPromotedBranch() + "' and cannot be changed");
		}
		existing.setParentId(lineItem.getParentId());
		existing.setLevel(lineItem.getLevel());
		existing.setSequence(lineItem.getSequence());
		existing.setContent(lineItem.getContent());
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
		final LineItem lineItem = lineItemRepository.findById(id).orElseThrow(() ->
				new ResourceNotFoundException("No line item found for id '" + id + "'"));
		String sourceBranch = lineItem.getSourceBranch();
		if (!path.equals(sourceBranch)) {
			throw new ResourceNotFoundException("No line item found for id '" + id + "' and source branch '" + path + "'");
		}
		subjectService.joinSubject(lineItem);
		return lineItem;
	}

	public List<LineItem> find(final String path) {
		Query query = new NativeSearchQueryBuilder().withQuery(boolQuery()
						.mustNot(QueryBuilders.existsQuery("end"))
						.must(QueryBuilders.termQuery("sourceBranch", path)))
				.build();
		SearchHits<LineItem> searchHits = elasticsearchOperations.search(query, LineItem.class);

		List<LineItem> lineItems = searchHits.get().map(SearchHit::getContent).collect(toList());
		LOGGER.info("{} line items found on path {}", lineItems.size(), path);

		subjectService.joinSubjects(lineItems);
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

		subjectService.joinSubjects(lineItems);

		if (ordered) {
			return doOrder(lineItems);
		} else {
			return lineItems;
		}
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

	public void version(final String path, final VersionRequest versionRequest) throws BusinessServiceException {
		final String releaseBranch = versionRequest.getReleaseBranch();

		if (Strings.isNullOrEmpty(releaseBranch)) {
			throw new BadRequestException("'releaseBranch' is required");
		}
		if (!BranchUtil.isReleaseBranch(releaseBranch)) {
			throw new BadRequestException("releaseBranch '" + releaseBranch + "' is not a release branch");
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

	private String getPromotedBranch(final String sourceBranch) throws BusinessServiceException {
		String promotedBranch = BranchUtil.getParentBranch(sourceBranch);
		if (promotedBranch == null) {
			throw new BadConfigurationException("Line item on source branch '" + sourceBranch + "' cannot be promoted");
		}
		return promotedBranch;
	}

	private void doPromote(LineItem lineItem, String branch, List<LineItem> toSave) {
		// Find or create a promoted line item to merge content to
		LineItem promotedLineItem = findOpenLineItem(lineItem.getSubjectId(), branch);
		if (promotedLineItem == null) {
			promotedLineItem = new LineItem(lineItem.getSubjectId(), branch, lineItem.getParentId(), lineItem.getLevel(), lineItem.getSequence(), lineItem.getContent());
		} else {
			promotedLineItem.setContent(promotedLineItem.getContent() + '\n' + lineItem.getContent());
		}
		toSave.add(promotedLineItem);

		// Set promotedBranch and end
		lineItem.setPromotedBranch(branch);
		lineItem.setEnd(LocalDate.now());
		toSave.add(lineItem);
	}

	private List<LineItem> doOrder(List<LineItem> lineItems) {
		List<LineItem> topLevelItems = lineItems.stream().filter(lineItem -> lineItem.getLevel() == 1).collect(toList());
		topLevelItems.sort(Comparator.comparing(LineItem::getSequence));

		Map<String, List<LineItem>> lineItemsMappedByParent = new HashMap<>();
		List<LineItem> subItemsWithoutParent = new ArrayList<>();
		lineItems.stream().filter(lineItem -> lineItem.getLevel() == 2).forEach(item -> {
			if (item.getParentId() == null) {
				subItemsWithoutParent.add(item);
			} else {
				lineItemsMappedByParent.computeIfAbsent(item.getParentId(), items -> new ArrayList<>()).add(item);
			}
		});
		lineItemsMappedByParent.values().forEach(items -> items.sort(Comparator.comparing(LineItem::getSequence)));

		topLevelItems.forEach(topItem -> {
			if (lineItemsMappedByParent.containsKey(topItem.getId())) {
				topItem.setChildren(lineItemsMappedByParent.get(topItem.getId()));
			}
		});
		if (!subItemsWithoutParent.isEmpty()) {
			LOGGER.warn("{} sub line items without parent id", subItemsWithoutParent.size());
			// Return these sub items without parent as well
			topLevelItems.addAll(subItemsWithoutParent);
		}
		return topLevelItems;
	}

	private LineItem findOpenLineItem(final String subjectId, final String sourceBranch) {
		Query query = new NativeSearchQueryBuilder().withQuery(boolQuery()
						.must(QueryBuilders.termQuery("subjectId", subjectId))
						.must(QueryBuilders.termQuery("sourceBranch", sourceBranch))
						.mustNot(QueryBuilders.existsQuery("end")))
				.build();
		// Should always be just one open line item per subject, per branch
		SearchHit<LineItem> searchHit = elasticsearchOperations.searchOne(query, LineItem.class);
		return searchHit == null ? null : searchHit.getContent();
	}

}
