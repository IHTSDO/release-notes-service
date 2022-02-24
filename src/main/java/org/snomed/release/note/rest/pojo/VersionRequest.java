package org.snomed.release.note.rest.pojo;

public class VersionRequest {

	private final String releaseBranch;

	public VersionRequest(String releaseBranch) {
		this.releaseBranch = releaseBranch;
	}

	public String getReleaseBranch() {
		return releaseBranch;
	}

}
