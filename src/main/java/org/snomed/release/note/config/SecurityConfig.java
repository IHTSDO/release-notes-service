package org.snomed.release.note.config;

import io.kaicode.rest.util.branchpathrewrite.BranchPathUriRewriteFilter;
import org.ihtsdo.sso.integration.RequestHeaderAuthenticationDecorator;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import static java.util.function.Predicate.*;
import static springfox.documentation.builders.PathSelectors.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	public void configure(WebSecurity web) {
		web.httpFirewall(allowUrlEncodedSlashHttpFirewall());
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.authorizeRequests()
				.antMatchers("/swagger-ui/index.html",
				"/swagger-resources/**",
				"/v2/api-docs").permitAll()
				.anyRequest().permitAll() // disable it temporarily
				.and().httpBasic();

		http.addFilterAfter(new RequestHeaderAuthenticationDecorator(), BasicAuthenticationFilter.class);
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
