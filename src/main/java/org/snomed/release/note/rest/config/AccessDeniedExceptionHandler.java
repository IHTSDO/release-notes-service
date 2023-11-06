package org.snomed.release.note.rest.config;

import us.monoid.json.JSONException;
import us.monoid.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AccessDeniedExceptionHandler implements AccessDeniedHandler {

	protected static final Logger LOGGER = LoggerFactory.getLogger(AccessDeniedExceptionHandler.class);

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException {
		LOGGER.error("Request '{}' raised: " + exception.getMessage(), request.getRequestURL(), exception);
		response.setStatus(HttpStatus.FORBIDDEN.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);

		try {
			response.getWriter().write(new JSONObject()
					.put("errorMessage", exception.getLocalizedMessage())
					.put("httpStatus", HttpStatus.FORBIDDEN.toString())
					.toString());
		} catch (JSONException e) {
			LOGGER.error("Failed to write response body", e);
			throw new RuntimeException(e);
		}
	}

}
