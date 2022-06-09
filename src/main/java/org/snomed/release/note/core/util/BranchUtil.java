package org.snomed.release.note.core.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BranchUtil {

	public static final String SEPARATOR = "/";

	private BranchUtil() {
	}

	public static String extractCodeSystem(String path) {
		if (path.contains("SNOMEDCT-")) {
			return path.substring(path.lastIndexOf("SNOMEDCT-")).replaceAll("/.*", "");
		} else {
			return "SNOMEDCT";
		}
	}

	public static LocalDate extractVersionDate(String path) {
		String branchPath = removeTrailingSeparator(path);
		if (!isReleaseBranch(branchPath)) {
			return null;
		}
		String version = branchPath.substring(branchPath.lastIndexOf(SEPARATOR) + 1);
		return LocalDate.parse(version, DateTimeFormatter.ISO_LOCAL_DATE);
	}

	public static boolean isCodeSystemBranch(String path) {
		String branchPath = removeTrailingSeparator(path);
		return branchPath.equals("MAIN") || branchPath.startsWith("SNOMEDCT-", branchPath.lastIndexOf(SEPARATOR) + 1);
	}

	public static boolean isReleaseBranch(String path) {
		String branchPath = removeTrailingSeparator(path);
		String parentBranchPath = getParentBranch(branchPath);
		if (parentBranchPath == null) {
			return false;
		}
		String version = branchPath.substring(branchPath.lastIndexOf(SEPARATOR) + 1);
		try {
			LocalDate.parse(version, DateTimeFormatter.ISO_LOCAL_DATE);
			return isCodeSystemBranch(parentBranchPath);
		} catch (DateTimeParseException e) {
			return false;
		}
	}

	public static String getParentBranch(String path) {
		String branchPath = removeTrailingSeparator(path);
		int lastIndex = branchPath.lastIndexOf(SEPARATOR);
		return lastIndex == -1 ? null : branchPath.substring(0, lastIndex);
	}

	private static String removeTrailingSeparator(String path) {
		return path.endsWith(SEPARATOR) ? path.substring(0, path.length() - 1) : path;
	}
}
