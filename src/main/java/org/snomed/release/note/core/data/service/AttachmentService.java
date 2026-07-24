package org.snomed.release.note.core.data.service;

import org.ihtsdo.otf.rest.exception.BadConfigurationException;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.ihtsdo.sso.integration.SecurityUtil;
import org.snomed.release.note.core.data.domain.Attachment;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.repository.AttachmentRepository;
import org.snomed.release.note.core.data.repository.LineItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Service
public class AttachmentService {

	public static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024L;
	public static final String CSV_CONTENT_TYPE = "text/csv";


	private final AttachmentRepository attachmentRepository;

	private final LineItemRepository lineItemRepository;

	@Autowired
	public AttachmentService(AttachmentRepository attachmentRepository, LineItemRepository lineItemRepository) {
		this.attachmentRepository = attachmentRepository;
		this.lineItemRepository = lineItemRepository;
	}

	public List<Attachment> findByBranch(String path) {
		return attachmentRepository.findBySourceBranch(path);
	}

	public List<Attachment> findByBranchAndReportType(String path, String reportType) {
		return attachmentRepository.findAllBySourceBranchAndReportType(path, reportType);
	}

	public Attachment find(String path, String id) {
		Attachment attachment = attachmentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException(
						"No attachment found for id '" + id + "' on branch '" + path + "'"));
		if (!path.equals(attachment.getSourceBranch())) {
			throw new ResourceNotFoundException(
					"No attachment found for id '" + id + "' on branch '" + path + "'");
		}
		return attachment;
	}

	public Attachment upload(String path, String reportType, MultipartFile file) throws BusinessServiceException {
		validateReportType(reportType);
		validateUpload(file);
		assertBranchNotReleased(path);

		String trimmedReportType = reportType.trim();
		Attachment attachment = new Attachment();

		try {
			attachment.setReportType(trimmedReportType);
			attachment.setFilename(sanitizeFilename(file.getOriginalFilename()));
			attachment.setContentType(resolveContentType(file));
			attachment.setContent(file.getBytes());
			attachment.setSourceBranch(path);
			attachment.setUploadedBy(SecurityUtil.getUsername());
			attachment.setUploadedAt(new Date());
			attachment.setReleased(false);
			return attachmentRepository.save(attachment);
		} catch (IOException e) {
			throw new BusinessServiceException("Failed to read uploaded file", e);
		}
	}

	public void delete(String path, String id) throws BusinessServiceException {
		assertBranchNotReleased(path);
		Attachment attachment = find(path, id);
		attachmentRepository.delete(attachment);
	}

	public void version(String sourcePath, String releaseBranch) {
		List<Attachment> attachments = findByBranch(sourcePath);
		for (Attachment attachment : attachments) {
			Attachment clone = new Attachment();
			clone.setReportType(attachment.getReportType());
			clone.setFilename(attachment.getFilename());
			clone.setContentType(attachment.getContentType());
			clone.setContent(attachment.getContent());
			clone.setSourceBranch(releaseBranch);
			clone.setUploadedBy(attachment.getUploadedBy());
			clone.setUploadedAt(attachment.getUploadedAt());
			clone.setReleased(false);
			attachmentRepository.save(clone);
		}
		attachmentRepository.deleteAll(attachments);
	}

	public void publish(String path) {
		List<Attachment> attachments = findByBranch(path);
		attachments.forEach(attachment -> attachment.setReleased(true));
		attachmentRepository.saveAll(attachments);
	}

	public void deleteAll() {
		attachmentRepository.deleteAll();
	}

	private void assertBranchNotReleased(String path) throws BadConfigurationException {
		boolean lineItemsReleased = lineItemRepository.findBySourceBranch(path).stream().anyMatch(LineItem::isReleased);
		boolean attachmentsReleased = findByBranch(path).stream().anyMatch(Attachment::isReleased);
		if (lineItemsReleased || attachmentsReleased) {
			throw new BadConfigurationException(
					"Branch '" + path + "' has already been released and its attachments cannot be changed");
		}
	}

	private void validateReportType(String reportType) throws BadRequestException {
		if (!StringUtils.hasLength(reportType) || !StringUtils.hasLength(reportType.trim())) {
			throw new BadRequestException("'reportType' is required");
		}
	}

	private void validateUpload(MultipartFile file) throws BadRequestException {
		if (file == null || file.isEmpty()) {
			throw new BadRequestException("A CSV file is required");
		}
		if (file.getSize() > MAX_FILE_SIZE_BYTES) {
			throw new BadRequestException("File size exceeds the maximum of 10MB");
		}
		String originalFilename = file.getOriginalFilename();
		if (!StringUtils.hasLength(originalFilename) || !originalFilename.toLowerCase(Locale.ROOT).endsWith(".csv")) {
			throw new BadRequestException("Only .csv files are allowed");
		}
	}

	private String sanitizeFilename(String originalFilename) {
		if (!StringUtils.hasLength(originalFilename)) {
			return "attachment.csv";
		}
		int slash = Math.max(originalFilename.lastIndexOf('/'), originalFilename.lastIndexOf('\\'));
		return slash >= 0 ? originalFilename.substring(slash + 1) : originalFilename;
	}

	private String resolveContentType(MultipartFile file) {
		String contentType = file.getContentType();
		if (StringUtils.hasLength(contentType)) {
			return contentType;
		}
		return CSV_CONTENT_TYPE;
	}
}
