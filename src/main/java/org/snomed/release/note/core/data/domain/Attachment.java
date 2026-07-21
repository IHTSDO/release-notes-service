package org.snomed.release.note.core.data.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.Date;
import java.util.Objects;

@Document(indexName = "#{@indexNameProvider.getIndexNameWithPrefix('attachment')}")
@Setting(settingPath = "elasticsearch-settings.json")
public class Attachment {

	public static final String FIELD_SOURCE_BRANCH = "sourceBranch";
	public static final String FIELD_REPORT_TYPE = "reportType";

	@Id
	@Field(type = FieldType.Keyword)
	private String id;

	@Field(type = FieldType.Keyword)
	private String reportType;

	@Field(type = FieldType.Keyword)
	private String filename;

	@Field(type = FieldType.Keyword)
	private String contentType;

	@JsonIgnore
	@Field(type = FieldType.Binary)
	private byte[] content;

	@Field(type = FieldType.Keyword)
	private String sourceBranch;

	@Field(type = FieldType.Keyword)
	private String uploadedBy;

	@Field(type = FieldType.Date)
	private Date uploadedAt;

	@Field(type = FieldType.Boolean)
	private boolean released;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getReportType() {
		return reportType;
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getSourceBranch() {
		return sourceBranch;
	}

	public void setSourceBranch(String sourceBranch) {
		this.sourceBranch = sourceBranch;
	}

	public String getUploadedBy() {
		return uploadedBy;
	}

	public void setUploadedBy(String uploadedBy) {
		this.uploadedBy = uploadedBy;
	}

	public Date getUploadedAt() {
		return uploadedAt;
	}

	public void setUploadedAt(Date uploadedAt) {
		this.uploadedAt = uploadedAt;
	}

	public boolean isReleased() {
		return released;
	}

	public void setReleased(boolean released) {
		this.released = released;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Attachment that = (Attachment) o;
		if (id != null || that.id != null) {
			return Objects.equals(id, that.id);
		}
		return Objects.equals(reportType, that.reportType) && Objects.equals(sourceBranch, that.sourceBranch);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, reportType, sourceBranch);
	}
}
