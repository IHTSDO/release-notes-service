package org.snomed.release.note.core.data.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Document(indexName = "lineitem")
public class LineItem {

	@Id
	@Field(type = FieldType.Keyword)
	private String id;

	@Field(type = FieldType.Keyword)
	private String subjectId;

	@Field(type = FieldType.Keyword)
	private String parentId;

	@Field(type = FieldType.Integer)
	private Integer level;

	@Field(type = FieldType.Text)
	private String content;

	@Field(type = FieldType.Integer)
	private Integer sequence;

	@Field(type = FieldType.Keyword)
	private String sourceBranch;

	@Field(type = FieldType.Keyword)
	private String promotedBranch;

	@Field(type = FieldType.Date, format = DateFormat.year_month_day)
	private LocalDate startDate;

	@Field(type = FieldType.Date, format = DateFormat.year_month_day)
	private LocalDate endDate;

	@Field(type = FieldType.Boolean)
	private Boolean released;

	public LineItem() {
	}

	public LineItem(String subjectId, String content, String sourceBranch) {
		this();
		this.subjectId = subjectId;
		this.level = 0;
		this.content = content;
		this.sequence = 0;
		this.sourceBranch = sourceBranch;
		this.startDate = LocalDate.now();
		this.released = false;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public String getSourceBranch() {
		return sourceBranch;
	}

	public void setSourceBranch(String sourceBranch) {
		this.sourceBranch = sourceBranch;
	}

	public String getPromotedBranch() {
		return promotedBranch;
	}

	public void setPromotedBranch(String promotedBranch) {
		this.promotedBranch = promotedBranch;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public Boolean getReleased() {
		return released;
	}

	public void setReleased(Boolean released) {
		this.released = released;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LineItem lineItem = (LineItem) o;

		if (id != null || lineItem.id != null) {
			return Objects.equals(id, lineItem.id);
		}

		return Objects.equals(subjectId, lineItem.subjectId)
				&& Objects.equals(content, lineItem.content)
				&& Objects.equals(sourceBranch, lineItem.sourceBranch);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, subjectId);
	}

	@Override
	public String toString() {
		return "LineItem{" +
				"id='" + id + '\'' +
				", subjectId='" + subjectId + '\'' +
				", parentId='" + parentId + '\'' +
				", level='" + level + '\'' +
				", content=" + content + '\'' +
				", sequence='" + sequence + '\'' +
				", sourceBranch='" + sourceBranch + '\'' +
				", promotedBranch='" + promotedBranch + '\'' +
				", startDate='" + formatDate(startDate) + '\'' +
				", endDate='" + formatDate(endDate) + '\'' +
				", released='" + released + '\'' +
				'}';
	}

	private String formatDate(LocalDate date) {
		return (date == null) ? "null" : date.format(DateTimeFormatter.ISO_LOCAL_DATE);
	}
}
