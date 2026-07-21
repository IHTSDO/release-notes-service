package org.snomed.release.note.core.data.service;

import org.ihtsdo.otf.rest.exception.BadConfigurationException;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.snomed.release.note.AbstractTest;
import org.snomed.release.note.core.data.domain.Attachment;
import org.snomed.release.note.rest.pojo.LineItemCreateRequest;
import org.snomed.release.note.rest.pojo.VersionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.snomed.release.note.core.data.service.LineItemService.DATE_FORMATTER;

public class AttachmentServiceTest extends AbstractTest {

	@Autowired
	private AttachmentService attachmentService;

	@Test
	void testUploadAndFind() throws BusinessServiceException {
		final String path = "MAIN";
		byte[] csv = "id,name\n1,Concept A\n".getBytes(StandardCharsets.UTF_8);
		MockMultipartFile file = new MockMultipartFile("file", "components.csv", "text/csv", csv);

		Attachment uploaded = attachmentService.upload(path, "New and changed components", file);
		assertNotNull(uploaded.getId());
		assertEquals("New and changed components", uploaded.getReportType());
		assertEquals("components.csv", uploaded.getFilename());
		assertEquals(path, uploaded.getSourceBranch());
		assertFalse(uploaded.isReleased());

		List<Attachment> attachments = attachmentService.findByBranch(path);
		assertEquals(1, attachments.size());
		assertEquals("components.csv", attachments.get(0).getFilename());

		Attachment found = attachmentService.find(path, uploaded.getId());
		assertArrayEquals(csv, found.getContent());
	}

	@Test
	void testUploadAnyReportType() throws BusinessServiceException {
		final String path = "MAIN";
		MockMultipartFile file = new MockMultipartFile("file", "custom.csv", "text/csv", "a,b\n".getBytes(StandardCharsets.UTF_8));

		Attachment uploaded = attachmentService.upload(path, "Custom translator export", file);
		assertEquals("Custom translator export", uploaded.getReportType());
		assertEquals(1, attachmentService.findByBranch(path).size());
	}

	@Test
	void testReplaceSameReportType() throws BusinessServiceException {
		final String path = "MAIN";
		MockMultipartFile first = new MockMultipartFile("file", "old.csv", "text/csv", "a,b\n".getBytes(StandardCharsets.UTF_8));
		MockMultipartFile second = new MockMultipartFile("file", "new.csv", "text/csv", "c,d\n".getBytes(StandardCharsets.UTF_8));

		Attachment firstUpload = attachmentService.upload(path, "New descriptions", first);
		Attachment replaced = attachmentService.upload(path, "New descriptions", second);

		assertEquals(firstUpload.getId(), replaced.getId());
		assertEquals("new.csv", replaced.getFilename());
		assertEquals(1, attachmentService.findByBranch(path).size());
		assertArrayEquals("c,d\n".getBytes(StandardCharsets.UTF_8),
				attachmentService.find(path, replaced.getId()).getContent());
	}

	@Test
	void testFindByBranchAndReportType() throws BusinessServiceException {
		final String path = "MAIN";
		attachmentService.upload(path, "New and changed components",
				new MockMultipartFile("file", "components.csv", "text/csv", "a,b\n".getBytes(StandardCharsets.UTF_8)));
		attachmentService.upload(path, "New descriptions",
				new MockMultipartFile("file", "descriptions.csv", "text/csv", "c,d\n".getBytes(StandardCharsets.UTF_8)));

		List<Attachment> filtered = attachmentService.findByBranchAndReportType(path, "New descriptions");
		assertEquals(1, filtered.size());
		assertEquals("New descriptions", filtered.get(0).getReportType());
		assertEquals("descriptions.csv", filtered.get(0).getFilename());

		assertEquals(0, attachmentService.findByBranchAndReportType(path, "Nonexistent").size());
	}

	@Test
	void testDelete() throws BusinessServiceException {
		final String path = "MAIN";
		MockMultipartFile file = new MockMultipartFile("file", "report.csv", "text/csv", "x,y\n".getBytes(StandardCharsets.UTF_8));
		Attachment uploaded = attachmentService.upload(path, "Temp attachment", file);

		attachmentService.delete(path, uploaded.getId());
		assertThrows(ResourceNotFoundException.class, () -> attachmentService.find(path, uploaded.getId()));
	}

	@Test
	void testUploadRejectsMissingReportType() {
		MockMultipartFile file = new MockMultipartFile("file", "report.csv", "text/csv", "a,b\n".getBytes(StandardCharsets.UTF_8));
		assertThrows(BadRequestException.class, () -> attachmentService.upload("MAIN", "  ", file));
	}

	@Test
	void testUploadRejectsNonCsv() {
		MockMultipartFile file = new MockMultipartFile("file", "report.txt", "text/plain", "nope".getBytes(StandardCharsets.UTF_8));
		assertThrows(BadRequestException.class,
				() -> attachmentService.upload("MAIN", "Anything", file));
	}

	@Test
	void testUploadRejectsEmptyFile() {
		MockMultipartFile file = new MockMultipartFile("file", "report.csv", "text/csv", new byte[0]);
		assertThrows(BadRequestException.class,
				() -> attachmentService.upload("MAIN", "Anything", file));
	}

	@Test
	void testVersionClonesAttachments() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "components.csv", "text/csv", "id,fsn\n100,Heart\n".getBytes(StandardCharsets.UTF_8));
		attachmentService.upload("MAIN", "New and changed components", file);
		lineItemService.create(new LineItemCreateRequest("Body structure", "Anatomy notes"), "MAIN");

		lineItemService.version("MAIN", new VersionRequest(DATE_FORMATTER.parse("2022-01-31")));

		List<Attachment> versioned = attachmentService.findByBranch("MAIN/2022-01-31");
		assertEquals(1, versioned.size());
		assertEquals("New and changed components", versioned.get(0).getReportType());
		assertEquals("components.csv", versioned.get(0).getFilename());
		assertFalse(versioned.get(0).isReleased());

		Attachment downloaded = attachmentService.find("MAIN/2022-01-31", versioned.get(0).getId());
		assertArrayEquals("id,fsn\n100,Heart\n".getBytes(StandardCharsets.UTF_8), downloaded.getContent());

		// Source branch attachments are removed after versioning
		assertEquals(0, attachmentService.findByBranch("MAIN").size());
	}

	@Test
	void testPublishLocksAttachments() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "descriptions.csv", "text/csv", "id,term\n".getBytes(StandardCharsets.UTF_8));
		attachmentService.upload("MAIN", "New descriptions", file);
		lineItemService.create(new LineItemCreateRequest("Background", "Notes"), "MAIN");
		lineItemService.version("MAIN", new VersionRequest(DATE_FORMATTER.parse("2022-01-31")));
		lineItemService.publish("MAIN/2022-01-31");

		List<Attachment> publishedList = attachmentService.findByBranch("MAIN/2022-01-31");
		assertEquals(1, publishedList.size());
		assertTrue(publishedList.get(0).isReleased());

		MockMultipartFile replacement = new MockMultipartFile(
				"file", "other.csv", "text/csv", "a,b\n".getBytes(StandardCharsets.UTF_8));
		assertThrows(BadConfigurationException.class,
				() -> attachmentService.upload("MAIN/2022-01-31", "New descriptions", replacement));
		assertThrows(BadConfigurationException.class,
				() -> attachmentService.delete("MAIN/2022-01-31", publishedList.get(0).getId()));
	}
}
