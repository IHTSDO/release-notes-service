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
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfConversionService {

	public static final String STYLE_ATTRIBUTE = "style";

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

		// Add logo to the document
		addLogoToDocument(document);

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

	private void addLogoToDocument(Document document) {
		try {
			// Load logo from resources
			InputStream logoStream = getClass().getClassLoader().getResourceAsStream("logo.png");
			if (logoStream == null) {
				LOGGER.warn("Logo file snomed-logo.png not found in resources");
				return;
			}

			// Read image bytes
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] data = new byte[1024];
			int nRead;
			while ((nRead = logoStream.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			byte[] imageBytes = buffer.toByteArray();

			// Convert to base64
			String base64Image = Base64.getEncoder().encodeToString(imageBytes);

			// Create img element with base64 data URI
			org.jsoup.nodes.Element body = document.body();
            // Set body to relative positioning for absolute positioning of logo
            String bodyStyle = body.attr(STYLE_ATTRIBUTE);
            if (bodyStyle.isEmpty()) {
                body.attr(STYLE_ATTRIBUTE, "position: relative;");
            } else {
                body.attr(STYLE_ATTRIBUTE, bodyStyle + " position: relative;");
            }

            org.jsoup.nodes.Element logoDiv = new org.jsoup.nodes.Element("div");

            org.jsoup.nodes.Element img = new org.jsoup.nodes.Element("img");
            img.attr("src", "data:image/png;base64," + base64Image);
            img.attr(STYLE_ATTRIBUTE, "max-width: 150px; height: auto;");

            logoDiv.appendChild(img);
            body.prependChild(logoDiv);

            logoStream.close();
		} catch (IOException e) {
			LOGGER.error("Failed to load logo image", e);
		}
	}
}
