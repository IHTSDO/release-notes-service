package org.snomed.release.note.core.data.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

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
    private LocalDate lastModified;

    public Subject() {
    }

    public Subject(String id, String title, String path, LocalDate createdDate, LocalDate lastModified) {
        this();
        this.id = id;
        this.title = title;
        this.path = path;
        this.createdDate = createdDate;
        this.lastModified = lastModified;
    }

    public String getId() {
        return id;
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

    public LocalDate getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDate lastModified) {
        this.lastModified = lastModified;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public void setId(String id) {
        this.id = id;
    }
}
