package org.snomed.release.note.core.data.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.util.Date;
import java.util.Objects;

@Document(indexName = "#{@indexNameProvider.getIndexNameWithPrefix('subject')}")
@Setting(settingPath = "elasticsearch-settings.json")
public class Subject {

	@Id
	@Field(type = FieldType.Keyword)
	private String id;

	@Field(type = FieldType.Keyword)
	private String title;

	@Field(type = FieldType.Keyword)
	private String path;

	@Field(type = FieldType.Date)
	private Date created;

	@Field(type = FieldType.Date)
	private Date lastModified;

	public Subject() {
	}

	public Subject(String title, String path) {
		super();
		this.title = title;
		this.path = path;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Subject subject = (Subject) o;

		if (id != null || subject.id != null) {
			return Objects.equals(id, subject.id);
		}

		return Objects.equals(title, subject.title) && Objects.equals(path, subject.path);
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public String toString() {
		return "Subject{" +
				"id='" + id + '\'' +
				", title='" + title + '\'' +
				", path='" + path + '\'' +
				", created=" + created +
				", lastModified=" + lastModified +
				'}';
	}
}
