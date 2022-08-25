package org.snomed.release.note.rest.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;

public class CloneRequest {

	private final String destinationBranch;

	@JsonCreator
	public CloneRequest(String destinationBranch) {
		this.destinationBranch = destinationBranch;
	}

	public String getDestinationBranch() {
		return destinationBranch;
	}
}
