package org.snomed.release.note.config;

import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.RestClients;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

public class ElasticsearchConfig extends AbstractElasticsearchConfiguration {

	@Value("${elasticsearch.url}")
	private String elasticsearchUrl;

	@Override
	public RestHighLevelClient elasticsearchClient() {
		// TODO externalize config for elasticsearch url, index prefix and shard configurations etc.
		ClientConfiguration clientConfiguration = ClientConfiguration.builder().connectedTo(elasticsearchUrl).build();
		return RestClients.create(clientConfiguration).rest();
	}
}
