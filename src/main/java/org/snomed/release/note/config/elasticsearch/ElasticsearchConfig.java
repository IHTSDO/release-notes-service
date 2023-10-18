package org.snomed.release.note.config.elasticsearch;

import jakarta.validation.constraints.NotNull;
import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ElasticsearchConfig extends ElasticsearchConfiguration {

	private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchConfig.class);

	@Override
	public @NotNull ClientConfiguration clientConfiguration() {
		final String[] urls = elasticsearchProperties().getUrls();
		for (String url : urls) {
			LOGGER.info("Elasticsearch URL {}", url);
		}

		return ClientConfiguration.builder()
				.connectedTo(getHosts(elasticsearchProperties().getUrls()))
				.withBasicAuth(elasticsearchProperties().getUsername(), elasticsearchProperties().getPassword())
				.withClientConfigurer(ElasticsearchClients.ElasticsearchRestClientConfigurationCallback
					.from(restClientBuilder -> {
						restClientBuilder.setRequestConfigCallback(builder -> {
							builder.setConnectionRequestTimeout(0); //Disable lease handling for the connection pool! See https://github.com/elastic/elasticsearch/issues/24069
							return builder;
						});
						return restClientBuilder;
					}))
				.build();
	}

	@Bean
	public ElasticsearchProperties elasticsearchProperties() {
		return new ElasticsearchProperties();
	}

	@Bean
	public IndexNameProvider indexNameProvider(ElasticsearchProperties elasticsearchProperties) {
		return new IndexNameProvider(elasticsearchProperties);
	}

	private String[] getHosts(String[] urls) {
		List<String> hosts = new ArrayList<>();
		for (String url : urls) {
			hosts.add(HttpHost.create(url).toHostString());
		}
		return hosts.toArray(new String[]{});
	}

}
