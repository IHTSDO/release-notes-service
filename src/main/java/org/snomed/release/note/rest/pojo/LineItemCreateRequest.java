package org.snomed.release.note.rest.pojo;

public class LineItemCreateRequest {

	private String parentId;
	private String title;
	private String content;
	private Integer level;
	private Integer sequence;
	private String changeType;
	private String additionalChangeTypes;
	private String hierarchy;
	private String changedInAdditionalHierarchy;
	private Integer numberEditedConcepts;
	private String futureChangesPlanned;
	private String linkContentTracker;
	private String conceptInactivations;
	private String linkBriefingNote;
	private String linkToTemplate;
	private String descriptionChanges;
	private String notes;

	public LineItemCreateRequest() {
	}

	public LineItemCreateRequest(String title) {
		this();
		this.title = title;
	}

	public LineItemCreateRequest(String title, String content) {
		this();
		this.title = title;
		this.content = content;
	}

	public LineItemCreateRequest(String parentId, String title, String content) {
		this();
		this.parentId = parentId;
		this.title = title;
		this.content = content;
	}

	public LineItemCreateRequest(String parentId, String title, String content, Integer level, Integer sequence) {
		this();
		this.parentId = parentId;
		this.title = title;
		this.content = content;
		this.level = level;
		this.sequence = sequence;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
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

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getNotes() {
		return notes;
	}
}
