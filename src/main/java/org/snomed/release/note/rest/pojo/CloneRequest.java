package org.snomed.release.note.rest.pojo;

public class CloneRequest {

	private final String destinationBranch;

	public CloneRequest(String destinationBranch) {
		this.destinationBranch = destinationBranch;
	}

	public String getDestinationBranch() {
		return destinationBranch;
	}
}
