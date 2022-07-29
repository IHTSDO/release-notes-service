package org.snomed.release.note.core.data.domain;

import java.util.Comparator;

public class LineItemComparator implements Comparator<LineItem> {
	@Override
	public int compare(LineItem o1, LineItem o2) {
		if (o1.getSequence().equals(o2.getSequence())) {
			return o1.getTitle().compareTo(o2.getTitle());
		} else {
			return o1.getSequence().compareTo(o2.getSequence());
		}
	}
}
