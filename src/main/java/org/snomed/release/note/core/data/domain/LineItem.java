package org.snomed.release.note.core.data.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static org.snomed.release.note.core.util.ContentUtil.LINE_BREAK;

@Document(indexName = "#{@indexNameProvider.getIndexNameWithPrefix('lineitem')}")
@Setting(settingPath = "elasticsearch-settings.json")
public class LineItem implements LineItemView {

	@Id
	@Field(type = FieldType.Keyword)
	private String id;

	@Field(type = FieldType.Keyword)
	private String parentId;

	@Field(type = FieldType.Integer)
	private Integer level;

	@Field(type = FieldType.Keyword)
	private String title;

	@Field(type = FieldType.Text)
	private String content;

	@Field(type = FieldType.Text)
	private String notes;

	@Field(type = FieldType.Text)
	private String changeType;

	@Field(type = FieldType.Text)
	private String additionalChangeTypes;

	@Field(type = FieldType.Text)
	private String hierarchy;

	@Field(type = FieldType.Text)
	private String changedInAdditionalHierarchy;

	@Field(type = FieldType.Integer)
	private Integer numberEditedConcepts;

	@Field(type = FieldType.Text)
	private String futureChangesPlanned;

	@Field(type = FieldType.Text)
	private String linkContentTracker;

	@Field(type = FieldType.Text)
	private String conceptInactivations;

	@Field(type = FieldType.Text)
	private String linkBriefingNote;

	@Field(type = FieldType.Text)
	private String linkToTemplate;

	@Field(type = FieldType.Text)
	private String descriptionChanges;

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

	public LineItem() {
		this.children = new ArrayList<>();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public boolean isReleased() {
		return released;
	}

	public void setReleased(boolean released) {
		this.released = released;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getChangeType() {
		return changeType;
	}

	public void setChangeType(String changeType) {
		this.changeType = changeType;
	}

	public String getAdditionalChangeTypes() {
		return additionalChangeTypes;
	}

	public void setAdditionalChangeTypes(String additionalChangeTypes) {
		this.additionalChangeTypes = additionalChangeTypes;
	}

	public String getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}

	public String getChangedInAdditionalHierarchy() {
		return changedInAdditionalHierarchy;
	}

	public void setChangedInAdditionalHierarchy(String changedInAdditionalHierarchy) {
		this.changedInAdditionalHierarchy = changedInAdditionalHierarchy;
	}

	public Integer getNumberEditedConcepts() {
		return numberEditedConcepts;
	}

	public void setNumberEditedConcepts(Integer numberEditedConcepts) {
		this.numberEditedConcepts = numberEditedConcepts;
	}

	public String getFutureChangesPlanned() {
		return futureChangesPlanned;
	}

	public void setFutureChangesPlanned(String futureChangesPlanned) {
		this.futureChangesPlanned = futureChangesPlanned;
	}

	public String getLinkContentTracker() {
		return linkContentTracker;
	}

	public void setLinkContentTracker(String linkContentTracker) {
		this.linkContentTracker = linkContentTracker;
	}

	public String getConceptInactivations() {
		return conceptInactivations;
	}

	public void setConceptInactivations(String conceptInactivations) {
		this.conceptInactivations = conceptInactivations;
	}

	public String getLinkBriefingNote() {
		return linkBriefingNote;
	}

	public void setLinkBriefingNote(String linkBriefingNote) {
		this.linkBriefingNote = linkBriefingNote;
	}

	public String getLinkToTemplate() {
		return linkToTemplate;
	}

	public void setLinkToTemplate(String linkToTemplate) {
		this.linkToTemplate = linkToTemplate;
	}

	public String getDescriptionChanges() {
		return descriptionChanges;
	}

	public void setDescriptionChanges(String descriptionChanges) {
		this.descriptionChanges = descriptionChanges;
	}

	@Override
	public List<LineItem> getChildren() {
		return children;
	}

	public void setChildren(List<LineItem> children) {
		this.children = children;
	}

	public void generateContent() {
		StringBuilder builder = new StringBuilder();
		if (StringUtils.hasLength(changeType)) {
			builder.append("Change Type: ").append(changeType).append(LINE_BREAK);
		}
		if (StringUtils.hasLength(additionalChangeTypes)) {
			builder.append("Additional change types: ").append(additionalChangeTypes).append(LINE_BREAK);
		}
		if (StringUtils.hasLength(hierarchy)) {
			builder.append("Hierarchy: ").append(hierarchy).append(LINE_BREAK);
		}
		if (StringUtils.hasLength(changedInAdditionalHierarchy)) {
			builder.append("Changes in additional hierarchies: ").append(changedInAdditionalHierarchy).append(LINE_BREAK);
		}
		if (numberEditedConcepts != null) {
			builder.append("Number of concepts edited (approx): ").append(numberEditedConcepts).append(LINE_BREAK);
		}
		if (StringUtils.hasLength(futureChangesPlanned)) {
			builder.append("Future changes planned: ").append(futureChangesPlanned).append(LINE_BREAK);
		}
		if (StringUtils.hasLength(linkContentTracker)) {
			builder.append("Link to content tracker: ").append(linkContentTracker).append(LINE_BREAK);
		}
		if (StringUtils.hasLength(conceptInactivations)) {
			builder.append("Concept inactivations: ").append(conceptInactivations).append(LINE_BREAK);
		}
		if (StringUtils.hasLength(linkBriefingNote)) {
			builder.append("Link to briefing note: ").append(linkBriefingNote).append(LINE_BREAK);
		}
		if (StringUtils.hasLength(linkToTemplate)) {
			builder.append("Link to template: ").append(linkToTemplate).append(LINE_BREAK);
		}
		if (StringUtils.hasLength(descriptionChanges)) {
			builder.append("Description changes: ").append(descriptionChanges).append(LINE_BREAK);
		}
		if (StringUtils.hasLength(notes)) {
			builder.append("Notes: ").append(notes).append(LINE_BREAK);
		}
		String content = builder.toString();
		if (content != null && content.endsWith(LINE_BREAK)) {
			content = content.replaceAll(LINE_BREAK + "$", "");
		}
		this.setContent(content);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		LineItem lineItem = (LineItem) o;

		if (id != null || lineItem.id != null) {
			return Objects.equals(id, lineItem.id);
		}

		return Objects.equals(title, lineItem.title)
				&& Objects.equals(content, lineItem.content)
				&& Objects.equals(sourceBranch, lineItem.sourceBranch);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id, title);
	}

}
