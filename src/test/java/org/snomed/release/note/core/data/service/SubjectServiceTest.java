package org.snomed.release.note.core.data.service;

import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.snomed.release.note.AbstractTest;
import org.snomed.release.note.core.data.domain.Subject;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SubjectServiceTest extends AbstractTest {

	@Test
	void testCreate() throws BusinessServiceException {
		final String path = "MAIN";
		Subject subject = subjectService.create(new Subject("Clinical Finding", path), path);
		assertNotNull(subject.getId());
		assertEquals("Clinical Finding", subject.getTitle());
		assertEquals(path, subject.getPath());
	}

	@Test
	void testUpdate() throws BusinessServiceException {
		final String path = "MAIN";
		Subject subject = subjectService.create(new Subject("Clinical Finding", path), path);
		subject.setTitle("Procedure");
		Subject updated = subjectService.update(subject, path);
		assertEquals(subject.getId(), updated.getId());
		assertEquals("Procedure", updated.getTitle());
		assertEquals(path, updated.getPath());
	}

	@Test
	void testDelete() throws BusinessServiceException {
		final String path = "MAIN";
		Subject subject = subjectService.create(new Subject("Clinical Finding", path), path);
		subjectService.delete(subject.getId(), path);
		assertThrows(ResourceNotFoundException.class, () -> {
			subjectService.find(subject.getId(), path);
		});
	}

	@Test
	void testFind() throws BusinessServiceException {
		Subject subject = subjectService.create(new Subject("Clinical Finding", "MAIN"), "MAIN");
		subjectService.create(new Subject("COVID-19", "MAIN"), "MAIN");
		subjectService.create(new Subject("Procedure", "MAIN/SNOMEDCT-US"), "MAIN/SNOMEDCT-US");
		subjectService.create(new Subject("COVID-19", "MAIN/SNOMEDCT-US"), "MAIN/SNOMEDCT-US");

		Subject found = subjectService.find(subject.getId(), subject.getPath());
		assertNotNull(found);
		assertEquals(subject.getId(), found.getId());
		assertEquals(subject.getPath(), found.getPath());
		assertEquals(subject.getTitle(), found.getTitle());

		List<Subject> foundList = subjectService.findAll();
		assertEquals(4, foundList.size());

		foundList = subjectService.find("MAIN");
		assertEquals(2, foundList.size());

		foundList = subjectService.find("MAIN/SNOMEDCT-US");
		assertEquals(2, foundList.size());
	}

}
