package org.snomed.release.note.rest.request;

public class MergeRequest {

	private String subjectId;

	private String branchPath;

	public MergeRequest() {
	}

	public MergeRequest(String subjectId, String branchPath) {
		this.subjectId = subjectId;
		this.branchPath = branchPath;
	}

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}

	public String getBranchPath() {
		return branchPath;
	}

	public void setBranchPath(String branchPath) {
		this.branchPath = branchPath;
	}
}
