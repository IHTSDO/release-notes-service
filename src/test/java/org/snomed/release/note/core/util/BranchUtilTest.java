package org.snomed.release.note.core.util;

import org.junit.jupiter.api.Test;
import org.snomed.release.note.AbstractTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class BranchUtilTest extends AbstractTest {

	@Test
	void testIsReleaseBranch() {
		assertFalse(BranchUtil.isReleaseBranch("MAIN/"));
		assertFalse(BranchUtil.isReleaseBranch("MAIN/ProjectA"));
		assertFalse(BranchUtil.isReleaseBranch("MAIN/ProjectA/Task1/"));
		assertTrue(BranchUtil.isReleaseBranch("MAIN/2020-01-01"));
		assertFalse(BranchUtil.isReleaseBranch("MAIN/20200101"));
		assertFalse(BranchUtil.isReleaseBranch("MAIN/ProjectA/2020-01-01"));
		assertFalse(BranchUtil.isReleaseBranch("MAIN/SNOMEDCT-US"));
		assertTrue(BranchUtil.isReleaseBranch("MAIN/SNOMEDCT-US/2020-01-01"));
		assertFalse(BranchUtil.isReleaseBranch("MAIN/SNOMEDCT-US/20200101"));
		assertFalse(BranchUtil.isReleaseBranch("MAIN/SNOMEDCT-US/ProjectA/Task1/2020-01-01"));
	}

	@Test
	void testIsCodeSystemBranch() {
		assertTrue(BranchUtil.isCodeSystemBranch("MAIN/"));
		assertFalse(BranchUtil.isCodeSystemBranch("MAIN/ProjectA"));
		assertFalse(BranchUtil.isCodeSystemBranch("MAIN/ProjectA/Task1/"));
		assertFalse(BranchUtil.isCodeSystemBranch("MAIN/2020-01-01"));
		assertFalse(BranchUtil.isCodeSystemBranch("MAIN/20200101"));
		assertFalse(BranchUtil.isCodeSystemBranch("MAIN/ProjectA/2020-01-01"));
		assertTrue(BranchUtil.isCodeSystemBranch("MAIN/SNOMEDCT-US"));
		assertFalse(BranchUtil.isCodeSystemBranch("MAIN/SNOMEDCT-US/2020-01-01"));
		assertFalse(BranchUtil.isCodeSystemBranch("MAIN/SNOMEDCT-US/20200101"));
		assertFalse(BranchUtil.isCodeSystemBranch("MAIN/SNOMEDCT-US/ProjectA/Task1/2020-01-01"));
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
	void testExtractVersionDate() {
		assertNull(BranchUtil.extractVersionDate("MAIN/"));
		assertNull(BranchUtil.extractVersionDate("MAIN/ProjectA"));
		assertNull(BranchUtil.extractVersionDate("MAIN/ProjectA/Task1/"));
		assertEquals(LocalDate.of(2020, 1, 1), BranchUtil.extractVersionDate("MAIN/2020-01-01"));
		assertNull(BranchUtil.extractVersionDate("MAIN/20200101"));
		assertNull(BranchUtil.extractVersionDate("MAIN/ProjectA/2020-01-01"));
		assertNull(BranchUtil.extractVersionDate("MAIN/SNOMEDCT-US"));
		assertEquals(LocalDate.of(2020, 1, 1), BranchUtil.extractVersionDate("MAIN/SNOMEDCT-US/2020-01-01"));
		assertNull(BranchUtil.extractVersionDate("MAIN/SNOMEDCT-US/20200101"));
		assertNull(BranchUtil.extractVersionDate("MAIN/SNOMEDCT-US/ProjectA/Task1/2020-01-01"));
	}

	@Test
	void testGetParentBranch() {
		assertNull(BranchUtil.getParentBranch("MAIN/"));
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
