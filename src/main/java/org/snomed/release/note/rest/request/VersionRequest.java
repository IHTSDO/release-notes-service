package org.snomed.release.note.rest.request;

public class VersionRequest {

	private String releaseBranch;

	public VersionRequest(String releaseBranch) {
		this.releaseBranch = releaseBranch;
	}

	public String getReleaseBranch() {
		return releaseBranch;
	}

}
