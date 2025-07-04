package org.snomed.release.note.config.elasticsearch;

import com.google.common.base.Strings;
import io.github.acm19.aws.interceptor.http.AwsRequestSigningApacheInterceptor;
import jakarta.validation.constraints.NotNull;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.support.HttpHeaders;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.signer.Aws4Signer;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

import java.util.*;

public class ElasticsearchConfig extends ElasticsearchConfiguration {

	@Value("${elasticsearch.username}")
	private String elasticsearchUsername;

	@Value("${elasticsearch.password}")
	private String elasticsearchPassword;

	@Value("${elasticsearch.index.prefix}")
	private String indexNamePrefix;

	@Value("${elasticsearch.index.app.prefix}")
	private String indexNameApplicationPrefix;

	@Value("${elasticsearch.api-key}")
	private String apiKey;

	@Value("${rnms.aws.request-signing.enabled}")
	private Boolean awsRequestSigning;

	private final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public @NotNull ClientConfiguration clientConfiguration() {
		final String[] urls = elasticsearchProperties().getUrls();
		for (String url : urls) {
			logger.info("Elasticsearch host: {}", url);
		}

		logger.info("Elasticsearch index prefix: {}", indexNamePrefix);
		logger.info("Elasticsearch index application prefix: {}", indexNameApplicationPrefix);

		HttpHeaders apiKeyHeaders = new HttpHeaders();
		if (!Strings.isNullOrEmpty(apiKey)) {
			logger.info("Using API key authentication.");
			apiKeyHeaders.add(HttpHeaders.AUTHORIZATION, "ApiKey " + apiKey);
		}

		if (useHttps(urls)) {
			return ClientConfiguration.builder()
					.connectedTo(getHosts(urls))
					.usingSsl()
					.withDefaultHeaders(apiKeyHeaders)
					.withClientConfigurer(configureHttpClient())
					.build();
		} else {
			return ClientConfiguration.builder()
					.connectedTo(getHosts(urls))
					.withDefaultHeaders(apiKeyHeaders)
					.withClientConfigurer(configureHttpClient())
					.build();
		}
	}

	private boolean useHttps(String[] urls) {
		for (String url : urls) {
			if (url.startsWith("https://")) {
				return true;
			}
		}
		return false;
	}

	private ElasticsearchClients.ElasticsearchRestClientConfigurationCallback configureHttpClient() {
		return ElasticsearchClients.ElasticsearchRestClientConfigurationCallback.from(clientBuilder -> {
			clientBuilder.setRequestConfigCallback(builder -> {
				builder.setConnectionRequestTimeout(0);//Disable lease handling for the connection pool! See https://github.com/elastic/elasticsearch/issues/24069
				return builder;
			});
			final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
			if (!Strings.isNullOrEmpty(elasticsearchUsername) && !Strings.isNullOrEmpty(elasticsearchPassword)) {
				credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(elasticsearchUsername, elasticsearchPassword));
			}
			clientBuilder.setHttpClientConfigCallback(httpClientBuilder -> {
				httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
				if (awsRequestSigning != null && awsRequestSigning) {
					httpClientBuilder.addInterceptorFirst(awsInterceptor("es"));
				}
				return httpClientBuilder;
			});
			return clientBuilder;
		});
	}

	private AwsRequestSigningApacheInterceptor awsInterceptor(String serviceName) {
		return new AwsRequestSigningApacheInterceptor(
				serviceName,
				Aws4Signer.create(),
				DefaultCredentialsProvider.create(),
				DefaultAwsRegionProviderChain.builder().build().getRegion());
	}

	private static String[] getHosts(String[] urls) {
		List<String> hosts = new ArrayList<>();
		for (String url : urls) {
			hosts.add(HttpHost.create(url).toHostString());
		}
		return hosts.toArray(new String[]{});
	}

	@Bean
	public ElasticsearchProperties elasticsearchProperties() {
		return new ElasticsearchProperties();
	}

	@Bean
	public IndexNameProvider indexNameProvider(ElasticsearchProperties elasticsearchProperties) {
		return new IndexNameProvider(elasticsearchProperties);
	}

}
