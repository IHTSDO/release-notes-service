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

	public LineItem create(LineItem lineItem) {
		lineItem.setStartDate(LocalDate.now());
		lineItem.setReleased(false);
		return lineItemRepository.save(lineItem);
	}

	public LineItem update(LineItem lineItem) throws BusinessServiceException {
		String lineItemId = lineItem.getId();
		if (Strings.isNullOrEmpty(lineItemId)) {
			throw new BadRequestException("Line item id is required");
		}
		if (!lineItemRepository.existsById(lineItemId)) {
			throw new ResourceNotFoundException("No line item found for id " + lineItemId);
		}
		return lineItemRepository.save(lineItem);
	}

	public LineItem find(String id) {
		return lineItemRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("No line item found for id " + id));
	}

	public List<LineItem> findBySubjectId(String subjectId) {
		return lineItemRepository.findBySubjectId(subjectId);
	}

	public List<LineItem> findAll() {
		List<LineItem> result = new ArrayList<>();
		Iterable<LineItem> foundLineItems = lineItemRepository.findAll();
		foundLineItems.forEach(result::add);
		return result;
	}

	public void delete(String id) {
		if (!lineItemRepository.existsById(id)) {
			throw new ResourceNotFoundException("No line item found for id " + id);
		}
		lineItemRepository.deleteById(id);
	}

	public void deleteAll() {
		lineItemRepository.deleteAll();
	}

}
