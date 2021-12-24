package org.snomed.release.note.core.data.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.AbstractTest;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.domain.Subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class LineItemServiceTest extends AbstractTest {

	private static final Logger logger = LoggerFactory.getLogger(LineItemServiceTest.class);

	@BeforeEach
	void setUp() {
		subjectService.create(new Subject("Clinical Finding", "MAIN"));
	}

	@Test
	void testCreate() {
		Subject subject = subjectService.findByTitle("Clinical Finding").get(0);
		LineItem createdLineItem = lineItemService.create(new LineItem(subject.getId(), "Added concepts", "MAIN"));
		assertNotNull(createdLineItem.getId());
		logger.info(subject.toString());
		logger.info(createdLineItem.toString());
		LineItem foundLineItem = lineItemService.find(createdLineItem.getId());
		assertEquals(subject.getId(), foundLineItem.getSubjectId());
		assertEquals("Added concepts", foundLineItem.getContent());
		assertEquals("MAIN", foundLineItem.getSourceBranch());
		assertEquals(false, foundLineItem.getReleased());
		assertEquals(createdLineItem, foundLineItem);
	}
}
