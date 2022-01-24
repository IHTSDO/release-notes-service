package org.snomed.release.note.core.util;

import org.junit.jupiter.api.Test;
import org.snomed.release.note.AbstractTest;

import static org.junit.jupiter.api.Assertions.*;

public class BranchUtilTest extends AbstractTest {

	@Test
	void testIsReleaseBranch() {
		assertEquals(false, BranchUtil.isReleaseBranch("MAIN/"));
		assertEquals(false, BranchUtil.isReleaseBranch("MAIN/ProjectA"));
		assertEquals(false, BranchUtil.isReleaseBranch("MAIN/ProjectA/Task1/"));
		assertEquals(true, BranchUtil.isReleaseBranch("MAIN/2020-01-01"));
		assertEquals(false, BranchUtil.isReleaseBranch("MAIN/20200101"));
		assertEquals(false, BranchUtil.isReleaseBranch("MAIN/ProjectA/2020-01-01"));
		assertEquals(false, BranchUtil.isReleaseBranch("MAIN/SNOMEDCT-US"));
		assertEquals(true, BranchUtil.isReleaseBranch("MAIN/SNOMEDCT-US/2020-01-01"));
		assertEquals(false, BranchUtil.isReleaseBranch("MAIN/SNOMEDCT-US/20200101"));
		assertEquals(false, BranchUtil.isReleaseBranch("MAIN/SNOMEDCT-US/ProjectA/Task1/2020-01-01"));
	}

	@Test
	void testIsCodeSystemBranch() {
		assertEquals(true, BranchUtil.isCodeSystemBranch("MAIN/"));
		assertEquals(false, BranchUtil.isCodeSystemBranch("MAIN/ProjectA"));
		assertEquals(false, BranchUtil.isCodeSystemBranch("MAIN/ProjectA/Task1/"));
		assertEquals(false, BranchUtil.isCodeSystemBranch("MAIN/2020-01-01"));
		assertEquals(false, BranchUtil.isCodeSystemBranch("MAIN/20200101"));
		assertEquals(false, BranchUtil.isCodeSystemBranch("MAIN/ProjectA/2020-01-01"));
		assertEquals(true, BranchUtil.isCodeSystemBranch("MAIN/SNOMEDCT-US"));
		assertEquals(false, BranchUtil.isCodeSystemBranch("MAIN/SNOMEDCT-US/2020-01-01"));
		assertEquals(false, BranchUtil.isCodeSystemBranch("MAIN/SNOMEDCT-US/20200101"));
		assertEquals(false, BranchUtil.isCodeSystemBranch("MAIN/SNOMEDCT-US/ProjectA/Task1/2020-01-01"));
	}

	@Test
	void testExtractCodeSystem() {
		assertEquals("SNOMEDCT", BranchUtil.extractCodeSystem("MAIN/"));
		assertEquals("SNOMEDCT", BranchUtil.extractCodeSystem("MAIN/ProjectA"));
		assertEquals("SNOMEDCT", BranchUtil.extractCodeSystem("MAIN/ProjectA/Task1/"));
		assertEquals("SNOMEDCT", BranchUtil.extractCodeSystem("MAIN/2020-01-01"));
		assertEquals("SNOMEDCT", BranchUtil.extractCodeSystem("MAIN/20200101"));
		assertEquals("SNOMEDCT", BranchUtil.extractCodeSystem("MAIN/ProjectA/2020-01-01"));
		assertEquals("SNOMEDCT-US", BranchUtil.extractCodeSystem("MAIN/SNOMEDCT-US"));
		assertEquals("SNOMEDCT-US", BranchUtil.extractCodeSystem("MAIN/SNOMEDCT-US/2020-01-01"));
		assertEquals("SNOMEDCT-US", BranchUtil.extractCodeSystem("MAIN/SNOMEDCT-US/20200101"));
		assertEquals("SNOMEDCT-US", BranchUtil.extractCodeSystem("MAIN/SNOMEDCT-US/ProjectA/Task1/2020-01-01"));
	}

	@Test
	void testGetParentBranch() {
		assertEquals(null, BranchUtil.getParentBranch("MAIN/"));
		assertEquals("MAIN", BranchUtil.getParentBranch("MAIN/ProjectA"));
		assertEquals("MAIN/ProjectA", BranchUtil.getParentBranch("MAIN/ProjectA/Task1/"));
		assertEquals("MAIN", BranchUtil.getParentBranch("MAIN/2020-01-01"));
		assertEquals("MAIN", BranchUtil.getParentBranch("MAIN/20200101"));
		assertEquals("MAIN/ProjectA", BranchUtil.getParentBranch("MAIN/ProjectA/2020-01-01"));
		assertEquals("MAIN", BranchUtil.getParentBranch("MAIN/SNOMEDCT-US"));
		assertEquals("MAIN/SNOMEDCT-US", BranchUtil.getParentBranch("MAIN/SNOMEDCT-US/2020-01-01"));
		assertEquals("MAIN/SNOMEDCT-US", BranchUtil.getParentBranch("MAIN/SNOMEDCT-US/20200101"));
		assertEquals("MAIN/SNOMEDCT-US/ProjectA/Task1", BranchUtil.getParentBranch("MAIN/SNOMEDCT-US/ProjectA/Task1/2020-01-01"));
	}

}
