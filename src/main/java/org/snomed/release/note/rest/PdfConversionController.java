package org.snomed.release.note.rest;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.snomed.release.note.core.data.service.PdfConversionService;
import org.snomed.release.note.core.util.BranchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RestController
@Api(tags = "PDF Conversion")
public class PdfConversionController {

	@Autowired
	private PdfConversionService pdfConversionService;

	@ApiOperation("Release notes in PDF format.")
	@GetMapping(value = "/{path}/lineitems/pdf")
	public ResponseEntity<byte[]> convertToPdf(
			@PathVariable String path) throws BusinessServiceException {
		String branch = BranchPathUriUtil.decodePath(path);

		byte[] contents = pdfConversionService.convertToPdf(branch);

		String filename = getFilename(BranchUtil.extractCodeSystem(branch), BranchUtil.extractVersionDate(branch));

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_PDF);
		headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=" + filename);

		return new ResponseEntity<>(contents, headers, HttpStatus.OK);
	}

	private String getFilename(String codeSystem, LocalDate versionDate) {
		// Examples:
		// SNOMED CT March 2022 International Edition - SNOMED International Release notes
		// SNOMED CT Managed Service - US Edition Release Notes - March 2022
		// SNOMED CT Managed Service - Denmark Extension Release Notes - March 2022
		// SNOMED CT Managed Service - Belgium Extension Release Notes - March 2022

		// Following the International Edition format here
		String edition = codeSystem.equals("SNOMEDCT") ? "International" : codeSystem.substring(codeSystem.indexOf("-") + 1);
		String version = versionDate == null ? "" : versionDate.format(DateTimeFormatter.ofPattern("MMMM uuuu"));

		return "SNOMED CT " + version + " " + edition + " Edition - SNOMED International Release Notes.pdf";
	}
}
