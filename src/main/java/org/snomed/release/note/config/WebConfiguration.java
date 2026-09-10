package org.snomed.release.note.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.UrlHandlerFilter;

@Configuration
public class WebConfiguration {

	/*
	 * Replaces PathMatchConfigurer.setUseTrailingSlashMatch(true), removed in Spring Framework 7.
	 * wrapRequest() keeps the old semantics - trailing slash is stripped from the path the rest of the
	 * chain sees, so no redirect is issued.
	 */
	@Bean
	public FilterRegistrationBean<UrlHandlerFilter> trailingSlashFilter() {
		FilterRegistrationBean<UrlHandlerFilter> registration = new FilterRegistrationBean<>(
				UrlHandlerFilter.trailingSlashHandler("/**").wrapRequest().build());
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
		return registration;
	}
}
