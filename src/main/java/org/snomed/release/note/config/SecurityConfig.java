package org.snomed.release.note.config;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriRewriteFilter;
import org.ihtsdo.sso.integration.RequestHeaderAuthenticationDecorator;
import org.snomed.release.note.rest.config.AccessDeniedExceptionHandler;
import org.snomed.release.note.rest.security.RequiredRoleFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import static java.util.function.Predicate.*;
import static springfox.documentation.builders.PathSelectors.*;

@Configuration
@EnableWebSecurity()
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Value("${rnms.rest-api.readonly}")
	private boolean restApiReadOnly;

	@Value("${ims-security.roles.enabled}")
	private boolean rolesEnabled;

	@Value("${ims-security.required-role}")
	private String requiredRole;

	private final String[] excludedUrlPatterns = {
			"/swagger-ui/**",
			"/swagger-resources/**",
			"/v2/api-docs",
			"/webjars/springfox-swagger-ui/**"
	};

	@Override
	public void configure(WebSecurity web) {
		web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.csrf().disable();

		// Add custom security filters
		http.addFilterBefore(new RequestHeaderAuthenticationDecorator(), FilterSecurityInterceptor.class);
		http.addFilterAt(new RequiredRoleFilter(requiredRole, excludedUrlPatterns), FilterSecurityInterceptor.class);

		if (restApiReadOnly) {
			// Read-only mode
			// Block all POST/PUT/PATCH/DELETE
			http.authorizeRequests()
					.antMatchers(excludedUrlPatterns).permitAll()
					.antMatchers(HttpMethod.POST, "/**").denyAll()
					.antMatchers(HttpMethod.PUT, "/**").denyAll()
					.antMatchers(HttpMethod.PATCH, "/**").denyAll()
					.antMatchers(HttpMethod.DELETE, "/**").denyAll()
					.anyRequest().authenticated()
					.and().httpBasic();

		} else {
			http.authorizeRequests()
					.antMatchers(excludedUrlPatterns).permitAll()
					.anyRequest().authenticated()
					.and().exceptionHandling().accessDeniedHandler(new AccessDeniedExceptionHandler())
					.and().httpBasic();
		}
	}

	@Bean
	public UserDetailsService userDetailsService(@Value("${spring.security.user.name}") final String username,
												 @Value("${spring.security.user.password}") final String password) {
		UserDetails user = User.builder()
				.username(username)
				.password(passwordEncoder().encode(password))
				.authorities("ROLE_USER")
				.build();
		return new InMemoryUserDetailsManager(user);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
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
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2)
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(not(regex("/error")))
				.build();
	}
}