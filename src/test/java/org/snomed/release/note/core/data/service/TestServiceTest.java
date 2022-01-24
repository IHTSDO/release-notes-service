package org.snomed.release.note.core.data.service;

import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.junit.jupiter.api.Test;
import org.snomed.release.note.AbstractTest;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.domain.Subject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TestServiceTest extends AbstractTest {

	@Autowired
	private TestService testService;

	@Test
	void testCreateData() throws BusinessServiceException {
		testService.createData("MAIN/Test");
		List<Subject> subjects = subjectService.findAll();
		List<LineItem> lineItems = lineItemService.findAll();
		assertEquals(16, subjects.size());
		assertEquals(16, lineItems.size());
		assertEquals(3, lineItemService.findOrderedLineItems("MAIN/Test").size());
	}

	@Test
	void testDeleteData() throws BusinessServiceException {
		testService.createData("MAIN");
		testService.deleteData("MAIN");
		List<Subject> subjects = subjectService.findAll();
		List<LineItem> lineItems = lineItemService.findAll();
		assertEquals(0, subjects.size());
		assertEquals(0, lineItems.size());
	}
}
