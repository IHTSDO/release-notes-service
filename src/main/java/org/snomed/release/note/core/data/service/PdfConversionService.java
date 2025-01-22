package org.snomed.release.note.core.data.service;

import com.lowagie.text.DocumentException;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import com.google.common.base.Strings;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfConversionService {

	@Autowired
	LineItemService lineItemService;

	private static final Logger LOGGER = LoggerFactory.getLogger(PdfConversionService.class);

	public byte[] convertToPdf(String path) throws BusinessServiceException {
		LOGGER.info("Collecting the release notes on path {}", path);

		StringBuilder content = new StringBuilder();
		List<LineItem> lineItems = lineItemService.findOrderedLineItems(path);
		for (LineItem lineItem : lineItems) {
			lineItem.setChildren(lineItem.getChildren().stream().filter(LineItemService::hasContent).toList());
		}
		lineItems = lineItems.stream().filter(LineItemService::hasContent).toList();
		collectContent(content, lineItems, List.of(1));

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

	private void collectContent(StringBuilder contentTotal, List<LineItem> lineItems, List<Integer> indices) {
		List<Integer> copiedIndices = new ArrayList<>(indices);
		for (LineItem lineItem : lineItems) {
			contentTotal.append(formatTitle(lineItem, copiedIndices));
			contentTotal.append("\n\n");

			String content = lineItem.getContent();
			if (!Strings.isNullOrEmpty(content)) {
				contentTotal.append(content);
				contentTotal.append("\n\n");
			}
			List<Integer> childIndices = new ArrayList<>(copiedIndices);
			childIndices.add(1);
			collectContent(contentTotal, lineItem.getChildren(), childIndices);
			copiedIndices.set(copiedIndices.size() - 1, copiedIndices.get(copiedIndices.size() - 1) + 1);
		}
	}

	private String formatTitle(LineItem lineItem, List<Integer> indices) {
		char[] heading = new char[lineItem.getLevel()];
		Arrays.fill(heading, '#');

		return String.valueOf(heading) + " " + indices.stream().map(Object::toString).collect(Collectors.joining(".")) + ". " + lineItem.getTitle();
	}
}
