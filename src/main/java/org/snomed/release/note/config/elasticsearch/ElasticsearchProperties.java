package org.snomed.release.note.config.elasticsearch;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("elasticsearch")
public class ElasticsearchProperties {

	private String[] urls;
	private String apiKey;
	private String username;
	private String password;
	private IndexProperties index = new IndexProperties();

	public String[] getUrls() {
		return urls;
	}

	public void setUrls(String[] urls) {
		this.urls = urls;
	}

	public String getApiKey() {
		return this.apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
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

	public String getIndexPrefix(){
		return index.getPrefix();
	}

	public static class IndexProperties {

		private String prefix;

		private App app;

		public String getPrefix() {
			return prefix;
		}

		public void setPrefix(String prefix) {
			this.prefix = prefix;
		}

		public App getApp() {
			return app;
		}

		public void setApp(App app) {
			this.app = app;
		}

		public static class App {
			private String prefix;

			public String getPrefix() {
				return prefix;
			}

			public void setPrefix(String prefix) {
				this.prefix = prefix;
			}
		}

	}

}
