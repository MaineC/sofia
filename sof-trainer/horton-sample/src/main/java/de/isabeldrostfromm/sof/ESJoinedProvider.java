package de.isabeldrostfromm.sof;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.google.common.base.Preconditions;

import lombok.extern.log4j.Log4j;

@Log4j
public class ESJoinedProvider implements DocumentProvider {

	private final Node node;
	private final Client client;
	private final QueryBuilder qbuilder;
	private final int start;
	private final int total;
	
	public static DocumentProvider defaultInstance(String field, String value) {
		return filterInstance(field, value, 0, 10);
	}

	public static DocumentProvider filterInstance(String field, String value, int start, int total) {
		QueryBuilder qbuilder = QueryBuilders.termQuery(field, value);
		return instance(qbuilder, start, total);
	}
	
	public static DocumentProvider negatedFilterInstance(String field, String value, int start, int total) {
		QueryBuilder qbuilder = QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery(field, value));
		return instance(qbuilder, start, total);
	}

	private static DocumentProvider instance(QueryBuilder qbuilder, int start, int total) {
		Node node = (new NodeBuilder().client(true).clusterName("elasticsearch_mainec_sandbox")).node();
		
		Client client = node.client();
		return new ESJoinedProvider(client, node, qbuilder, start, total); 
	}
	
	private ESJoinedProvider(Client client, Node node, QueryBuilder qbuilder, int start, int total) {
		this.client = client;
		this.node = node;
		this.start = start;
		this.total = total;
		this.qbuilder = qbuilder;
	}
	
	public void close() {
		try {
			node.close();
		} catch (Throwable t) {
			log.error("Unable to close JestClient ", t);
		}
	}
	
	@Override
	public Iterator<Document> iterator() {
		SearchResponse response = client.prepareSearch("sof-sample")
		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		        .setQuery(qbuilder)
		        .setFrom(start).setSize(total)
		        .execute()
		        .actionGet();
		return new ESDocumentIterator(response.getHits());
	}
	
	private class ESDocumentIterator implements Iterator<Document> {

		private SearchHit[] hits;
		private int cursor = -1;
		private Document parsed;

		@SuppressWarnings("unchecked")
		public ESDocumentIterator(SearchHits hits) {
			Preconditions.checkNotNull(hits);

			if (hits.getHits().length == 0) {
				this.hits = new SearchHit[0];
			} else {
				this.hits = hits.getHits();
			}
		}

		private synchronized Document parse() {
			if ( (cursor + 1) < hits.length) {
				cursor++;
				SearchHit entry = hits[cursor];
				Map<String, Object> srcDoc = entry.getSource();

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

		@Override
		public boolean hasNext() {
			if (this.parsed != null) {
				return true;
			}

			this.parsed = parse();
			if (this.parsed != null)
				return true;

			return false;
		}

		@Override
		public Document next() {
			if (this.parsed == null) {
				this.parsed = parse();
			}
			Document current = this.parsed;
			this.parsed = null;
			return current;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException("No removal of documents from search results.");
		}
		
	}


}
