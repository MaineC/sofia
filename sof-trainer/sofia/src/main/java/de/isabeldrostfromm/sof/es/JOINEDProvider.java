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

import java.util.Iterator;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;

import de.isabeldrostfromm.sof.Document;
import de.isabeldrostfromm.sof.DocumentProvider;

import lombok.extern.log4j.Log4j;


/**
 * This implementation provides documents as retrieved through the
 * Elastic Search Java API.
 * 
 * WARNING: Implementation does not work currently on Ubuntu 12.4!
 * TODO tests missing
 * */
@Log4j
public class JOINEDProvider implements DocumentProvider {

	private final Client client;
	private final Node node;
	private final QueryBuilder qbuilder;
	private final int start;
	private final int total;

	private JOINEDProvider(Client client, Node node, QueryBuilder qbuilder, int start, int total) {
		this.client = client;
		this.node = node;
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
		Node node = (new NodeBuilder().client(true).clusterName("elasticsearch_mainec_sandbox")).node();
		
		Client client = node.client();
		return new JOINEDProvider(client, node, qbuilder, start, total); 
	}
		
	/**
	 * Each call to this method will initiate a new query to elastic search and return
	 * the resulting hits (given the search parameter the Provider was initialized with.
	 * */
	@Override
	public Iterator<Document> iterator() {
		SearchResponse response = client.prepareSearch("sof-sample")
		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		        .setQuery(qbuilder)
		        .setFrom(start).setSize(total)
		        .execute()
		        .actionGet();
		return new JOINEDProviderIterator(response.getHits());
	}
	
	@Override
	public void close() {
		try {
			node.close();
		} catch (Throwable t) {
			log.error("Unable to close JestClient ", t);
		}
	}
}
