package org.snomed.release.note.core.data.service;

import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.AbstractTest;
import org.snomed.release.note.core.data.domain.Subject;

import static org.junit.jupiter.api.Assertions.*;

public class SubjectServiceTest extends AbstractTest {

	private static final Logger logger = LoggerFactory.getLogger(SubjectServiceTest.class);

	@BeforeEach
	void setUp() {

	}

	@Test
	void testCreate() throws BusinessServiceException {
		Subject subject = subjectService.create(new Subject("Clinical Finding", "MAIN"));
		assertNotNull(subject.getId());
		subject = subjectService.find(subject.getId());
		assertEquals("Clinical Finding", subject.getTitle());
		assertEquals("MAIN", subject.getPath());
	}

	@Test
	void testUpdate() throws BusinessServiceException {
		Subject created = subjectService.create(new Subject("Clinical Finding", "MAIN"));
		Subject updated = subjectService.update(created.getId(), new Subject("Procedure", "MAIN/SNOMEDCT-US"));
		assertEquals(created.getId(), updated.getId());
		assertEquals("Procedure", updated.getTitle());
		assertEquals("MAIN/SNOMEDCT-US", updated.getPath());
	}

	@Test
	void testDelete() throws BusinessServiceException {
		Subject subject = subjectService.create(new Subject("Clinical Finding", "MAIN"));
		subjectService.delete(subject.getId());
		assertThrows(ResourceNotFoundException.class, () -> {
			subjectService.find(subject.getId());
		});
	}

}
