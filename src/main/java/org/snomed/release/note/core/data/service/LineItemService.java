package org.snomed.release.note.core.data.service;

import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.repository.LineItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
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

	public String create(LineItem lineItem) {
		lineItem.setStartDate(LocalDate.now());
		lineItem.setReleased(false);

		IndexQuery indexQuery = new IndexQueryBuilder()
				.withId(lineItem.getId().toString())
				.withObject(lineItem)
				.build();

		return elasticsearchOperations.index(indexQuery, elasticsearchOperations.getIndexCoordinatesFor(LineItem.class));
	}

	public LineItem find(String id) {
		return elasticsearchOperations.get(id, LineItem.class);
	}

	public List<LineItem> findAll() {
		List<LineItem> result = new ArrayList<>();

		Iterable<LineItem> foundLineItems = lineItemRepository.findAll();
		foundLineItems.forEach(result::add);

		return result;
	}

}
