package de.isabeldrostfromm.sof;

import java.util.Map;
import java.util.Set;

import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.google.common.base.Preconditions;

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
public class JOINEDProviderIterator extends ProviderIterator {

	private SearchHit[] hits;
	private int cursor = -1;

	public JOINEDProviderIterator(SearchHits hits) {
		Preconditions.checkNotNull(hits);

		if (hits.getHits().length == 0) {
			this.hits = new SearchHit[0];
		} else {
			this.hits = hits.getHits();
		}
	}

	@Override
	protected Document parse() {
		if ((cursor + 1) < hits.length) {
			cursor++;
			SearchHit entry = hits[cursor];
			Map<String, Object> srcDoc = entry.getSource();

			String body = (String) srcDoc.get("body");
			String title = (String) srcDoc.get("title");

			double reputation = Double.parseDouble((String) srcDoc
					.get("reputation_at_post_creation"));

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