package org.snomed.release.note.rest.pojo;

import org.elasticsearch.common.collect.HppcMaps;

public class LineItemCreateRequest {

	private String subjectId;
	private String parentId;
	private Integer level;
	private Integer sequence;
	private String content;

	public LineItemCreateRequest() {
	}

	public LineItemCreateRequest(String subjectId) {
		this(subjectId, null, null, null, null);
	}

	public LineItemCreateRequest(String subjectId, String content) {
		this(subjectId, null, null, null, content);
	}

	public LineItemCreateRequest(String subjectId, String parentId, Integer level, Integer sequence, String content) {
		super();
		this.subjectId = subjectId;
		this.parentId = parentId;
		this.level = level;
		this.sequence = sequence;
		this.content = content;
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
