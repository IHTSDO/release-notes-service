package org.snomed.release.note.rest.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;

public class VersionRequest {

	private final String releaseBranch;

	@JsonCreator
	public VersionRequest(String releaseBranch) {
		this.releaseBranch = releaseBranch;
	}

	public String getReleaseBranch() {
		return releaseBranch;
	}

}
