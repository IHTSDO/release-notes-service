package org.snomed.release.note.core.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class BranchUtil {

	public static final String SEPARATOR = "/";

	private BranchUtil() {
	}

	public static String extractCodeSystem(String branch) {
		if (branch.contains("SNOMEDCT-")) {
			return branch.substring(branch.lastIndexOf("SNOMEDCT-")).replaceAll("/.*", "");
		} else {
			return "SNOMEDCT";
		}
	}

	public static boolean isCodeSystemBranch(String branch) {
		if (branch.endsWith(SEPARATOR)) {
			branch = branch.substring(0, branch.length() - 1);
		}
		return branch.equals("MAIN") || branch.startsWith("SNOMEDCT-", branch.lastIndexOf(SEPARATOR) + 1);
	}

	public static boolean isReleaseBranch(String branch) {
		if (branch.endsWith(SEPARATOR)) {
			branch = branch.substring(0, branch.length() - 1);
		}
		String parentBranch = getParentBranch(branch);
		if (parentBranch != null) {
			String version = branch.substring(branch.lastIndexOf(SEPARATOR) + 1);
			try {
				LocalDate.parse(version, DateTimeFormatter.ISO_LOCAL_DATE);
				return isCodeSystemBranch(parentBranch);
			} catch (DateTimeParseException e) {
			}
		}
		return false;
	}

	public static String getParentBranch(String branch) {
		if (branch.endsWith(SEPARATOR)) {
			branch = branch.substring(0, branch.length() - 1);
		}
		int lastIndex = branch.lastIndexOf(SEPARATOR);
		return lastIndex == -1 ? null : branch.substring(0, lastIndex);
	}
}
