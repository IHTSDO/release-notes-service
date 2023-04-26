package org.snomed.release.note.core.data.service;

import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.snomed.release.note.AbstractTest;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.repository.LineItemRepository;
import org.snomed.release.note.core.util.BranchUtil;
import org.snomed.release.note.core.util.ContentUtil;
import org.snomed.release.note.rest.pojo.CloneRequest;
import org.snomed.release.note.rest.pojo.LineItemCreateRequest;
import org.snomed.release.note.rest.pojo.LineItemUpdateRequest;
import org.snomed.release.note.rest.pojo.VersionRequest;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class LineItemServiceTest extends AbstractTest {

	@Autowired
	private TestDataHelper testDataHelper;

	@Autowired
	private LineItemRepository lineItemRepository;

	private final DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

	@Test
	void testCreate() throws BusinessServiceException{
		final String path = "MAIN/ProjectA";

		LineItem lineItem = lineItemService.create(new LineItemCreateRequest("Body structure", "Demonstration Release of the Anatomy Model"), path);
		assertNotNull(lineItem.getId());
		lineItem = lineItemService.find(lineItem.getId(), path);
		assertEquals("Body structure", lineItem.getTitle());
		assertEquals("Demonstration Release of the Anatomy Model", lineItem.getContent());
		assertEquals(path, lineItem.getSourceBranch());
		assertEquals(1, lineItem.getSequence());
		assertFalse(lineItem.isReleased());
		assertNull(lineItem.getPromotedBranch());

		lineItem = lineItemService.create(new LineItemCreateRequest("Clinical finding", "Allergy"), path);
		assertNotNull(lineItem.getId());
		lineItem = lineItemService.find(lineItem.getId(), path);
		assertEquals("Clinical finding", lineItem.getTitle());
		assertEquals("Allergy", lineItem.getContent());
		assertEquals(path, lineItem.getSourceBranch());
		assertEquals(2, lineItem.getSequence());
		assertFalse(lineItem.isReleased());
		assertNull(lineItem.getPromotedBranch());
	}

	@Test
	void testUpdate() throws BusinessServiceException {
		final String path = "MAIN/ProjectA";
		LineItem lineItem = lineItemService.create(new LineItemCreateRequest("Body structure", "Demonstration Release of the Anatomy Model"), path);
		lineItem.setContent("Final Release of the Anatomy Model");
		lineItem.setSequence(3);
		LineItem updated = lineItemService.update(new LineItemUpdateRequest(lineItem.getId(), lineItem.getParentId(), lineItem.getTitle(), lineItem.getContent(), lineItem.getLevel(), lineItem.getSequence()), path);
		assertEquals(lineItem.getId(), updated.getId());
		assertEquals("Final Release of the Anatomy Model", updated.getContent());
		assertEquals(3, updated.getSequence());
		assertEquals(path, updated.getSourceBranch());
	}

	@Test
	void testDelete() throws BusinessServiceException {
		final String path = "MAIN/ProjectA";
		LineItem lineItem = lineItemService.create(new LineItemCreateRequest("Body structure"), path);
		lineItemService.delete(lineItem.getId(), path);
		assertThrows(ResourceNotFoundException.class, () -> lineItemService.find(lineItem.getId(), path));
	}

	@Test
	void testFind() throws BusinessServiceException {
		LineItem lineItem = lineItemService.create(new LineItemCreateRequest("Background","Demonstration Release of the Anatomy Model"), "MAIN/ProjectA");
		lineItemService.create(new LineItemCreateRequest("Scope", "Limbs/Girdles"), "MAIN/ProjectA");
		lineItemService.create(new LineItemCreateRequest("ScopeBody structure", "Flexor annular pulley"), "MAIN/ProjectB");
		lineItemService.create(new LineItemCreateRequest("Clinical Finding", "Muscle tendon of toes"), "MAIN/ProjectB");
		lineItemService.create(new LineItemCreateRequest("Procedure", "Muscle tendon of toes"), "MAIN");

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
	void testGetChildren() {
		List<LineItem> lineItems = testDataHelper.createLineItems("MAIN");

		List<LineItem> children = lineItemService.getChildren(lineItems.get(0).getId(), "MAIN");
		assertEquals(2, children.size());

		children = lineItemService.getChildren(lineItems.get(1).getId(), "MAIN");
		assertEquals(3, children.size());

		children = lineItemService.getChildren(lineItems.get(2).getId(), "MAIN");
		assertEquals(2, children.size());
	}

	@Test
	void testPromote_oneLineItem() throws BusinessServiceException {
		String sourceBranch = "MAIN/ProjectA/Task1";
		LineItem lineItem1 = lineItemService.create(new LineItemCreateRequest("Body structure", "Release of the Anatomy Model"), sourceBranch);
		lineItemService.promote(lineItem1.getId(), sourceBranch);

		lineItem1 = lineItemService.find(lineItem1.getId(), sourceBranch);
		assertNotNull(lineItem1.getPromotedBranch());
		assertEquals("MAIN/ProjectA", lineItem1.getPromotedBranch());
		assertNotNull(lineItem1.getEnd());

		List<LineItem> promotedLineItems = lineItemService.find("MAIN/ProjectA");
		assertEquals(1, promotedLineItems.size());
		assertEquals("MAIN/ProjectA", promotedLineItems.get(0).getSourceBranch());
		assertEquals(lineItem1.getContent(), promotedLineItems.get(0).getContent());

		sourceBranch = "MAIN/ProjectA/Task2";
		LineItem lineItem2 = lineItemService.create(new LineItemCreateRequest("Body structure", "COVID-19"), sourceBranch);
		lineItemService.promote(lineItem2.getId(), sourceBranch);

		promotedLineItems = lineItemService.find("MAIN/ProjectA");
		assertEquals(1, promotedLineItems.size());
		assertEquals("MAIN/ProjectA", promotedLineItems.get(0).getSourceBranch());
		assertEquals(ContentUtil.merge(lineItem1.getContent(), lineItem2.getContent()), promotedLineItems.get(0).getContent());
	}

	@Test
	void testPromote_manyLineItems() throws BusinessServiceException {
		final String sourceBranchA = "MAIN/ProjectA";
		lineItemService.create(new LineItemCreateRequest("Body structure", "Project A: Limbs/Girdles"), sourceBranchA);
		lineItemService.create(new LineItemCreateRequest("Clinical Finding", "Project A: COVID-19"), sourceBranchA);
		lineItemService.promote(sourceBranchA);

		List<LineItem> lineItems = lineItemRepository.findBySourceBranch(sourceBranchA);
		assertEquals(2, lineItems.size());
		lineItems.forEach(lineItem -> {
			assertNotNull(lineItem.getPromotedBranch());
			assertEquals(BranchUtil.getParentBranch(sourceBranchA), lineItem.getPromotedBranch());
			assertNotNull(lineItem.getEnd());
		});

		final String sourceBranchB = "MAIN/ProjectB";
		lineItemService.create(new LineItemCreateRequest("Body structure", "Project B: Limbs/Girdles"), sourceBranchB);
		lineItemService.promote(sourceBranchB);

		lineItems.addAll(lineItemRepository.findBySourceBranch(sourceBranchB));
		assertEquals(3, lineItems.size());

		List<LineItem> promotedLineItems = lineItemService.find("MAIN");
		assertEquals(2, promotedLineItems.size());
		promotedLineItems.forEach(lineItem -> {
			assertEquals("MAIN", lineItem.getSourceBranch());
			assertEquals(getMergedContent(lineItems, lineItem.getTitle()), lineItem.getContent());
		});
	}

	@Test
	void testVersion() throws Exception {
		String sourceBranch = "MAIN";
		LineItem lineItem = lineItemService.create(new LineItemCreateRequest("Body structure", "Demonstration Release of the Anatomy Model"), sourceBranch);

		VersionRequest versionRequest = new VersionRequest(formatter.parse("2022-01-31"));
		lineItemService.version(lineItem.getSourceBranch(), versionRequest);

		lineItem = lineItemRepository.findById(lineItem.getId()).get();
		assertNotNull(lineItem.getPromotedBranch());
		assertEquals("MAIN/2022-01-31", lineItem.getSourceBranch());
		assertEquals("MAIN/2022-01-31", lineItem.getPromotedBranch());
	}

	@Test
	void testPublish() throws Exception {
		String sourceBranch = "MAIN";
		LineItem lineItem = lineItemService.create(new LineItemCreateRequest("Body structure", "Demonstration Release of the Anatomy Model"), sourceBranch);
		assertThrows(BusinessServiceException.class, () -> lineItemService.publish(lineItem.getSourceBranch()));

		VersionRequest versionRequest = new VersionRequest(formatter.parse("2022-01-31"));
		lineItemService.version(lineItem.getSourceBranch(), versionRequest);
		LineItem versioned = lineItemRepository.findById(lineItem.getId()).get();
		assertFalse(versioned.isReleased());

		lineItemService.publish(versioned.getSourceBranch());
		LineItem published = lineItemRepository.findById(lineItem.getId()).get();
		assertTrue(published.isReleased());
	}

	@Test
	void testCloneWithoutHierarchy() throws Exception {
		lineItemService.create(new LineItemCreateRequest("Body structure", "Demonstration Release of the Anatomy Model"), "MAIN");

		lineItemService.version("MAIN", new VersionRequest(formatter.parse("2022-01-31")));
		List<LineItem> versioned = lineItemService.find("MAIN/2022-01-31");

		lineItemService.clone("MAIN/2022-01-31", new CloneRequest("MAIN"));
		List<LineItem> cloned = lineItemService.find("MAIN");

		assertEquals(versioned.size(), cloned.size());
		assertEquals(versioned.get(0).getParentId(), cloned.get(0).getParentId());
		assertEquals(versioned.get(0).getTitle(), cloned.get(0).getTitle());
		assertEquals(versioned.get(0).getLevel(), cloned.get(0).getLevel());
		assertEquals(versioned.get(0).getSequence(), cloned.get(0).getSequence());
		assertEquals(versioned.get(0).getContent(), cloned.get(0).getContent());
	}

	@Test
	void testCloneWithHierarchy() throws Exception {
		testDataHelper.createLineItems("MAIN");

		lineItemService.version("MAIN", new VersionRequest(formatter.parse("2022-01-31")));
		List<LineItem> versioned = lineItemService.find("MAIN/2022-01-31");

		lineItemService.clone("MAIN/2022-01-31", new CloneRequest("MAIN"));
		List<LineItem> cloned = lineItemService.find("MAIN");

		assertEquals(versioned.size(), cloned.size());

		List<LineItem> clonedWithHierarchy = lineItemService.findOrderedLineItems("MAIN");
		assertEquals(3, clonedWithHierarchy.size());

		LineItem clonedTopLevel1 = clonedWithHierarchy.get(0);
		assertEquals(2, clonedTopLevel1.getChildren().size());

		LineItem clonedTopLevel2 = clonedWithHierarchy.get(1);
		assertEquals(3, clonedTopLevel2.getChildren().size());

		LineItem clonedTopLevel3 = clonedWithHierarchy.get(2);
		assertEquals(2, clonedTopLevel3.getChildren().size());
	}

	@Test
	void testFindPublishedLineItems() throws Exception {
		testDataHelper.createLineItems("MAIN");
		lineItemService.version("MAIN", new VersionRequest(formatter.parse("2022-01-31")));
		lineItemService.publish("MAIN/2022-01-31");

		List<LineItem> lineItemsUnordered = lineItemService.findPublished("MAIN", false);
		assertNotNull(lineItemsUnordered);
		assertEquals(10, lineItemsUnordered.size());
		lineItemsUnordered.forEach(item -> assertTrue(item.getChildren().isEmpty()));

		List<LineItem> lineItems = lineItemService.findPublished("MAIN", true);
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
	void testFindUnpublishedLineItems() throws Exception {
		testDataHelper.createLineItems("MAIN");
		lineItemService.version("MAIN", new VersionRequest(formatter.parse("2022-01-31")));
		lineItemService.publish("MAIN/2022-01-31");

		lineItemService.create(new LineItemCreateRequest("Body structure", "Demonstration Release of the Anatomy Model"), "MAIN");

		List<LineItem> lineItems = lineItemService.findUnpublished("MAIN", false);
		assertNotNull(lineItems);
		assertEquals(1, lineItems.size());
	}

	@Test
	void testFindCategories() throws BusinessServiceException {
		LineItem contentDevelopmentActivity = lineItemService.create(new LineItemCreateRequest(LineItemService.CONTENT_DEVELOPMENT_ACTIVITY, "Content Development"), "MAIN");

		lineItemService.create(new LineItemCreateRequest("Introduction", null), "MAIN");
		lineItemService.create(new LineItemCreateRequest(contentDevelopmentActivity.getId(), "Body structure", "Body structure content"), "MAIN");
		lineItemService.create(new LineItemCreateRequest(contentDevelopmentActivity.getId(), "Procedure", "Procedure content"), "MAIN");
		lineItemService.create(new LineItemCreateRequest("Technical notes", null), "MAIN");

		List<String> categories = lineItemService.findCategories("MAIN");
		assertEquals(2, categories.size());
		assertTrue(categories.contains("Body structure"));
		assertTrue(categories.contains("Procedure"));
	}

	@Test
	void testGetVersions() throws Exception {
		String sourceBranch = "MAIN";

		lineItemService.create(new LineItemCreateRequest("Body structure", "Release 2022-01-31"), sourceBranch);
		lineItemService.create(new LineItemCreateRequest("Finding", "Release 2022-01-31"), sourceBranch);
		lineItemService.create(new LineItemCreateRequest("Procedure", "Release 2022-01-31"), sourceBranch);
		lineItemService.version(sourceBranch, new VersionRequest(formatter.parse("2022-01-31")));

		lineItemService.create(new LineItemCreateRequest("Procedure", "Release 2022-07-31"), sourceBranch);
		lineItemService.create(new LineItemCreateRequest("COVID-19", "Release 2022-07-31"), sourceBranch);
		lineItemService.version(sourceBranch, new VersionRequest(formatter.parse("2022-07-31")));

		lineItemService.create(new LineItemCreateRequest("Body structure", "Release 2023-01-31"), sourceBranch);
		lineItemService.create(new LineItemCreateRequest("Procedure", "Release 2023-01-31"), sourceBranch);
		lineItemService.version(sourceBranch, new VersionRequest(formatter.parse("2023-01-31")));

		String projectBranch = "MAIN/Project1";
		lineItemService.create(new LineItemCreateRequest("Finding", "Not versioned"), projectBranch);
		lineItemService.create(new LineItemCreateRequest("Procedure", "Not versioned"), projectBranch);
		lineItemService.promote(projectBranch);

		List<String> versions = lineItemService.getVersions("MAIN");

		assertEquals(3, versions.size());
		assertTrue(versions.contains("MAIN/2022-01-31"));
		assertTrue(versions.contains("MAIN/2022-07-31"));
		assertTrue(versions.contains("MAIN/2023-01-31"));
		assertFalse(versions.contains("MAIN/Project1"));
	}

	private String getMergedContent(List<LineItem> lineItems, final String title) {
		String[] lines = lineItems.stream()
				.filter(lineItem -> title.equals(lineItem.getTitle()))
				.map(LineItem::getContent)
				.toArray(String[]::new);
		return ContentUtil.merge(lines);
	}

}
