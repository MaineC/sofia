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
package de.isabeldrostfromm.sof.es;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.JestResult;
import io.searchbox.client.config.ClientConfig;
import io.searchbox.client.config.ClientConstants;
import io.searchbox.core.Search;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;

import lombok.extern.log4j.Log4j;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.google.gson.internal.StringMap;

import de.isabeldrostfromm.sof.Document;
import de.isabeldrostfromm.sof.DocumentProvider;

/**
 * Provides query capabilities through the ES REST interface.
 * TODO tests missing
 * */
@Log4j
@SuppressWarnings("rawtypes")
public class RESTProvider implements DocumentProvider, Closeable {

	private final JestClient client;
	private final QueryBuilder qbuilder;
	private final int start;
	private final int total;
	
	private RESTProvider(JestClient client, QueryBuilder qbuilder, int start, int total) {
		this.client = client;
		this.start = start;
		this.total = total;
		this.qbuilder = qbuilder;
	}
	
	/**
	 * Returns a document provider that provides documents that match the string value in the given field.
	 * @param field the field to search for terms
	 * @param value the term to search for in field
	 * @param start first hit position to consider
	 * @param total total number of hits to return
	 * @return fully initiated document provider
	 * */
	public static DocumentProvider filterInstance(String field, String value, int start, int total) {
		QueryBuilder qbuilder = QueryBuilders.termQuery(field, value);
		return instance(qbuilder, start, total);
	}
	
	/**
	 * Returns a document provider that provides documents that do *NOT* match the string value in the given field.
	 * @param field the field to search for terms
	 * @param value the term to search for in field
	 * @param start first hit position to consider
	 * @param total total number of hits to return
	 * @return fully initiated document provider
	 * */
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

		return new RESTProvider(client, qbuilder, start, total); 
	}

	/**
	 * Each call to this method will initiate a new query to elastic search and return
	 * the resulting hits (given the search parameter the Provider was initialized with.
	 * */
	@Override
	public Iterator<Document> iterator() {
		Search search = new Search(Search.createQueryWithBuilder(qbuilder.toString()));
		search.addParameter("from", start);
		search.addParameter("size", total); 
		search.addParameter("sort", "post_creation_date");
		search.addIndex("sof-sample");
		JestResult result;
		try {
			result = client.execute(search);
		} catch (Exception e) {
			return new RESTProviderIterator(new HashMap<String, StringMap>());
		}
		@SuppressWarnings("unchecked")
		RESTProviderIterator iter = new RESTProviderIterator(result.getJsonMap());
		return iter;
	}

	@Override
	public void close() {
		try {
			this.client.shutdownClient();
		} catch (Throwable t) {
			log.error("Unable to close JestClient ", t);
		}
	}
	
}
