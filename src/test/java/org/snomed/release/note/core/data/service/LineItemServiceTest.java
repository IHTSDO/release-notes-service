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

import static org.junit.jupiter.api.Assertions.*;

public class LineItemServiceTest extends AbstractTest {

	private static final Logger logger = LoggerFactory.getLogger(LineItemServiceTest.class);

	private Subject subject;

	@BeforeEach
	void setUp() throws BusinessServiceException {
		subject = subjectService.create(new Subject("Clinical Finding", "MAIN"));
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
		// Do not update source branch
		assertEquals("MAIN/ProjectA", updated.getSourceBranch());
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
}
