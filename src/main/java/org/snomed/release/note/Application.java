package org.snomed.release.note;

import org.snomed.release.note.config.Config;
import org.springframework.boot.SpringApplication;

public class Application extends Config {

	public static void main(String[] args) {
		System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true"); // Swagger encodes the slash in branch paths
		SpringApplication.run(Application.class, args);
	}

}
