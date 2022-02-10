package org.snomed.release.note.core.data.domain;

import java.util.List;

public interface LineItemView {

	List<LineItem> getChildren();

	String getTitle();
}
