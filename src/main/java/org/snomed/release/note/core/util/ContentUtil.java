package org.snomed.release.note.core.util;

import com.google.common.base.Strings;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ContentUtil {

	public static final String LINE_BREAK = System.lineSeparator() + System.lineSeparator();
	public static final String ZERO_WIDTH_SPACE = "&#8203;";

	public static String merge(String ... lines) {
		return Arrays.stream(lines)
				.filter(line -> !Strings.isNullOrEmpty(line))
				.collect(Collectors.joining(LINE_BREAK + ZERO_WIDTH_SPACE + LINE_BREAK));
	}
}
