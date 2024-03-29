package org.snomed.release.note.config.elasticsearch;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("rnms.elasticsearch")
public class ElasticsearchProperties {

	private String[] urls;
	private String username;
	private String password;
	private IndexProperties index = new IndexProperties();

	public String[] getUrls() {
		return urls;
	}

	public void setUrls(String[] urls) {
		this.urls = urls;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public IndexProperties getIndex() {
		return index;
	}

	public void setIndex(IndexProperties index) {
		this.index = index;
	}

	public static class IndexProperties {

		private String prefix;

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

	}

}
