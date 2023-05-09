package org.snomed.release.note.core.data.service;

import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.repository.LineItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TestDataHelper {

	public static final List<String> LEVEL_ONE_TITLES = Arrays.asList("Introduction", "Content Development Activity", "Technical notes");
	public static final List<String> LEVEL_TWO_TITLES = Arrays.asList("Background", "Scope", "Body structure", "Clinical Finding", "Procedure", "Known Issues", "Resolved Issues");

	@Autowired
	private LineItemRepository lineItemRepository;

	public List<LineItem> createLineItems(String path) {
		// Create top level items
		List<LineItem> topItems = new ArrayList<>();
		int topSequence = 1;
		for (String title : LEVEL_ONE_TITLES) {
			topItems.add(lineItemRepository.save(constructLineItem(path, title, 1, topSequence++)));
		}
		// Create sub level items
		List<LineItem> subItems = new ArrayList<>();
		int subSequence = 1;
		for (String title : LEVEL_TWO_TITLES) {
			subItems.add(constructLineItem(path, title, 2, subSequence++));
		}

		// Add parents for sub line items
		subItems.subList(0, 2).stream().forEach(lineItem -> lineItem.setParentId(topItems.get(0).getId()));
		subItems.subList(2, 5).stream().forEach(lineItem -> lineItem.setParentId(topItems.get(1).getId()));
		subItems.subList(5, 7).stream().forEach(lineItem -> lineItem.setParentId(topItems.get(2).getId()));

		List<LineItem> results = new ArrayList<>(topItems);
		lineItemRepository.saveAll(subItems).forEach(results::add);
		return results;
	}

	public LineItem constructLineItem(String path, String title, int level, int sequence) {
		return constructLineItem(null, path, title, "", level, sequence);
	}

	public LineItem constructLineItem(String parentId, String path, String title, String content, int level, int sequence) {
		LineItem lineItem = new LineItem();
		lineItem.setParentId(parentId);
		lineItem.setSourceBranch(path);
		lineItem.setTitle(title);
		lineItem.setContent(content);
		lineItem.setLevel(level);
		lineItem.setSequence(sequence);
		lineItem.setStart(new Date());
		return lineItem;
	}

}
