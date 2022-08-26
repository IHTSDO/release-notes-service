package org.snomed.release.note.rest.pojo;

public class LineItemCreateRequest {

	private String parentId;
	private String title;
	private String content;
	private Integer level;
	private Integer sequence;

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
}
