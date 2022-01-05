package org.snomed.release.note.core.data.service;

import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.AbstractTest;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.domain.Subject;

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
		assertEquals("MAIN/ProjectA", lineItem.getSourceBranch());
		assertFalse(lineItem.getReleased());
		assertNull(lineItem.getPromotedBranch());
	}

	@Test
	void testUpdate() throws BusinessServiceException {
		LineItem created = lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", "MAIN/ProjectA"));
		LineItem updated = lineItemService.update(created.getId(), new LineItem(subject.getId(), "Final Release of the Anatomy Model", "MAIN/ProjectA/Task1"));
		assertEquals(created.getId(), updated.getId());
		assertEquals("MAIN/ProjectA/Task1", updated.getSourceBranch());
		assertEquals("Final Release of the Anatomy Model", updated.getContent());
	}

	@Test
	void testPromote() throws BusinessServiceException {
		LineItem created = lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", "MAIN/ProjectA"));
		assertNull(created.getPromotedBranch());
		LineItem promoted = lineItemService.promote(created.getId(), "MAIN");
		assertEquals(created.getId(), promoted.getId());
		assertEquals("MAIN/ProjectA", promoted.getSourceBranch());
		assertEquals("MAIN", promoted.getPromotedBranch());
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
	void testFind() throws BusinessServiceException {
		lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", "MAIN/ProjectA"));
		lineItemService.create(new LineItem(subject.getId(), "Limbs/Girdles", "MAIN/ProjectA"));
		lineItemService.create(new LineItem(subject.getId(), "Flexor annular pulley", "MAIN/ProjectB"));
		lineItemService.create(new LineItem(subject.getId(), "Muscle tendon of toes", "MAIN/ProjectB"));

		List<LineItem> found = lineItemService.find(null, null, null, null);
		assertEquals(4, found.size());

		found = lineItemService.find("MAIN/ProjectA", null, null, null);
		assertEquals(2, found.size());

		LineItem lineItem = found.get(0);

		lineItemService.promote(lineItem.getId(), "MAIN");

		found = lineItemService.find("MAIN/ProjectA", "MAIN", null, null);
		assertEquals(1, found.size());
		assertEquals(lineItem, found.get(0));

		found = lineItemService.find(null, null, LocalDate.now(), null);
		assertEquals(4, found.size());

		found = lineItemService.find(null, null, null, LocalDate.now());
		assertEquals(0, found.size());
	}

	@Test
	void testMerge() throws BusinessServiceException {
		LineItem lineItem1 = lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", "MAIN/ProjectA"));
		LineItem lineItem2 = lineItemService.create(new LineItem(subject.getId(), "Limbs/Girdles", "MAIN/ProjectA"));
		LineItem lineItem3 = lineItemService.create(new LineItem(subject.getId(), "Flexor annular pulley", "MAIN/ProjectB"));
		LineItem lineItem4 = lineItemService.create(new LineItem(subject.getId(), "Muscle tendon of toes", "MAIN/ProjectB"));

		lineItemService.promote(lineItem1.getId(), "MAIN");
		lineItemService.promote(lineItem2.getId(), "MAIN");
		lineItemService.promote(lineItem3.getId(), "MAIN");

		LineItem merged = lineItemService.merge(subject.getId(), "MAIN");

		lineItem1 = lineItemService.find(lineItem1.getId());
		lineItem2 = lineItemService.find(lineItem2.getId());
		lineItem3 = lineItemService.find(lineItem3.getId());
		lineItem4 = lineItemService.find(lineItem4.getId());

		assertEquals(merged.getStartDate(), lineItem1.getEndDate());
		assertEquals(merged.getStartDate(), lineItem2.getEndDate());
		assertEquals(merged.getStartDate(), lineItem3.getEndDate());

		assertNull(lineItem4.getEndDate());

		String mergedContent = lineItem1.getContent() + "\n" + lineItem2.getContent() + "\n" + lineItem3.getContent();
		assertEquals(mergedContent, merged.getContent());

		assertThrows(BusinessServiceException.class, () -> {
			lineItemService.merge(subject.getId(), "MAIN");
		});
	}
}
