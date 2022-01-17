package org.snomed.release.note.config;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableConfigurationProperties
@PropertySource(value = "classpath:application.properties", encoding = "UTF-8")
public abstract class Config extends ElasticsearchConfig {
	
}
