package org.snomed.release.note;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.snomed.release.note.core.data.service.LineItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfig.class})
public abstract class AbstractTest {

	@Autowired
	protected LineItemService lineItemService;

	private final static ElasticsearchContainer elasticsearchContainer = TestConfig.getElasticsearchContainerInstance();

	@BeforeAll
	static void setUp() {
		if (!TestConfig.useLocalElasticsearch) {
			assertTrue(elasticsearchContainer.isRunning(), "Test container is not running");
		}
	}

	@AfterEach
	void defaultTearDown() {
		lineItemService.deleteAll();
	}
}