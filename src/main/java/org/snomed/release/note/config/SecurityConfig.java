package org.snomed.release.note.config;

import org.ihtsdo.sso.integration.RequestHeaderAuthenticationDecorator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import static java.util.function.Predicate.*;
import static springfox.documentation.builders.PathSelectors.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers("/swagger-ui/index.html",
				"/swagger-resources/**",
				"/v2/api-docs").permitAll()
				.anyRequest().authenticated()
				.and().httpBasic();
		http.csrf().disable();
		http.addFilterAfter(new RequestHeaderAuthenticationDecorator(), BasicAuthenticationFilter.class);
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
