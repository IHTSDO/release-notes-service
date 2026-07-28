package org.snomed.release.note.rest;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.snomed.release.note.core.data.domain.Attachment;
import org.snomed.release.note.core.data.service.AttachmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@Tag(name = "Attachments")
public class AttachmentController {

	private final AttachmentService attachmentService;

	@Autowired
	public AttachmentController(AttachmentService attachmentService) {
		this.attachmentService = attachmentService;
	}

	@GetMapping(value = "/{path}/attachments", produces = MediaType.APPLICATION_JSON_VALUE)
	@Operation(summary = "List CSV attachments for a branch (metadata only).")
	public List<Attachment> listAttachments(@PathVariable String path) {
		return attachmentService.findByBranch(BranchPathUriUtil.decodePath(path));
	}

	@GetMapping(value = "/{path}/attachments/{id}")
	@Operation(summary = "Download a CSV attachment.")
	public ResponseEntity<byte[]> downloadAttachment(
			@PathVariable String path,
			@PathVariable String id) {
		Attachment attachment = attachmentService.find(BranchPathUriUtil.decodePath(path), id);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.parseMediaType(
				attachment.getContentType() != null ? attachment.getContentType() : AttachmentService.CSV_CONTENT_TYPE));
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + attachment.getFilename() + "\"");

		return new ResponseEntity<>(attachment.getContent(), headers, HttpStatus.OK);
	}

	@PostMapping(value = "/{path}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("hasPermission('AUTHOR', #path) || hasPermission('PROJECT_LEAD', #path) || hasPermission('RELEASE_LEAD', #path) || hasPermission('RELEASE_ADMIN', #path) || hasPermission('RELEASE_MANAGER', #path)")
	@Operation(summary = "Upload a CSV attachment.")
	public Attachment uploadAttachment(
			@PathVariable String path,
			@RequestParam("file") MultipartFile file) throws BusinessServiceException {
		return attachmentService.upload(BranchPathUriUtil.decodePath(path), file);
	}

	@DeleteMapping(value = "/{path}/attachments/{id}")
	@PreAuthorize("hasPermission('AUTHOR', #path) || hasPermission('PROJECT_LEAD', #path) || hasPermission('RELEASE_LEAD', #path) || hasPermission('RELEASE_ADMIN', #path) || hasPermission('RELEASE_MANAGER', #path)")
	@Operation(summary = "Delete a CSV attachment.")
	public void deleteAttachment(
			@PathVariable String path,
			@PathVariable String id) throws BusinessServiceException {
		attachmentService.delete(BranchPathUriUtil.decodePath(path), id);
	}
}
