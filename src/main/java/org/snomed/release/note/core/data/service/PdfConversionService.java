package org.snomed.release.note.core.data.service;

import com.qkyrie.markdown2pdf.internal.converting.Html2PdfConverter;
import com.qkyrie.markdown2pdf.internal.converting.HtmlCleaner;
import com.qkyrie.markdown2pdf.internal.converting.Markdown2HtmlConverter;
import com.qkyrie.markdown2pdf.internal.exceptions.ConversionException;
import org.elasticsearch.common.Strings;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.core.data.domain.LineItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PdfConversionService {

	@Autowired
	LineItemService lineItemService;

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfConversionService.class);

	public byte[] convertToPdf(String path) throws BusinessServiceException {
		List<LineItem> lineItems = lineItemService.findOrderedLineItems(path);

		StringBuilder content = new StringBuilder();
		collectContent(content, lineItems);

		String originalMarkdownFile = content.toString();

		try {
			LOGGER.info("Converting the release notes on path {} to PDF", path);

			String htmlFile = new Markdown2HtmlConverter().convert(originalMarkdownFile);
			String cleanedHtmlFile = new HtmlCleaner().clean(htmlFile);
			byte[] convertedPdfFile = new Html2PdfConverter().convert(cleanedHtmlFile);

			LOGGER.info("Successfully converted the release notes on path {} to PDF", path);

			return convertedPdfFile;
		} catch (ConversionException e) {
			LOGGER.error("Failed to convert the release notes on path {} to PDF", path, e);
			throw new BusinessServiceException("Failed to convert the release notes on path " + path + " to PDF", e);
		}
	}

	private void collectContent(StringBuilder contentTotal, List<LineItem> lineItems) {
		lineItems.forEach(lineItem -> {
			contentTotal.append(formatTitle(lineItem));
			contentTotal.append("\n\n");

			String content = lineItem.getContent();
			if (!Strings.isNullOrEmpty(content)) {
				contentTotal.append(content);
				contentTotal.append("\n\n");
			}
			collectContent(contentTotal, lineItem.getChildren());
		});
	}

	private String formatTitle(LineItem lineItem) {
		char[] heading = new char[lineItem.getLevel()];
		Arrays.fill(heading, '#');

		return String.valueOf(heading) + " " + lineItem.getTitle();
	}
}
