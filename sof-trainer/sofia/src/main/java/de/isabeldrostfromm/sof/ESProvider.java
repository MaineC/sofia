package de.isabeldrostfromm.sof;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.ClientConfig;
import io.searchbox.client.config.ClientConstants;
import io.searchbox.core.Search;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import lombok.extern.log4j.Log4j;

import org.elasticsearch.common.collect.Sets;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.google.common.base.Preconditions;
import com.google.gson.internal.StringMap;

@Log4j
@SuppressWarnings("rawtypes")
public class ESProvider implements DocumentProvider, Closeable {

	private final JestClient client;
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
		ClientConfig conf = new ClientConfig();
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		set.add("http://localhost:9200");
		conf.getServerProperties().put(ClientConstants.SERVER_LIST, set);
		
		JestClientFactory factory = new JestClientFactory();
		factory.setClientConfig(conf);
		JestClient client = factory.getObject();

		return new ESProvider(client, qbuilder, start, total); 
	}
	
	private ESProvider(JestClient client, QueryBuilder qbuilder, int start, int total) {
		this.client = client;
		this.start = start;
		this.total = total;
		this.qbuilder = qbuilder;
	}
	
	public void close() {
		try {
			this.client.shutdownClient();
		} catch (Throwable t) {
			log.error("Unable to close JestClient ", t);
		}
	}
	
	@Override
	public Iterator<Document> iterator() {
		Search search = new Search(Search.createQueryWithBuilder(qbuilder.toString()));
		search.addParameter("from", start);
		search.addParameter("size", total); 
		search.addIndex("sof-sample");
		JestResult result;
		try {
			result = client.execute(search);
		} catch (Exception e) {
			return new ESDocumentIterator(new HashMap<String, StringMap>());
		}
		ESDocumentIterator iter = new ESDocumentIterator(result.getJsonMap());
		return iter;
	}
	
	private class ESDocumentIterator implements Iterator<Document> {

		private ArrayList<StringMap> hits;
		private int cursor = -1;
		private Document parsed;

		@SuppressWarnings("unchecked")
		public ESDocumentIterator(Map<String, StringMap> result) {
			Preconditions.checkNotNull(result);

			if (result.isEmpty()) {
				this.hits = new ArrayList<StringMap>();
			} else {
				StringMap obj_1 = (StringMap) result.get("hits");
				if (obj_1 == null) 	System.out.println(result);
				hits = (ArrayList) obj_1.get("hits");
			}
		}

		private synchronized Document parse() {
			if ( (cursor + 1) < hits.size()) {
				cursor++;
				StringMap entry = hits.get(cursor);
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
