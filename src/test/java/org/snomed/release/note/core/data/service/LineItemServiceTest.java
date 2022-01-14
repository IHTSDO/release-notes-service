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

	@BeforeEach
	void setUp() throws BusinessServiceException {

	}

	@Test
	void testCreate() throws BusinessServiceException{
		final String path = "MAIN/ProjectA";
		Subject subject = subjectService.create(new Subject("Body structure", "MAIN"), "MAIN");
		LineItem lineItem = lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", path), path);
		assertNotNull(lineItem.getId());
		lineItem = lineItemService.find(lineItem.getId(), path);
		assertEquals(subject.getId(), lineItem.getSubjectId());
		assertEquals("Demonstration Release of the Anatomy Model", lineItem.getContent());
		assertEquals(path, lineItem.getSourceBranch());
		assertFalse(lineItem.getReleased());
		assertNull(lineItem.getPromotedBranch());
	}

	@Test
	void testUpdate() throws BusinessServiceException {
		final String path = "MAIN/ProjectA";
		Subject subject = subjectService.create(new Subject("Body structure", "MAIN"), "MAIN");
		LineItem lineItem = lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", path), path);
		lineItem.setContent("Final Release of the Anatomy Model");
		lineItem.setSequence(3);
		LineItem updated = lineItemService.update(lineItem, path);
		assertEquals(lineItem.getId(), updated.getId());
		assertEquals("Final Release of the Anatomy Model", updated.getContent());
		assertEquals(3, updated.getSequence());
		assertEquals(path, updated.getSourceBranch());
	}

	/*@Test
	void testPromote() throws BusinessServiceException {
		LineItem lineItem = lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", "MAIN/ProjectA"));
		assertNull(lineItem.getPromotedBranch());
		LineItem promoted = lineItemService.promote(lineItem.getId(), new PromoteRequest("MAIN"));
		assertEquals(lineItem.getId(), promoted.getId());
		assertEquals("MAIN/ProjectA", promoted.getSourceBranch());
		assertEquals("MAIN", promoted.getPromotedBranch());
	}*/

	@Test
	void testDelete() throws BusinessServiceException {
		final String path = "MAIN/ProjectA";
		Subject subject = subjectService.create(new Subject("Body structure", "MAIN"), "MAIN");
		LineItem lineItem = lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", path), path);
		lineItemService.delete(lineItem.getId(), path);
		assertThrows(ResourceNotFoundException.class, () -> {
			lineItemService.find(lineItem.getId(), path);
		});
	}

	@Test
	void testFind() throws BusinessServiceException {
		Subject subject = subjectService.create(new Subject("Body structure", "MAIN"), "MAIN");
		Subject anotherSubject = subjectService.create(new Subject(subject.getTitle(), "MAIN"), "MAIN");

		LineItem lineItem = lineItemService.create(new LineItem(subject.getId(), "Demonstration Release of the Anatomy Model", "MAIN/ProjectA"), "MAIN/ProjectA");
		lineItemService.create(new LineItem(subject.getId(), "Limbs/Girdles", "MAIN/ProjectA"), "MAIN/ProjectA");
		lineItemService.create(new LineItem(subject.getId(), "Flexor annular pulley", "MAIN/ProjectB"), "MAIN/ProjectB");
		lineItemService.create(new LineItem(subject.getId(), "Muscle tendon of toes", "MAIN/ProjectB"), "MAIN/ProjectB");
		lineItemService.create(new LineItem(anotherSubject.getId(), "Muscle tendon of toes", "MAIN/ProjectB"), "MAIN/ProjectB");

		LineItem found = lineItemService.find(lineItem.getId(), lineItem.getSourceBranch());
		assertNotNull(found);
		assertEquals(lineItem.getId(), found.getId());
		assertEquals(lineItem.getSourceBranch(), found.getSourceBranch());
		assertEquals(lineItem.getContent(), found.getContent());

		List<LineItem> foundList = lineItemService.findAll();
		assertEquals(5, foundList.size());

		foundList = lineItemService.find("MAIN/ProjectA");
		assertEquals(2, foundList.size());

		foundList = lineItemService.find("MAIN/ProjectB");
		assertEquals(3, foundList.size());

		foundList = lineItemService.findBySubjectId(anotherSubject.getId());
		assertEquals(1, foundList.size());

		/*LineItem lineItem = foundList.get(0);
		PromoteRequest promoteRequest = new PromoteRequest("MAIN");
		lineItemService.promote(lineItem.getId(), promoteRequest);

		foundList = lineItemService.find(null, null, "MAIN/ProjectA", "MAIN", null, null, null);
		assertEquals(1, foundList.size());
		assertEquals(lineItem, foundList.get(0));*/
	}

	/*@Test
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
	}*/
}
