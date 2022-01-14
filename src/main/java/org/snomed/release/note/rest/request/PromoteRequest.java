package org.snomed.release.note.rest.request;

public class PromoteRequest {

	private String promotedBranch;

	public PromoteRequest() {
	}

	public PromoteRequest(String promotedBranch) {
		this.promotedBranch = promotedBranch;
	}

	public String getPromotedBranch() {
		return promotedBranch;
	}

	public void setPromotedBranch(String promotedBranch) {
		this.promotedBranch = promotedBranch;
	}
}
