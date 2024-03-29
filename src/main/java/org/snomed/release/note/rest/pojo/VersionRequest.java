package org.snomed.release.note.rest.pojo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

public record VersionRequest(
		@JsonFormat(pattern = "yyyy-MM-dd") @Schema(type = "string", format = "date", example = "2023-01-31") Date effectiveTime) {
	@JsonCreator
	public VersionRequest(Date effectiveTime) {
		this.effectiveTime = effectiveTime;
	}

	@Override
	public Date effectiveTime() {
		return effectiveTime;
	}

}
