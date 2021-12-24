package org.snomed.release.note.core.data.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.AbstractTest;
import org.snomed.release.note.core.data.domain.Subject;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SubjectServiceTest extends AbstractTest {

	private static final Logger logger = LoggerFactory.getLogger(SubjectServiceTest.class);

	@BeforeEach
	void setUp() {

	}

	@Test
	void testCreate() {
		Subject subject = subjectService.create(new Subject("Clinical Finding", "MAIN"));
		assertNotNull(subject.getId());
		logger.info(subject.toString());
		subject = subjectService.find(subject.getId());
		assertEquals("Clinical Finding", subject.getTitle());
		assertEquals("MAIN", subject.getPath());
	}
}
