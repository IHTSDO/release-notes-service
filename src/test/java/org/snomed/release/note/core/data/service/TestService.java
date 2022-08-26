package org.snomed.release.note.core.data.service;

import org.ihtsdo.otf.rest.exception.BadConfigurationException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.rest.pojo.LineItemCreateRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
public class TestService {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestService.class);

	@Autowired
	private LineItemService lineItemService;

	public void createData(final String path) throws BusinessServiceException {
		try {
			JSONArray array = new JSONArray(readFile("src/test/resources/data.json"));
			createLineItems(array, path, null);
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
			throw new BadConfigurationException(e.getMessage(), e);
		}
	}

	public void deleteData(final String path) {
		lineItemService.find(path).forEach(lineItem -> lineItemService.delete(lineItem.getId(), path));
	}

	private String readFile(final String fileName) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))) {
			br.lines().forEach(sb::append);
		}
		return sb.toString();
	}

	private LineItem createLineItem(final JSONObject obj, final String path, final String parentId) throws BusinessServiceException {
		LineItemCreateRequest lineItemCreateRequest = new LineItemCreateRequest();

		lineItemCreateRequest.setParentId(parentId);
		if (!obj.isNull("title")) {
			lineItemCreateRequest.setTitle(obj.getString("title"));
		}
		if (!obj.isNull("content")) {
			lineItemCreateRequest.setContent(obj.getString("content"));
		}
		if (!obj.isNull("level")) {
			lineItemCreateRequest.setLevel(obj.getInt("level"));
		}
		if (!obj.isNull("sequence")) {
			lineItemCreateRequest.setSequence(obj.getInt("sequence"));
		}

		return lineItemService.create(lineItemCreateRequest, path);
	}

	private void createLineItems(JSONArray array, String path, String parentId) throws BusinessServiceException {
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			LineItem lineItem = createLineItem(obj, path, parentId);
			createLineItems(obj.getJSONArray("children"), path, lineItem.getId());
		}
	}
}