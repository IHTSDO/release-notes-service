package org.snomed.release.note.rest.pojo;

public class LineItemUpdateRequest {

	private String id;
	private String parentId;
	private Integer level;
	private Integer sequence;
	private String content;

	public LineItemUpdateRequest() {
	}

	public LineItemUpdateRequest(String id, String parentId, Integer level, Integer sequence, String content) {
		super();
		this.id = id;
		this.parentId = parentId;
		this.level = level;
		this.sequence = sequence;
		this.content = content;
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

	public Integer getSequence() {
		return sequence;
	}

	public void setSequence(Integer sequence) {
		this.sequence = sequence;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
