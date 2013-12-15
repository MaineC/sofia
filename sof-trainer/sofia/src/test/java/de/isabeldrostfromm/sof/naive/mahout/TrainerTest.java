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
package de.isabeldrostfromm.sof.naive.mahout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.google.common.collect.Sets;

import de.isabeldrostfromm.sof.Example;
import de.isabeldrostfromm.sof.ExampleProvider;
import de.isabeldrostfromm.sof.ModelTargets;
import de.isabeldrostfromm.sof.Trainer;
import de.isabeldrostfromm.sof.naive.Document;
import de.isabeldrostfromm.sof.naive.Vectoriser;

public class TrainerTest extends RandomizedTest {

	private final Trainer trainer = new Trainer();
	private final Vectoriser v = new Vectoriser();

	// TODO make test more meaningful - only works for first category right now.
	@Test
	@Repeat(iterations = 10)
	public void testTestingOnTraining() {
		ArrayList<Example> docs = new ArrayList<Example>();
		ArrayList<String> states = new ArrayList<String>();
		for (int i = 0; i < ModelTargets.INDECES.size(); i++) {
			String body = randomText(3, 20, 20, 100);
			String state = ModelTargets.INDECES.get(0);
			Document doc = Document.of(body, state, "", 0.0, Sets.newHashSet(""));
			docs.add(Example.of(v.vectorise(doc), doc.getState()));
			states.add(state);
		}

		ExampleProvider provider = new MockProvider(docs);
		OnlineLogisticRegression model = trainer.train(provider);
		List<String> result = trainer.apply(model, provider);
		for (int i = 0; i < states.size(); i++) {
			assertEquals("Testing on train data should return perfect classification.",
					states.get(i),
					result.get(i));
		}
	}

	private class MockProvider implements ExampleProvider {

		private Collection<Example> docs;

		public MockProvider(Collection<Example> docs) {
			this.docs = docs;
		}

		@Override
		public Iterator<Example> iterator() {
			return docs.iterator();
		}

		@Override
		public void close() throws IOException {
			// not needed here
		}
		
	}

	// TODO factor into separate random sofia testing class
	private String randomText(int minTokenLength, int maxTokenLength, int minTokens, int maxTokens) {
		StringBuffer result = new StringBuffer();
		int tokens = randomIntBetween(minTokens, maxTokens);
		for (int i = 0; i < tokens; i++) {
			result.append(randomAsciiOfLengthBetween(minTokenLength, maxTokenLength));
			result.append(" ");
		}
		return result.toString();
	}

}
