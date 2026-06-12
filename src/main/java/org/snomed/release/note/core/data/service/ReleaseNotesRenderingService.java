package org.snomed.release.note.core.data.service;

import com.google.common.base.Strings;
import com.lowagie.text.DocumentException;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.util.BranchUtil;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReleaseNotesRenderingService {

	public static final String STYLE_ATTRIBUTE = "style";

	private static final Logger LOGGER = LoggerFactory.getLogger(ReleaseNotesRenderingService.class);

	/**
	 * NBSP repeats so Markdown→HTML rendering does not collapse tab stops (regular spaces collapse in HTML).
	 */
	private static final String VISIBLE_TAB = "\u00a0".repeat(4);

	public List<LineItem> filterLineItemsWithContent(List<LineItem> lineItems) {
		for (LineItem lineItem : lineItems) {
			lineItem.setChildren(lineItem.getChildren().stream().filter(LineItemService::hasContent).toList());
		}
		return lineItems.stream().filter(LineItemService::hasContent).toList();
	}

	public String buildMarkdown(List<LineItem> lineItems) {
		StringBuilder content = new StringBuilder();
		collectContent(content, lineItems, List.of(1));
		return content.toString();
	}

	public String markdownToHtmlBody(String markdown) {
		Parser parser = Parser.builder().build();
		Node node = parser.parse(markdown);
		HtmlRenderer htmlRenderer = HtmlRenderer.builder().build();
		return htmlRenderer.render(node);
	}

	public String buildHtmlDocument(String pageTitle, String markdown) {
		String bodyHtml = markdownToHtmlBody(markdown);

		Document document = Jsoup.parse(bodyHtml);
		document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
		document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
		addLogoToDocument(document);

		String escapedTitle = Jsoup.parse(pageTitle).text();

		return """
				<!DOCTYPE html>
				<html lang="en">
				<head>
				  <meta charset="UTF-8"/>
				  <meta name="viewport" content="width=device-width, initial-scale=1"/>
				  <title>%s</title>
				  <style>
				    body { font-family: Arial, Helvetica, sans-serif; line-height: 1.6; color: #1a1a1a; max-width: 900px; margin: 0 auto; padding: 2rem 1.5rem 3rem; position: relative; }
				    h1, h2, h3, h4, h5, h6 { color: #003366; margin-top: 1.5rem; }
				    h1 { font-size: 1.75rem; border-bottom: 2px solid #003366; padding-bottom: 0.5rem; }
				    h2 { font-size: 1.35rem; }
				    a { color: #0066cc; }
				    ul, ol { padding-left: 1.5rem; }
				    table { border-collapse: collapse; width: 100%%; margin: 1rem 0; }
				    th, td { border: 1px solid #ccc; padding: 0.5rem; text-align: left; }
				    th { background: #f5f5f5; }
				    .page-header { margin-bottom: 2rem; }
				    .page-header h1 { border: none; margin: 0; }
				    .page-header p { color: #555; margin: 0.25rem 0 0; }
				  </style>
				</head>
				<body>
				  <header class="page-header">
				    <h1>%s</h1>
				  </header>
				  <main>
				    %s
				  </main>
				</body>
				</html>
				""".formatted(escapedTitle, escapedTitle, document.body().html());
	}

	public byte[] markdownToPdf(String markdown) throws BusinessServiceException {
		String html = markdownToHtmlBody(markdown);

		Document document = Jsoup.parse(html);
		document.outputSettings().syntax(Document.OutputSettings.Syntax.xml);
		document.outputSettings().escapeMode(Entities.EscapeMode.xhtml);
		addLogoToDocument(document);

		try {
			ITextRenderer renderer = new ITextRenderer();
			renderer.setDocumentFromString(document.html());
			renderer.layout();
			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
			renderer.createPDF(outputStream);
			return outputStream.toByteArray();
		} catch (DocumentException e) {
			throw new BusinessServiceException("Failed to convert release notes to PDF", e);
		}
	}

	public String buildPageTitle(String branchPath) {
		String codeSystem = BranchUtil.extractCodeSystem(branchPath);
		LocalDate versionDate = BranchUtil.extractVersionDate(branchPath);
		String edition = codeSystem.equals("SNOMEDCT") ? "International" : codeSystem.substring(codeSystem.indexOf("-") + 1);
		String version = versionDate == null ? "" : versionDate.format(DateTimeFormatter.ofPattern("MMMM uuuu"));
		return "SNOMED CT " + version + " " + edition + " Edition - Release Notes";
	}

	private void collectContent(StringBuilder contentTotal, List<LineItem> lineItems, List<Integer> indices) {
		List<Integer> copiedIndices = new ArrayList<>(indices);
		for (LineItem lineItem : lineItems) {
			contentTotal.append(formatTitle(lineItem, copiedIndices));
			contentTotal.append("\n\n");

			String content = lineItem.getContent();
			if (!Strings.isNullOrEmpty(content)) {
				contentTotal.append(normalizeLineItemBodyForMarkdown(content));
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

		return String.valueOf(heading) + " " + indices.stream().map(Object::toString).collect(Collectors.joining(".")) + ". " + expandTabStops(lineItem.getTitle());
	}

	private static String expandTabStops(String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&#9;", VISIBLE_TAB)
				.replace("&#09;", VISIBLE_TAB)
				.replace("&#x9;", VISIBLE_TAB)
				.replace("&#x09;", VISIBLE_TAB)
				.replace("&#X9;", VISIBLE_TAB)
				.replace("&#X09;", VISIBLE_TAB)
				.replace("\\u0009", VISIBLE_TAB)
				.replace("\\t", VISIBLE_TAB)
				.replace("\t", VISIBLE_TAB);
	}

	private static String normalizeLineItemBodyForMarkdown(String raw) {
		return expandTabStops(raw).replace(" \n\n ", "<br>");
	}

	private void addLogoToDocument(Document document) {
		try {
			InputStream logoStream = getClass().getClassLoader().getResourceAsStream("logo.png");
			if (logoStream == null) {
				LOGGER.warn("Logo file logo.png not found in resources");
				return;
			}

			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] data = new byte[1024];
			int nRead;
			while ((nRead = logoStream.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			byte[] imageBytes = buffer.toByteArray();

			String base64Image = Base64.getEncoder().encodeToString(imageBytes);

			org.jsoup.nodes.Element body = document.body();
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
