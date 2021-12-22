package org.snomed.release.note.core.data.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;

@Document(indexName = "lineitem")
public class LineItem {

    @Id
    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Keyword)
    private String subjectId;

    @Field(type = FieldType.Long)
    private Long parentId;

    @Field(type = FieldType.Integer)
    private Integer level;

    @Field(type = FieldType.Text)
    private String content;

    @Field(type = FieldType.Integer)
    private Integer sequence;

    @Field(type = FieldType.Keyword)
    private String sourceBranch;

    @Field(type = FieldType.Keyword)
    private String promotedBranch;

    @Field(type = FieldType.Date, format = DateFormat.year_month_day)
    private LocalDate startDate;

    @Field(type = FieldType.Date, format = DateFormat.year_month_day)
    private LocalDate endDate;

    @Field(type = FieldType.Boolean)
    private Boolean released;

    public LineItem() {
    }

    public LineItem(Long id, String subjectId, Long parentId, Integer level, String content, Integer sequence, String sourceBranch, String promotedBranch, LocalDate startDate, LocalDate endDate, Boolean released) {
        this();
        this.id = id;
        this.subjectId = subjectId;
        this.parentId = parentId;
        this.level = level;
        this.content = content;
        this.sequence = sequence;
        this.sourceBranch = sourceBranch;
        this.promotedBranch = promotedBranch;
        this.startDate = startDate;
        this.endDate = endDate;
        this.released = released;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(String subjectId) {
        this.subjectId = subjectId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getSourceBranch() {
        return sourceBranch;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public String getPromotedBranch() {
        return promotedBranch;
    }

    public void setPromotedBranch(String promotedBranch) {
        this.promotedBranch = promotedBranch;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Boolean getReleased() {
        return released;
    }

    public void setReleased(Boolean released) {
        this.released = released;
    }
}
