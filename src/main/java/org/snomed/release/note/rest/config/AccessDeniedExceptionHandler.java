package org.snomed.release.note.rest.config;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AccessDeniedExceptionHandler implements AccessDeniedHandler {

	protected static final Logger LOGGER = LoggerFactory.getLogger(AccessDeniedExceptionHandler.class);

	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException, ServletException {
		LOGGER.error("Request '{}' raised: " + exception.getMessage(), request.getRequestURL(), exception);
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write(new JSONObject()
				.put("errorMessage", exception.getLocalizedMessage())
				.put("httpStatus", HttpStatus.FORBIDDEN.toString())
				.toString());
	}

}
