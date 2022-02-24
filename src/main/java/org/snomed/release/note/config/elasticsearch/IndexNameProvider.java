package org.snomed.release.note.config.elasticsearch;

import org.elasticsearch.common.Strings;

public class IndexNameProvider {

	private final String prefix;

	public IndexNameProvider(ElasticsearchProperties elasticsearchProperties) {
		this.prefix = elasticsearchProperties.getIndex().getPrefix();
	}

	public String getIndexNameWithPrefix(String indexName) {
		return Strings.isNullOrEmpty(prefix) ? indexName : prefix + indexName;
	}

}
