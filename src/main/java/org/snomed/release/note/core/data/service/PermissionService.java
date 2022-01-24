package org.snomed.release.note.core.data.service;

import org.ihtsdo.otf.rest.client.RestClientException;
import org.ihtsdo.otf.rest.client.terminologyserver.SnowstormRestClientFactory;
import org.ihtsdo.otf.rest.client.terminologyserver.pojo.Branch;
import org.ihtsdo.sso.integration.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

	private final SnowstormRestClientFactory snowstormRestClientFactory;

	private static final Logger LOGGER = LoggerFactory.getLogger(PermissionService.class);

	@Value("${ims-security.roles.enabled}")
	private boolean rolesEnabled;

	public PermissionService(@Value("${snowstorm.url}") String snowstormUrl) {
		snowstormRestClientFactory = new SnowstormRestClientFactory(snowstormUrl, null);
	}

	public boolean currentUserHasRoleOnBranch(String role, String branchPath) {
		if (!rolesEnabled) {
			return true;
		}
		try {
			boolean contains = getBranchOrThrow(branchPath).getUserRoles().contains(role);
			if (!contains) {
				LOGGER.info("User '{}' does not have required role '{}' on branch '{}'", SecurityUtil.getUsername(), role, branchPath);
			}
			return contains;
		} catch (RestClientException e) {
			LOGGER.error("Could not ascertain user roles: Failed to communicate with Snowstorm.", e);
			throw new AccessDeniedException("Could not ascertain user roles: Failed to communicate with Snowstorm.", e);
		}
	}

	private Branch getBranchOrThrow(String branchPath) throws RestClientException {
		final Branch branch = snowstormRestClientFactory.getClient().getBranch(branchPath);
		if (branch == null) {
			LOGGER.error("Branch {} does not exist", branchPath);
			throw new AccessDeniedException("Branch '" + branchPath + "' does not exist.");
		}
		return branch;
	}

}
