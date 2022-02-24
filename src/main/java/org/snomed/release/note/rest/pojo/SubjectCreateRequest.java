package org.snomed.release.note.rest.request;

public class SubjectCreateRequest {

	private String title;

	public SubjectCreateRequest() {
	}

	public SubjectCreateRequest(String title) {
		super();
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
}
