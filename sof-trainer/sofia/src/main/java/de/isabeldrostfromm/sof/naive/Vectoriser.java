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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.LuceneTextValueEncoder;

import de.isabeldrostfromm.sof.util.Vectors;

/**
 * Vectorisation based on LuceneTextValueEncoder for body, title and tags.
 * */
public class Vectoriser {
	/** Cardinality of the vector portion to use for encoding posting bodies. */
	public static final int bodyCard = 1000000;
	/** Cardinality of the vector portion to use for encoding posting titles. */
	public static final int titleCard = 1000000;
	/** Cardinality of the vector portion to use for encoding posting tags. */
	private static final int tagCard = 0;
	/** Number of single double values to encode */
	private static final int doubles = 0;

	public static int getCardinality() {
		return bodyCard + titleCard + tagCard + doubles;
	}
	
	
	/**
	 * Turn a document bean into a vector.
	 * @param document the document to turn in a vector.
	 * @return the resulting vector.
	 * */
	public Vector vectorise(Document document) {
		Vector body = luceneEncode(bodyCard, document.getBody());
		Vector title = luceneEncode(titleCard, document.getTitle());
		//Vector tags = luceneEncode(tagCard, Strings.collectionToCommaDelimitedString(document.getTags()));
		//Vector reputation = Vectors.newSequentialAccessSparseVector(document.getReputation());

		return Vectors.append(body, title);//, tags);//, reputation);
	}
	
	/**
	 * @return Returns a vector generated for the given text based on encoding with LuceneTextValueEncoder
	 * */
	private static Vector luceneEncode(int probes, String text) {
		LuceneTextValueEncoder encoder = new LuceneTextValueEncoder("sof");
		encoder.setAnalyzer(new StandardAnalyzer(Version.LUCENE_36));
		encoder.setProbes(probes);
		encoder.addText(text);
		Vector vector = new SequentialAccessSparseVector(probes);
		encoder.flush(1, vector);
		return vector;
	}

}
