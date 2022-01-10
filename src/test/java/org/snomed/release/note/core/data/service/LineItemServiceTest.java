package org.snomed.release.note.core.data.service;

import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.AbstractTest;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.rest.request.MergeRequest;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.rest.request.PromoteRequest;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LineItemServiceTest extends AbstractTest {

	private static final Logger logger = LoggerFactory.getLogger(LineItemServiceTest.class);

	private Subject subject;

	@BeforeEach
	void setUp() throws BusinessServiceException {
		subject = subjectService.create(new Subject("Body structure", "MAIN"));
	}

	@Test
	void testCreate() throws BusinessServiceException{
		LineItem lineItem = lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", "MAIN/ProjectA"));
		assertNotNull(lineItem.getId());
		lineItem = lineItemService.find(lineItem.getId());
		assertEquals(subject.getId(), lineItem.getSubjectId());
		assertEquals("Demonstration Release of the Anatomy Model", lineItem.getContent());
		assertEquals("MAIN/ProjectA", lineItem.getSourceBranchPath());
		assertFalse(lineItem.getReleased());
		assertNull(lineItem.getPromotedBranchPath());
	}

	@Test
	void testUpdate() throws BusinessServiceException {
		LineItem lineItem = lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", "MAIN/ProjectA"));
		lineItem.setContent("Final Release of the Anatomy Model");
		lineItem.setSourceBranchPath("MAIN/ProjectA/Task1");
		LineItem updated = lineItemService.update(lineItem);
		assertEquals(lineItem.getId(), updated.getId());
		// do not change sourceBranchPath
		assertEquals("MAIN/ProjectA", updated.getSourceBranchPath());
		assertEquals("Final Release of the Anatomy Model", updated.getContent());
	}

	@Test
	void testPromote() throws BusinessServiceException {
		LineItem lineItem = lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", "MAIN/ProjectA"));
		assertNull(lineItem.getPromotedBranchPath());
		LineItem promoted = lineItemService.promote(lineItem.getId(), new PromoteRequest("MAIN"));
		assertEquals(lineItem.getId(), promoted.getId());
		assertEquals("MAIN/ProjectA", promoted.getSourceBranchPath());
		assertEquals("MAIN", promoted.getPromotedBranchPath());
	}

	@Test
	void testDelete() throws BusinessServiceException {
		LineItem lineItem = lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", "MAIN/ProjectA"));
		lineItemService.delete(lineItem.getId());
		assertThrows(ResourceNotFoundException.class, () -> {
			lineItemService.find(lineItem.getId());
		});
	}

	@Test
	void testFindAll() throws BusinessServiceException {
		lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", "MAIN/ProjectA"));
		lineItemService.create(new LineItem(subject.getId(), "Limbs/Girdles", "MAIN/ProjectA"));
		lineItemService.create(new LineItem(subject.getId(), "Flexor annular pulley", "MAIN/ProjectB"));
		lineItemService.create(new LineItem(subject.getId(), "Muscle tendon of toes", "MAIN/ProjectB"));
		lineItemService.create(new LineItem(subjectService.create(new Subject(subject.getTitle(), "MAIN")).getId(), "Muscle tendon of toes", "MAIN/ProjectB"));

		List<LineItem> found = lineItemService.findAll();
		assertEquals(5, found.size());
	}

	@Test
	void testFind() throws BusinessServiceException {
		lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", "MAIN/ProjectA"));
		lineItemService.create(new LineItem(subject.getId(), "Limbs/Girdles", "MAIN/ProjectA"));
		lineItemService.create(new LineItem(subject.getId(), "Flexor annular pulley", "MAIN/ProjectB"));
		lineItemService.create(new LineItem(subject.getId(), "Muscle tendon of toes", "MAIN/ProjectB"));
		lineItemService.create(new LineItem(subjectService.create(new Subject(subject.getTitle(), "MAIN")).getId(), "Muscle tendon of toes", "MAIN/ProjectB"));

		List<LineItem> found = lineItemService.find(null, null, null, null, null, null, null);
		assertEquals(5, found.size());

		found = lineItemService.find(null, subject.getId(), null, null, null, null, null);
		assertEquals(4, found.size());

		found = lineItemService.find(subject.getTitle(), null, null, null, null, null, null);
		assertEquals(5, found.size());

		found = lineItemService.find(null, null, "MAIN/ProjectA", null, null, null, null);
		assertEquals(2, found.size());

		LineItem lineItem = found.get(0);
		PromoteRequest promoteRequest = new PromoteRequest("MAIN");
		lineItemService.promote(lineItem.getId(), promoteRequest);

		found = lineItemService.find(null, null, "MAIN/ProjectA", "MAIN", null, null, null);
		assertEquals(1, found.size());
		assertEquals(lineItem, found.get(0));

		found = lineItemService.find(null, null, null, null, "Muscle", null, null);
		assertEquals(2, found.size());

		found = lineItemService.find(null, null, null, null, null, LocalDate.now(), null);
		assertEquals(5, found.size());

		found = lineItemService.find(null, null, null, null, null, null, LocalDate.now());
		assertEquals(0, found.size());
	}

	@Test
	void testMerge() throws BusinessServiceException {
		LineItem lineItem1 = lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", "MAIN/ProjectA"));
		LineItem lineItem2 = lineItemService.create(new LineItem(subject.getId(), "Limbs/Girdles", "MAIN/ProjectA"));
		LineItem lineItem3 = lineItemService.create(new LineItem(subject.getId(), "Flexor annular pulley", "MAIN/ProjectB"));
		LineItem lineItem4 = lineItemService.create(new LineItem(subject.getId(), "Muscle tendon of toes", "MAIN/ProjectB"));

		PromoteRequest promoteRequest = new PromoteRequest("MAIN");
		lineItemService.promote(lineItem1.getId(), promoteRequest);
		lineItemService.promote(lineItem2.getId(), promoteRequest);
		lineItemService.promote(lineItem3.getId(), promoteRequest);

		MergeRequest mergeRequest = new MergeRequest(subject.getId(), "MAIN");
		LineItem merged = lineItemService.merge(mergeRequest);

		lineItem1 = lineItemService.find(lineItem1.getId());
		lineItem2 = lineItemService.find(lineItem2.getId());
		lineItem3 = lineItemService.find(lineItem3.getId());
		lineItem4 = lineItemService.find(lineItem4.getId());

		assertEquals(merged.getStart(), lineItem1.getEnd());
		assertEquals(merged.getStart(), lineItem2.getEnd());
		assertEquals(merged.getStart(), lineItem3.getEnd());

		assertNull(lineItem4.getEnd());

		String mergedContent = lineItem1.getContent() + "\n" + lineItem2.getContent() + "\n" + lineItem3.getContent();
		assertEquals(mergedContent, merged.getContent());

		assertThrows(BusinessServiceException.class, () -> {
			lineItemService.merge(mergeRequest);
		});
	}
}
