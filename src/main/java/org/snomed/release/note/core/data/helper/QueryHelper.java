package org.snomed.release.note.core.data.helper;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.json.JsonData;

public class QueryHelper {

	public static Query existsQuery(String field) {
		return new ExistsQuery.Builder().field(field).build()._toQuery();
	}

	public static Query termQuery(String field, Object value) {
		return new TermQuery.Builder().field(field).value(FieldValue.of(JsonData.of(value))).build()._toQuery();
	}

	public static Query regexpQuery(String field, String value) {
		return new RegexpQuery.Builder().field(field).value(value).build()._toQuery();
	}
}
