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
public class ESProvider implements DocumentProvider, Closeable {

	private JestClient client;
	private boolean onlyOpen;
	private int start = 0;
	private int total = 10;
	
	public static ESProvider defaultInstance(boolean onlyOpen) {
		return pagedInstance(onlyOpen, 0, 10);
	}

	public static ESProvider pagedInstance(boolean onlyOpen, int start, int total) {
		ClientConfig conf = new ClientConfig();
		LinkedHashSet<String> set = new LinkedHashSet<String>();
		set.add("http://localhost:9200");
		conf.getServerProperties().put(ClientConstants.SERVER_LIST, set);
		
		JestClientFactory factory = new JestClientFactory();
		factory.setClientConfig(conf);
		JestClient client = factory.getObject();
		return new ESProvider(client, onlyOpen, start, total); 
	}
	
	private ESProvider(JestClient client, boolean onlyOpen, int start, int total) {
		this.client = client;
		this.onlyOpen = onlyOpen;
		this.start = start;
		this.total = total;
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
		QueryBuilder qbuilder = null;
		if (onlyOpen) {
			//qbuilder = QueryBuilders.termQuery("open_status", "open");			
			qbuilder = QueryBuilders.termQuery("body", "java");
		} else {
			//qbuilder = QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery("open_status", "open"));
			qbuilder = QueryBuilders.boolQuery().mustNot(QueryBuilders.termQuery("body", "java"));
		}

		// TODO retrieve more than just top 10 results
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

		public ESDocumentIterator(Map<String, StringMap> result) {
			Preconditions.checkNotNull(result);

			if (result.isEmpty()) {
				this.hits = new ArrayList<StringMap>();
			} else {
				StringMap obj_1 = (StringMap) result.get("hits");
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
				return Document.of(body, title, reputation, tags);
				
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
