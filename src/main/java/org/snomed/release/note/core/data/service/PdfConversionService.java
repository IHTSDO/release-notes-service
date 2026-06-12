package org.snomed.release.note.core.data.service;

import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.core.data.domain.LineItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PdfConversionService {

	private final LineItemService lineItemService;

	private final ReleaseNotesRenderingService releaseNotesRenderingService;

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfConversionService.class);

	@Autowired
	public PdfConversionService(LineItemService lineItemService, ReleaseNotesRenderingService releaseNotesRenderingService) {
		this.lineItemService = lineItemService;
		this.releaseNotesRenderingService = releaseNotesRenderingService;
	}

	public byte[] convertToPdf(String path) throws BusinessServiceException {
		LOGGER.info("Collecting the release notes on path {}", path);

		List<LineItem> lineItems = lineItemService.findOrderedLineItems(path);
		lineItems = releaseNotesRenderingService.filterLineItemsWithContent(lineItems);
		String markdown = releaseNotesRenderingService.buildMarkdown(lineItems);

		LOGGER.info("Converting the release notes on path {} to PDF", path);

		byte[] pdf = releaseNotesRenderingService.markdownToPdf(markdown);

		LOGGER.info("Successfully converted the release notes on path {} to PDF", path);

		return pdf;
	}
}
