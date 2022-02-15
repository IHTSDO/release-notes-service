package org.snomed.release.note.core.data.service;

import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.repository.LineItemRepository;
import org.snomed.release.note.core.data.repository.SubjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TestDataHelper {

	public static List<String> LEVEL_ONE_TITLES = Arrays.asList("Introduction", "Content Development Activity", "Technical notes");
	public static List<String> LEVEL_TWO_TITLES = Arrays.asList("Background", "Scope", "Body structure", "Clinical Finding", "Procedure", "Known Issues", "Resolved Issues");

	@Autowired
	private SubjectRepository subjectRepository;

	@Autowired
	private LineItemRepository lineItemRepository;

	public List<Subject> createSubjects(String path, List<String> titles) {
		List<Subject> subjects = new ArrayList<>();
		titles.stream().forEach(title -> subjects.add(new Subject(title, path)));
		return (List<Subject>) subjectRepository.saveAll(subjects);
	}

	public List<LineItem> createLineItems(String path) {
		// TODO Create subjects on the code system path only
		List<Subject> topLevelSubjects = createSubjects(path, LEVEL_ONE_TITLES);

		List<Subject> subLevelSubjects = createSubjects(path, LEVEL_TWO_TITLES);
		// Create top level items
		List<LineItem> topItems = new ArrayList<>();
		int topSequence = 1;
		for (Subject subject : topLevelSubjects) {
			topItems.add(lineItemRepository.save(constructLineItem(subject, path, 1, topSequence++)));
		}
		// Create sub level items
		List<LineItem> subItems = new ArrayList<>();
		int subSequence = 1;
		for (Subject subject : subLevelSubjects) {
			subItems.add(constructLineItem(subject, path, 2, subSequence++));
		}

		// Add parents for sub line items
		subItems.subList(0, 2).stream().forEach(lineItem -> lineItem.setParentId(topItems.get(0).getId()));
		subItems.subList(2, 5).stream().forEach(lineItem -> lineItem.setParentId(topItems.get(1).getId()));
		subItems.subList(5, 7).stream().forEach(lineItem -> lineItem.setParentId(topItems.get(2).getId()));

		List<LineItem> results = new ArrayList<>();
		results.addAll(topItems);
		lineItemRepository.saveAll(subItems).forEach(lineItem -> results.add(lineItem));
		return results;
	}

	public LineItem constructLineItem(Subject subject, String path, int level, int sequence) {
		return constructLineItem(subject, path, level, sequence, "");
	}

	public LineItem constructLineItem(Subject subject, String path, int level, int sequence, String content) {
		LineItem lineItem = new LineItem(subject, path);
		lineItem.setSequence(sequence);
		lineItem.setLevel(level);
		lineItem.setContent(content);
		return lineItem;
	}

}
