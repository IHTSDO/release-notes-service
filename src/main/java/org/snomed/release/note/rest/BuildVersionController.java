package org.snomed.release.note.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.snomed.release.note.rest.pojo.BuildVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "Version", description = "Build Version")
public class BuildVersionController {

	@Autowired
	BuildProperties buildProperties;

	@ApiOperation("Software build version and build timestamp.")
	@RequestMapping(value = "/version", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public BuildVersion getBuildInformation() {
		return new BuildVersion(buildProperties.getVersion(), buildProperties.getTime().toString());
	}


}