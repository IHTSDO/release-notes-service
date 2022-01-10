package org.snomed.release.note.rest.request;

public class PromoteRequest {

	private String branchPath;

	public PromoteRequest() {
	}

	public PromoteRequest(String branchPath) {
		this.branchPath = branchPath;
	}

	public String getBranchPath() {
		return branchPath;
	}

	public void setBranchPath(String branchPath) {
		this.branchPath = branchPath;
	}
}
