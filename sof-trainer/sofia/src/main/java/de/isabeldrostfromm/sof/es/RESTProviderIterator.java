package de.isabeldrostfromm.sof;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.collect.Sets;

import com.google.common.base.Preconditions;
import com.google.gson.internal.StringMap;

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
	protected Document parse() {
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
			return Document.of(body, state, title, reputation, tags);
			
		}
		return null;
	}
}
