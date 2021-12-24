package org.snomed.release.note.core.data.service;

import org.elasticsearch.common.Strings;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.repository.LineItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
		lineItem.setReleased(false);

		return lineItemRepository.save(lineItem);
	}

	public LineItem update(final String id, final LineItem lineItemDetails) {
		LineItem lineItem = find(id);

		if (lineItemDetails.getContent() != null) {
			lineItem.setContent(lineItemDetails.getContent());
		}
		if (lineItemDetails.getSequence() != null) {
			lineItem.setSequence(lineItemDetails.getSequence());
		}

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

}
