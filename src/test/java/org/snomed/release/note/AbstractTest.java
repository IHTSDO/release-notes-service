package org.snomed.release.note;

import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.snomed.release.note.core.data.service.LineItemService;
import org.snomed.release.note.core.data.service.SubjectService;
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
	protected RestHighLevelClient elasticsearchClient;

	@Autowired
	protected LineItemService lineItemService;

	@Autowired
	protected SubjectService subjectService;


	private static ElasticsearchContainer elasticsearchContainer = TestConfig.getElasticsearchContainerInstance();

	@BeforeAll
	static void setUp() {
		if (!TestConfig.useLocalElasticsearch) {
			assertTrue(TestConfig.getElasticsearchContainerInstance().isRunning(), "Test container is not running");
		}
	}

	@AfterEach
	void defaultTearDown() throws InterruptedException {
		subjectService.deleteAll();
		lineItemService.deleteAll();
	}
}