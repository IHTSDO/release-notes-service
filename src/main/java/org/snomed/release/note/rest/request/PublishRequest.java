package org.snomed.release.note.rest.request;

public class PublishRequest {

	private String version;

	public PublishRequest(String version) {
		this.version = version;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
}
