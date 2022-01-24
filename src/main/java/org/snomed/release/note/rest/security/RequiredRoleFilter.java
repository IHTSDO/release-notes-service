package org.snomed.release.note.rest.security;

import org.elasticsearch.common.Strings;
import org.ihtsdo.sso.integration.SecurityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class RequiredRoleFilter extends OncePerRequestFilter {

	private final String requiredRole;
	private final Set<String> excludedUrlPatterns = new HashSet<>();

	private final static Logger LOGGER = LoggerFactory.getLogger(RequiredRoleFilter.class);

	public RequiredRoleFilter(String requiredRole) {
		this.requiredRole = requiredRole;
	}

	public RequiredRoleFilter addExcludedUrlPatterns(String... excludedUrlPatterns) {
		Assert.notNull(excludedUrlPatterns, "excludedUrlPatterns must not be null");
		Collections.addAll(this.excludedUrlPatterns, excludedUrlPatterns);
		return this;
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		return Strings.isNullOrEmpty(requiredRole) || !(authentication instanceof PreAuthenticatedAuthenticationToken) || isPathExcluded(request);
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof PreAuthenticatedAuthenticationToken) {
			// IMS filter in use
			List<String> roles = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
			if (roles.contains(requiredRole)) {
				filterChain.doFilter(request, response);
				return;
			}
			LOGGER.info("User '{}' with roles '{}' does not have permission to access this resource: {}", SecurityUtil.getUsername(), roles, request.getRequestURI());
			response.setStatus(HttpStatus.UNAUTHORIZED.value());
			response.getWriter().println("The current user does not have permission to access this resource.");
		}
	}

	private boolean isPathExcluded(HttpServletRequest request) {
		String requestURI = request.getRequestURI();
		String contextPath = request.getContextPath();
		if (requestURI != null) {
			if (requestURI.startsWith(contextPath)) {
				requestURI = requestURI.substring(contextPath.length());
			}
			AntPathMatcher antPathMatcher = new AntPathMatcher();
			for (String excludedUrlPattern : excludedUrlPatterns) {
				if (antPathMatcher.match(excludedUrlPattern, requestURI)) {
					return true;
				}
			}
		}
		return false;
	}

}
