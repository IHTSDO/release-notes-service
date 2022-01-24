package org.snomed.release.note.core.data.service;

import org.ihtsdo.otf.rest.exception.BadConfigurationException;
import org.ihtsdo.otf.rest.exception.BusinessServiceException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snomed.release.note.core.data.domain.LineItem;
import org.snomed.release.note.core.data.domain.Subject;
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
	private SubjectService subjectService;

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
		subjectService.find(path).forEach(subject -> subjectService.delete(subject.getId(), path));
		lineItemService.find(path).forEach(lineItem -> lineItemService.delete(lineItem.getId(), path));
	}

	private String readFile(final String fileName) throws IOException {
		StringBuilder sb = new StringBuilder();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))) {
			br.lines().forEach(line -> sb.append(line));
		}
		return sb.toString();
	}

	private Subject createSubject(final JSONObject obj, final String path) throws BusinessServiceException {
		return subjectService.create(new Subject(obj.getString("title"), path), path);
	}

	private LineItem createLineItem(final JSONObject obj, final String path, final String parentId) throws BusinessServiceException {
		Subject subject = createSubject(obj, path);
		String content = null;
		if (obj.get("content") != JSONObject.NULL) {
			content = obj.getString("content");
		}
		LineItem lineItem = new LineItem(subject.getId(), path, content);
		lineItem.setLevel(obj.getInt("level"));
		lineItem.setSequence(obj.getInt("sequence"));
		lineItem.setParentId(parentId);
		return lineItemService.create(lineItem, path);
	}

	private void createLineItems(JSONArray array, String path, String parentId) throws BusinessServiceException {
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj = array.getJSONObject(i);
			LineItem lineItem = createLineItem(obj, path, parentId);
			createLineItems(obj.getJSONArray("children"), path, lineItem.getId());
		}
	}
}
