package org.snomed.release.note.core.data.service;

import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.snomed.release.note.AbstractTest;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.domain.Subject;
import org.snomed.release.note.core.data.repository.LineItemRepository;
import org.snomed.release.note.core.util.BranchUtil;
import org.snomed.release.note.rest.pojo.LineItemCreateRequest;
import org.snomed.release.note.rest.pojo.LineItemUpdateRequest;
import org.snomed.release.note.rest.pojo.VersionRequest;
import org.snomed.release.note.rest.request.SubjectCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class LineItemServiceTest extends AbstractTest {

	@Autowired
	private TestDataHelper testDataHelper;

	@Autowired
	private LineItemRepository lineItemRepository;

	@Test
	void testCreate() throws BusinessServiceException{
		final String path = "MAIN/ProjectA";
		Subject subject = subjectService.create(new SubjectCreateRequest("Body structure"), "MAIN");
		LineItem lineItem = lineItemService.create(new LineItemCreateRequest(subject.getId(), "Demonstration Release of the Anatomy Model"), path);
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
		Subject subject = subjectService.create(new SubjectCreateRequest("Body structure"), "MAIN");
		LineItem lineItem = lineItemService.create(new LineItemCreateRequest(subject.getId(), "Demonstration Release of the Anatomy Model"), path);
		lineItem.setContent("Final Release of the Anatomy Model");
		lineItem.setSequence(3);
		LineItem updated = lineItemService.update(new LineItemUpdateRequest(lineItem.getId(), lineItem.getParentId(), lineItem.getLevel(), lineItem.getSequence(), lineItem.getContent()), path);
		assertEquals(lineItem.getId(), updated.getId());
		assertEquals("Final Release of the Anatomy Model", updated.getContent());
		assertEquals(3, updated.getSequence());
		assertEquals(path, updated.getSourceBranch());
	}

	@Test
	void testDelete() throws BusinessServiceException {
		final String path = "MAIN/ProjectA";
		Subject subject = subjectService.create(new SubjectCreateRequest("Body structure"), "MAIN");
		LineItem lineItem = lineItemService.create(new LineItemCreateRequest(subject.getId()), path);
		lineItemService.delete(lineItem.getId(), path);
		assertThrows(ResourceNotFoundException.class, () -> lineItemService.find(lineItem.getId(), path));
	}

	@Test
	void testFind() throws BusinessServiceException {
		Subject subject = subjectService.create(new SubjectCreateRequest("Body structure"), "MAIN");
		Subject anotherSubject = subjectService.create(new SubjectCreateRequest(subject.getTitle()), "MAIN");

		LineItem lineItem = lineItemService.create(new LineItemCreateRequest(subject.getId(),"Demonstration Release of the Anatomy Model"), "MAIN/ProjectA");
		lineItemService.create(new LineItemCreateRequest(anotherSubject.getId(), "Limbs/Girdles"), "MAIN/ProjectA");
		lineItemService.create(new LineItemCreateRequest(subject.getId(), "Flexor annular pulley"), "MAIN/ProjectB");
		lineItemService.create(new LineItemCreateRequest(anotherSubject.getId(), "Muscle tendon of toes"), "MAIN/ProjectB");
		lineItemService.create(new LineItemCreateRequest(anotherSubject.getId(), "Muscle tendon of toes"), "MAIN");

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
		assertEquals(2, foundList.size());

		lineItemService.promote(lineItem.getId(), lineItem.getSourceBranch());
		foundList = lineItemService.find("MAIN");
		assertEquals(2, foundList.size());
	}

	@Test
	void testFindOrderedLineItems() {
		testDataHelper.createLineItems("MAIN");
		List<LineItem> lineItems = lineItemService.findOrderedLineItems("MAIN");
		assertNotNull(lineItems);
		assertEquals(3, lineItems.size());
		lineItems.forEach(item -> {
			assertEquals(1, item.getLevel());
			assertTrue(TestDataHelper.LEVEL_ONE_TITLES.contains(item.getTitle()));
			assertFalse(item.getChildren().isEmpty());
			item.getChildren().forEach(child -> {
				assertEquals(item.getId(), child.getParentId());
				assertTrue(TestDataHelper.LEVEL_TWO_TITLES.contains(child.getTitle()));
			});
		});
	}

	@Test
	void testPromote_oneLineItem() throws BusinessServiceException {
		Subject subject = subjectService.create(new SubjectCreateRequest("Body structure"), "MAIN");

		String sourceBranch = "MAIN/ProjectA/Task1";
		LineItem lineItem = lineItemService.create(new LineItemCreateRequest(subject.getId(), "Release of the Anatomy Model"), sourceBranch);
		lineItemService.promote(lineItem.getId(), sourceBranch);

		lineItem = lineItemService.find(lineItem.getId(), sourceBranch);
		assertNotNull(lineItem.getPromotedBranch());
		assertEquals("MAIN/ProjectA", lineItem.getPromotedBranch());
		assertNotNull(lineItem.getEnd());

		List<LineItem> promotedLineItems = lineItemService.find("MAIN/ProjectA");
		assertEquals(1, promotedLineItems.size());
		assertEquals("MAIN/ProjectA", promotedLineItems.get(0).getSourceBranch());
		assertEquals(lineItem.getContent(), promotedLineItems.get(0).getContent());

		sourceBranch = "MAIN/ProjectA/Task2";
		LineItem lineItem2 = lineItemService.create(new LineItemCreateRequest(subject.getId(), "COVID-19"), sourceBranch);
		lineItemService.promote(lineItem2.getId(), sourceBranch);

		promotedLineItems = lineItemService.find("MAIN/ProjectA");
		assertEquals(1, promotedLineItems.size());
		assertEquals("MAIN/ProjectA", promotedLineItems.get(0).getSourceBranch());
		assertEquals(lineItem.getContent() + System.lineSeparator() + lineItem2.getContent(), promotedLineItems.get(0).getContent());
	}

	@Test
	void testPromote_manyLineItems() throws BusinessServiceException {
		Subject subject1 = subjectService.create(new SubjectCreateRequest("Body structure"), "MAIN");
		Subject subject2 = subjectService.create(new SubjectCreateRequest("Clinical Finding"), "MAIN");

		final String sourceBranchA = "MAIN/ProjectA";
		lineItemService.create(new LineItemCreateRequest(subject1.getId(), "Project A: Limbs/Girdles"), sourceBranchA);
		lineItemService.create(new LineItemCreateRequest(subject2.getId(), "Project A: COVID-19"), sourceBranchA);
		lineItemService.promote(sourceBranchA);

		List<LineItem> lineItems = lineItemRepository.findBySourceBranch(sourceBranchA);
		assertEquals(2, lineItems.size());
		lineItems.forEach(lineItem -> {
			assertNotNull(lineItem.getPromotedBranch());
			assertEquals(BranchUtil.getParentBranch(sourceBranchA), lineItem.getPromotedBranch());
			assertNotNull(lineItem.getEnd());
		});

		final String sourceBranchB = "MAIN/ProjectB";
		lineItemService.create(new LineItemCreateRequest(subject1.getId(), "Project B: Limbs/Girdles"), sourceBranchB);
		lineItemService.promote(sourceBranchB);

		lineItems.addAll(lineItemRepository.findBySourceBranch(sourceBranchB));
		assertEquals(3, lineItems.size());

		List<LineItem> promotedLineItems = lineItemService.find("MAIN");
		assertEquals(2, promotedLineItems.size());
		promotedLineItems.forEach(lineItem -> {
			assertEquals("MAIN", lineItem.getSourceBranch());
			assertEquals(getMergedContent(lineItems, lineItem.getSubjectId()), lineItem.getContent());
		});
	}

	@Test
	void testVersion() throws BusinessServiceException {
		Subject subject = subjectService.create(new SubjectCreateRequest("Body structure"), "MAIN");

		String sourceBranch = "MAIN";
		LineItem lineItem = lineItemService.create(new LineItemCreateRequest(subject.getId(), "Demonstration Release of the Anatomy Model"), sourceBranch);
		VersionRequest versionRequest = new VersionRequest("MAIN/2022-01-31");
		lineItemService.version(lineItem.getSourceBranch(), versionRequest);

		lineItem = lineItemRepository.findById(lineItem.getId()).get();
		assertNotNull(lineItem.getPromotedBranch());
		assertEquals(versionRequest.getReleaseBranch(), lineItem.getSourceBranch());
		assertEquals(versionRequest.getReleaseBranch(), lineItem.getPromotedBranch());
	}

	@Test
	void testPublish() throws BusinessServiceException {
		Subject subject = subjectService.create(new SubjectCreateRequest("Body structure"), "MAIN");

		String sourceBranch = "MAIN";
		LineItem lineItem = lineItemService.create(new LineItemCreateRequest(subject.getId(), "Demonstration Release of the Anatomy Model"), sourceBranch);
		assertThrows(BusinessServiceException.class, () -> lineItemService.publish(lineItem.getSourceBranch()));

		VersionRequest versionRequest = new VersionRequest("MAIN/2022-01-31");
		lineItemService.version(lineItem.getSourceBranch(), versionRequest);
		LineItem versioned = lineItemRepository.findById(lineItem.getId()).get();
		assertFalse(versioned.getReleased());

		lineItemService.publish(versioned.getSourceBranch());
		LineItem published = lineItemRepository.findById(lineItem.getId()).get();
		assertTrue(published.getReleased());
	}

	@Test
	void testFindPublishedLineItems() throws BusinessServiceException {
		testDataHelper.createLineItems("MAIN");
		lineItemService.version("MAIN", new VersionRequest("MAIN/2022-01-31"));
		lineItemService.publish("MAIN/2022-01-31");

		List<LineItem> lineItemsUnordered = lineItemService.findPublished("MAIN/2022-01-31", false);
		assertNotNull(lineItemsUnordered);
		assertEquals(10, lineItemsUnordered.size());
		lineItemsUnordered.forEach(item -> assertTrue(item.getChildren().isEmpty()));

		List<LineItem> lineItems = lineItemService.findPublished("MAIN/2022-01-31", true);
		assertNotNull(lineItems);
		assertEquals(3, lineItems.size());
		lineItems.forEach(item -> {
			assertEquals(1, item.getLevel());
			assertTrue(TestDataHelper.LEVEL_ONE_TITLES.contains(item.getTitle()));
			assertFalse(item.getChildren().isEmpty());
			item.getChildren().forEach(child -> {
				assertEquals(item.getId(), child.getParentId());
				assertTrue(TestDataHelper.LEVEL_TWO_TITLES.contains(child.getTitle()));
			});
		});
	}

	private String getMergedContent(List<LineItem> lineItems, final String subjectId) {
		return lineItems.stream()
				.filter(lineItem -> subjectId.equals(lineItem.getSubjectId()))
				.map(LineItem::getContent)
				.collect(Collectors.joining(System.lineSeparator()));
	}

}
