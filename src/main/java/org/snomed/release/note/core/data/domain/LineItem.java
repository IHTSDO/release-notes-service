package org.snomed.release.note.core.data.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.*;

@Document(indexName = "#{@indexNameProvider.getIndexNameWithPrefix('lineitem')}")
@Setting(settingPath = "elasticsearch-settings.json")
public class LineItem implements LineItemView {

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

	@Field(type = FieldType.Date)
	private Date start;

	@Field(type = FieldType.Date)
	private Date end;

	@Field(type = FieldType.Boolean)
	private boolean released;

	@Transient
	private List<LineItem> children;

	@Transient
	private Subject subject;

	public LineItem() {
		this.children = new ArrayList<>();
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

	public String getContentNotNull() {
		return content == null ? "" : content;
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

	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public boolean getReleased() {
		return released;
	}

	public void setReleased(boolean released) {
		this.released = released;
	}

	@Override
	public List<LineItem> getChildren() {
		return children;
	}

	public void setChildren(List<LineItem> children) {
		this.children = children;
	}

	@Override
	public String getTitle() {
		return subject != null ? subject.getTitle() : null;
	}

	public void setSubject(Subject subject) {
		this.subject = subject;
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
				", level=" + level +
				", content='" + content + '\'' +
				", sequence=" + sequence +
				", sourceBranch='" + sourceBranch + '\'' +
				", promotedBranch='" + promotedBranch + '\'' +
				", start=" + start +
				", end=" + end +
				", released=" + released +
				", children=" + children +
				", subject=" + subject +
				'}';
	}
}
