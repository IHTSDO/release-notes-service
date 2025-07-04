package org.snomed.release.note.config;

import com.google.common.base.Strings;
import io.kaicode.rest.util.branchpathrewrite.BranchPathUriRewriteFilter;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.ihtsdo.sso.integration.RequestHeaderAuthenticationDecorator;
import org.snomed.release.note.rest.config.AccessDeniedExceptionHandler;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Autowired(required = false)
	private BuildProperties buildProperties;

	@Value("${rnms.rest-api.readonly}")
	private boolean restApiReadOnly;

	@Value("${ims-security.roles.enabled}")
	private boolean rolesEnabled;

	@Value("${ims-security.required-role}")
	private String requiredRole;

	private final String[] excludedUrlPatterns = {
			"/version",
			"/swagger-ui/**",
			"/v3/api-docs/**"
	};

	@Bean
	public WebSecurityCustomizer webSecurityCustomizer() {
		return (web) -> web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(AbstractHttpConfigurer::disable);// lgtm [java/spring-disabled-csrf-protection]
		http.sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

		if (restApiReadOnly) {
			// Read-only mode
			// Block all POST/PUT/PATCH/DELETE
			http.authorizeHttpRequests(c -> c
					.requestMatchers(HttpMethod.POST, "/**").denyAll()
					.requestMatchers(HttpMethod.PUT, "/**").denyAll()
					.requestMatchers(HttpMethod.PATCH, "/**").denyAll()
					.requestMatchers(HttpMethod.DELETE, "/**").denyAll()
					.anyRequest().permitAll());
		} else {
			if (rolesEnabled) {
				http.addFilterBefore(new RequestHeaderAuthenticationDecorator(), AuthorizationFilter.class);

				if (!Strings.isNullOrEmpty(requiredRole)) {
					http.authorizeHttpRequests(c -> c
							.requestMatchers(excludedUrlPatterns).permitAll()
							.anyRequest().hasAuthority(requiredRole));
				} else {
					http.authorizeHttpRequests(c -> c
							.requestMatchers(excludedUrlPatterns).permitAll()
							.anyRequest().authenticated());
				}
			} else {
				http.authorizeHttpRequests(c -> c
						.requestMatchers(excludedUrlPatterns).permitAll()
						.anyRequest().authenticated());
			}

			http.exceptionHandling(c -> c.accessDeniedHandler(new AccessDeniedExceptionHandler()))
					.httpBasic(Customizer.withDefaults());
		}

		return http.build();
	}

	@Bean
	public FilterRegistrationBean<BranchPathUriRewriteFilter> getUrlRewriteFilter() {
		// Encode branch paths in uri to allow request mapping to work
		return new FilterRegistrationBean<>(new BranchPathUriRewriteFilter(
				"/(.*)/subjects",
				"/(.*)/subjects/.*",
				"/(.*)/lineitems",
				"/(.*)/lineitems/.*",
				"/test/(.*)/testData"
		));
	}

	@Bean
	public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
		DefaultHttpFirewall firewall = new DefaultHttpFirewall();
		firewall.setAllowUrlEncodedSlash(true);
		return firewall;
	}

	@Bean
	public GroupedOpenApi apiDocs() {
		return GroupedOpenApi.builder()
				.group("release-notes-service")
				.packagesToScan("org.snomed.release.note.rest")
				// Don't show the error or root endpoints in swagger
				.pathsToExclude("/error", "/")
				.build();
	}

	@Bean
	public GroupedOpenApi springActuatorApi() {
		return GroupedOpenApi.builder()
				.group("actuator")
				.packagesToScan("org.springframework.boot.actuate")
				.pathsToMatch("/actuator/**")
				.build();
	}

	@Bean
	public OpenAPI apiInfo() {
		final String version = buildProperties != null ? buildProperties.getVersion() : "DEV";
		return new OpenAPI()
				.info(new Info()
						.title("SNOMED CT Release Notes")
						.description("Standalone service for management of SNOMED CT release notes")
						.version(version)
						.contact(new Contact().name("SNOMED International").url("https://www.snomed.org"))
						.license(new License().name("Apache 2.0").url("http://www.apache.org/licenses/LICENSE-2.0")))
				.externalDocs(new ExternalDocumentation()
								.description("See more about Release Notes Service in GitHub")
								.url("https://github.com/IHTSDO/release-notes-service"));
	}
}