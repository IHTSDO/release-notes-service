package org.snomed.release.note;

import org.apache.tomcat.util.buf.EncodedSolidusHandling;
import org.snomed.release.note.config.Config;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.context.annotation.Bean;

public class Application extends Config {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public TomcatConnectorCustomizer connectorCustomizer() {
		// Swagger encodes the slash in branch paths
		return connector -> connector.setEncodedSolidusHandling(EncodedSolidusHandling.DECODE.getValue());
	}

}
