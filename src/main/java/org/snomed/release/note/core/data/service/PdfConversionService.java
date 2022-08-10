package org.snomed.release.note.core.data.service;

import com.lowagie.text.DocumentException;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.elasticsearch.common.Strings;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.core.data.domain.LineItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.*;
import java.util.Arrays;
import java.util.List;

@Service
public class PdfConversionService {

	@Autowired
	LineItemService lineItemService;

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfConversionService.class);

	public byte[] convertToPdf(String path) throws BusinessServiceException {
		LOGGER.info("Collecting the release notes on path {}", path);

		StringBuilder content = new StringBuilder();
		collectContent(content, lineItemService.findOrderedLineItems(path));

		LOGGER.info("Converting the release notes on path {} to PDF", path);

		Parser parser = Parser.builder().build();
		Node node = parser.parse(content.toString());
		HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();
		String html = htmlRenderer.render(node);

		Document document = Jsoup.parse(html);
		document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
		document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);

		try {
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocumentFromString(document.html());
			renderer.layout();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			renderer.createPDF(outputStream);
			byte[] pdf = outputStream.toByteArray();

			LOGGER.info("Successfully converted the release notes on path {} to PDF", path);

			return pdf;
		} catch (DocumentException e) {
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
