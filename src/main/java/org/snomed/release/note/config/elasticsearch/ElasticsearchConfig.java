package org.snomed.release.note.config.elasticsearch;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

	private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchConfig.class);

	@Override
	public RestHighLevelClient elasticsearchClient() {
		final String[] urls = elasticsearchProperties().getUrls();
		for (String url : urls) {
			LOGGER.info("Elasticsearch host: {}", url);
		}

		RestClientBuilder restClientBuilder = RestClient.builder(getHttpHosts(urls));
		restClientBuilder.setRequestConfigCallback(builder -> {
			builder.setConnectionRequestTimeout(0); // Disable lease handling for the connection pool! See https://github.com/elastic/elasticsearch/issues/24069
			return builder;
		});

		if (elasticsearchProperties().getUsername() != null && !elasticsearchProperties().getUsername().isEmpty()) {
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(elasticsearchProperties().getUsername(), elasticsearchProperties().getPassword()));
			restClientBuilder.setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider));
		}
		return new RestHighLevelClient(restClientBuilder);
	}

	private static HttpHost[] getHttpHosts(String[] hosts) {
		List<HttpHost> httpHosts = new ArrayList<>();
		for (String host : hosts) {
			httpHosts.add(HttpHost.create(host));
		}
		return httpHosts.toArray(new HttpHost[]{});
	}

	@Bean
	public ElasticsearchProperties elasticsearchProperties() {
		return new ElasticsearchProperties();
	}

}
