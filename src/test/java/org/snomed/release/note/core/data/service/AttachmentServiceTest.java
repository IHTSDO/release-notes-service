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

class AttachmentServiceTest extends AbstractTest {

	@Autowired
	private AttachmentService attachmentService;

	@Test
	void testUploadAndFind() throws BusinessServiceException {
		final String path = "MAIN";
		byte[] csv = "id,name\n1,Concept A\n".getBytes(StandardCharsets.UTF_8);
		MockMultipartFile file = new MockMultipartFile("file", "components.csv", "text/csv", csv);

		Attachment uploaded = attachmentService.upload(path, file);
		assertNotNull(uploaded.getId());
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
	void testMultipleAttachments() throws BusinessServiceException {
		final String path = "MAIN";
		MockMultipartFile first = new MockMultipartFile("file", "old.csv", "text/csv", "a,b\n".getBytes(StandardCharsets.UTF_8));
		MockMultipartFile second = new MockMultipartFile("file", "new.csv", "text/csv", "c,d\n".getBytes(StandardCharsets.UTF_8));

		Attachment firstUpload = attachmentService.upload(path, first);
		Attachment secondUpload = attachmentService.upload(path, second);

		assertNotEquals(firstUpload.getId(), secondUpload.getId());
		assertEquals(2, attachmentService.findByBranch(path).size());
	}

	@Test
	void testDelete() throws BusinessServiceException {
		final String path = "MAIN";
		MockMultipartFile file = new MockMultipartFile("file", "attachment.csv", "text/csv", "x,y\n".getBytes(StandardCharsets.UTF_8));
		Attachment uploaded = attachmentService.upload(path, file);
		String uploadedId = uploaded.getId();

		attachmentService.delete(path, uploadedId);
		assertThrows(ResourceNotFoundException.class, () -> attachmentService.find(path, uploadedId));
	}

	@Test
	void testUploadRejectsNonCsv() {
		MockMultipartFile file = new MockMultipartFile("file", "attachment.txt", "text/plain", "nope".getBytes(StandardCharsets.UTF_8));
		assertThrows(BadRequestException.class, () -> attachmentService.upload("MAIN", file));
	}

	@Test
	void testUploadRejectsEmptyFile() {
		MockMultipartFile file = new MockMultipartFile("file", "attachment.csv", "text/csv", new byte[0]);
		assertThrows(BadRequestException.class, () -> attachmentService.upload("MAIN", file));
	}

	@Test
	void testVersionClonesAttachments() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "components.csv", "text/csv", "id,fsn\n100,Heart\n".getBytes(StandardCharsets.UTF_8));
		attachmentService.upload("MAIN", file);
		lineItemService.create(new LineItemCreateRequest("Body structure", "Anatomy notes"), "MAIN");

		lineItemService.version("MAIN", new VersionRequest(DATE_FORMATTER.parse("2022-01-31")));

		List<Attachment> versioned = attachmentService.findByBranch("MAIN/2022-01-31");
		assertEquals(1, versioned.size());
		assertEquals("components.csv", versioned.get(0).getFilename());
		assertFalse(versioned.get(0).isReleased());

		Attachment downloaded = attachmentService.find("MAIN/2022-01-31", versioned.get(0).getId());
		assertArrayEquals("id,fsn\n100,Heart\n".getBytes(StandardCharsets.UTF_8), downloaded.getContent());

		assertEquals(0, attachmentService.findByBranch("MAIN").size());
	}

	@Test
	void testPublishLocksAttachments() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "descriptions.csv", "text/csv", "id,term\n".getBytes(StandardCharsets.UTF_8));
		attachmentService.upload("MAIN", file);
		lineItemService.create(new LineItemCreateRequest("Background", "Notes"), "MAIN");
		lineItemService.version("MAIN", new VersionRequest(DATE_FORMATTER.parse("2022-01-31")));
		lineItemService.publish("MAIN/2022-01-31");

		List<Attachment> publishedList = attachmentService.findByBranch("MAIN/2022-01-31");
		assertEquals(1, publishedList.size());
		Attachment published = publishedList.get(0);
		assertTrue(published.isReleased());
		String publishedId = published.getId();

		MockMultipartFile replacement = new MockMultipartFile(
				"file", "other.csv", "text/csv", "a,b\n".getBytes(StandardCharsets.UTF_8));
		assertThrows(BadConfigurationException.class, () -> attachmentService.upload("MAIN/2022-01-31", replacement));
		assertThrows(BadConfigurationException.class, () -> attachmentService.delete("MAIN/2022-01-31", publishedId));
	}

	@Test
	void testUploadAndDeleteAllowedOnVersionedBranch() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "components.csv", "text/csv", "id,fsn\n".getBytes(StandardCharsets.UTF_8));
		attachmentService.upload("MAIN", file);
		lineItemService.create(new LineItemCreateRequest("Body structure", "Notes"), "MAIN");
		lineItemService.version("MAIN", new VersionRequest(DATE_FORMATTER.parse("2022-01-31")));

		MockMultipartFile uploadOnVersion = new MockMultipartFile(
				"file", "extra.csv", "text/csv", "a,b\n".getBytes(StandardCharsets.UTF_8));
		Attachment uploaded = attachmentService.upload("MAIN/2022-01-31", uploadOnVersion);
		assertNotNull(uploaded.getId());
		assertEquals("extra.csv", uploaded.getFilename());
		assertEquals(2, attachmentService.findByBranch("MAIN/2022-01-31").size());

		attachmentService.delete("MAIN/2022-01-31", uploaded.getId());
		assertEquals(1, attachmentService.findByBranch("MAIN/2022-01-31").size());
	}
}
