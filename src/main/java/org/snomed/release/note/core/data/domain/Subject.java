package org.snomed.release.note.core.data.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Document(indexName = "subject")
public class Subject {

	@Id
	@Field(type = FieldType.Keyword)
	private String id;

	@Field(type = FieldType.Keyword)
	private String title;

	@Field(type = FieldType.Keyword)
	private String path;

	@Field(type = FieldType.Date, format = DateFormat.year_month_day)
	private LocalDate createdDate;

	@Field(type = FieldType.Date, format = DateFormat.year_month_day)
	private LocalDate lastModifiedDate;

	public Subject() {
	}

	public Subject(String id, String title, String path, LocalDate createdDate, LocalDate lastModifiedDate) {
		this();
		this.id = id;
		this.title = title;
		this.path = path;
		this.createdDate = createdDate;
		this.lastModifiedDate = lastModifiedDate;
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

	public LocalDate getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(LocalDate createdDate) {
		this.createdDate = createdDate;
	}

	public LocalDate getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(LocalDate lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
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
		return Objects.hash(id, title, path);
	}

	@Override
	public String toString() {
		return "Subject{" +
				"id='" + id + '\'' +
				", title='" + title + '\'' +
				", path=" + path +
				", createdDate='" + createdDate.format(DateTimeFormatter.BASIC_ISO_DATE) + '\'' +
				", lastModifiedDate='" + lastModifiedDate.format(DateTimeFormatter.BASIC_ISO_DATE) + '\'' +
				'}';
	}

}
