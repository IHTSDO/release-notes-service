package org.snomed.release.note.core.util;

import org.elasticsearch.common.Strings;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ContentUtil {

	public static final String LINE_BREAK = System.lineSeparator() + System.lineSeparator();
	public static final String SPACE_CHAR = " ";

	public static String merge(String ... lines) {
		return Arrays.stream(lines)
				.filter(line -> !Strings.isNullOrEmpty(line))
				.collect(Collectors.joining(LINE_BREAK + SPACE_CHAR + LINE_BREAK));
	}
}
