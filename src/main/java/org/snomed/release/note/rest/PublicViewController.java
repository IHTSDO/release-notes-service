package org.snomed.release.note.rest;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.ihtsdo.otf.rest.exception.BadRequestException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.ihtsdo.otf.rest.exception.ResourceNotFoundException;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.service.LineItemService;
import org.snomed.release.note.core.data.service.ReleaseNotesRenderingService;
import org.snomed.release.note.core.util.BranchUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Public View")
public class PublicViewController {

	private final LineItemService lineItemService;

	private final ReleaseNotesRenderingService releaseNotesRenderingService;

	@Autowired
	public PublicViewController(LineItemService lineItemService, ReleaseNotesRenderingService releaseNotesRenderingService) {
		this.lineItemService = lineItemService;
		this.releaseNotesRenderingService = releaseNotesRenderingService;
	}

	@Operation(summary = "Browsable HTML view of published release notes (no authentication required).")
	@GetMapping(value = "/{path}/lineitems/view", produces = MediaType.TEXT_HTML_VALUE)
	public ResponseEntity<String> viewPublishedReleaseNotes(@PathVariable String path) throws BusinessServiceException {
		String branch = BranchPathUriUtil.decodePath(path);

		if (!BranchUtil.isReleaseBranch(branch)) {
			throw new BadRequestException("Branch '" + branch + "' must be a release branch");
		}

		List<LineItem> published = lineItemService.findOrderedLineItems(branch);
		if (published.isEmpty()) {
			throw new ResourceNotFoundException("No published release notes found on branch '" + branch + "'");
		}

		List<LineItem> lineItems = releaseNotesRenderingService.filterLineItemsWithContent(published);
		String markdown = releaseNotesRenderingService.buildMarkdown(lineItems);
		String pageTitle = releaseNotesRenderingService.buildPageTitle(branch);
		String html = releaseNotesRenderingService.buildHtmlDocument(pageTitle, markdown);

		return ResponseEntity.ok()
				.contentType(MediaType.TEXT_HTML)
				.body(html);
	}
}
