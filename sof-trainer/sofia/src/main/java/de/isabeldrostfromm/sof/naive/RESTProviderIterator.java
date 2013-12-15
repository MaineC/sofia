/**
 * Copyright (C) 2013 Isabel Drost-Fromm
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.isabeldrostfromm.sof.naive;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.collect.Sets;

import com.google.common.base.Preconditions;
import com.google.gson.internal.StringMap;

import de.isabeldrostfromm.sof.Example;
import de.isabeldrostfromm.sof.ProviderIterator;

/**
 * Iterator for ES results as returned by the Java API.
 * 
 * Given a finished search init objects of this type
 * with the resulting hits. For each call to iterator
 * a new hit will be parsed and returned as Document
 * object.
 * 
 * Implementation is not thread safe!
 * 
 * TODO provide Document not as a copied object but
 * as a proxy that directly works on the underlying
 * hit and enable lazy Document init/ hit parsing that way.
 * 
 * TODO tests missing
 * */
@SuppressWarnings("unchecked")
public class RESTProviderIterator extends ProviderIterator {

	@SuppressWarnings("rawtypes")
	private ArrayList<StringMap> hits;
	private int cursor = -1;
	private Vectoriser v = new Vectoriser();

	@SuppressWarnings("rawtypes")
	public RESTProviderIterator(Map<String, StringMap> result) {
		Preconditions.checkNotNull(result);

		if (result.isEmpty()) {
			this.hits = new ArrayList<StringMap>();
		} else {
			StringMap obj_1 = result.get("hits");
			if (obj_1 == null) 	System.out.println(result);
			hits = (ArrayList<StringMap>) obj_1.get("hits");
		}
	}

	@Override
	protected Example parse() {
		if ( (cursor + 1) < hits.size()) {
			cursor++;
			@SuppressWarnings("rawtypes")
			StringMap entry = hits.get(cursor);
			@SuppressWarnings("rawtypes")
			StringMap srcDoc = (StringMap) entry.get("_source");

			String body = (String) srcDoc.get("body");
			String title = (String) srcDoc.get("title");

			double reputation = Double.parseDouble((String) srcDoc.get("reputation_at_post_creation"));

			String tag1 = (String) srcDoc.get("tag_1");
			String tag2 = (String) srcDoc.get("tag_2");
			String tag3 = (String) srcDoc.get("tag_3");
			String tag4 = (String) srcDoc.get("tag_4");
			String tag5 = (String) srcDoc.get("tag_5");
			Set<String> tags = Sets.newHashSet(tag1, tag2, tag3, tag4, tag5);
			
			String state = (String) srcDoc.get("open_status");
			return Example.of(v.vectorise(Document.of(body, state, title, reputation, tags)), state);
		}
		return null;
	}
}
