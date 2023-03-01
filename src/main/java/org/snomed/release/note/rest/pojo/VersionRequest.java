package org.snomed.release.note.rest.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;

public class VersionRequest {

	@JsonFormat(pattern = "yyyy-MM-dd")
	@ApiModelProperty(example = "2023-01-01")
	private final Date effectiveTime;

	@JsonCreator
	public VersionRequest(Date effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	public Date getEffectiveTime() {
		return effectiveTime;
	}

}
