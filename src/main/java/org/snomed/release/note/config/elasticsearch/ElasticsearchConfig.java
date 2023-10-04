package org.snomed.release.note.config.elasticsearch;

import jakarta.validation.constraints.NotNull;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
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

		if (elasticsearchProperties().getUsername() != null && !elasticsearchProperties().getUsername().isEmpty()) {
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();

			credentialsProvider.setCredentials(AuthScope.ANY,
					new UsernamePasswordCredentials(
							elasticsearchProperties().getUsername(),
							elasticsearchProperties().getPassword())
			);

			return ClientConfiguration.builder()
					.connectedTo(elasticsearchProperties().getUrls())
					.withClientConfigurer(ElasticsearchClients.ElasticsearchRestClientConfigurationCallback
							.from(restClientBuilder -> restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectionRequestTimeout(0))))
					.withClientConfigurer(ElasticsearchClients.ElasticsearchHttpClientConfigurationCallback
							.from(httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider)))
					.build();
		}

		ClientConfiguration.TerminalClientConfigurationBuilder builder = ClientConfiguration.builder()
				.connectedTo(elasticsearchProperties().getUrls())
				.withClientConfigurer(ElasticsearchClients.ElasticsearchRestClientConfigurationCallback
						.from(restClientBuilder -> restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectionRequestTimeout(0))));

		return ClientConfiguration.builder()
				.connectedTo(getHosts(elasticsearchProperties().getUrls()))
				.withClientConfigurer(ElasticsearchClients.ElasticsearchRestClientConfigurationCallback
						.from(restClientBuilder -> restClientBuilder.setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectionRequestTimeout(0))))
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
