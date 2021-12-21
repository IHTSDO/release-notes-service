package org.snomed.release.note.core.data.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Date;

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

    @Field(type = FieldType.Date)
    private Date startDate;

    @Field(type = FieldType.Date)
    private Date endDate;

    @Field(type = FieldType.Boolean)
    private Boolean released;

    public LineItem() {
    }

    public LineItem(Long id, String subjectId, Long parentId, Integer level, String content, Integer sequence, String sourceBranch, String promotedBranch, Date startDate, Date endDate, Boolean released) {
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
}
