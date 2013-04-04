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
package de.isabeldrostfromm.sof.util;

import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;

public class VectorsTest extends RandomizedTest {

	@Test
	@Repeat(iterations = 5)
	public void testAppendOne() {
		SequentialAccessSparseVector vec = randomVector();
		Vector result = Vectors.append(vec);
		assertEquals("Appending a single vector should result that same vector.",
				vec,
				result);
	}

	@Test
	@Repeat(iterations = 10)
	public void testAppendTwo() {
		Vector vecA = randomVector();
		Vector vecB = randomVector();
		Vector result = Vectors.append(vecA, vecB);
		double sum = Math.pow(vecA.norm(2), 2) + Math.pow(vecB.norm(2), 2);
		double length = Math.sqrt(sum);
		assertEquals("Appending two vectors should result in a vector of added length.",
				length,
				result.norm(2),
				0.00001);
	}
	
	@Test
	@Repeat(iterations = 10)
	public void testCreation() {
		Vector vec = randomVector();
		double[] entries = new double[vec.getNumNondefaultElements()];
		int index = 0;
		for (Vector.Element e : vec) {
			entries[index] = e.get();
			index++;
		}
		Vector result = Vectors.newSequentialAccessSparseVector(entries);
		assertEquals("Original vector should have same length as the one created from its entries.",
				vec.norm(2),
				result.norm(2),
				0.0001);
	}

	private SequentialAccessSparseVector randomVector() {
		int length = atMost(100);
		SequentialAccessSparseVector vec = new SequentialAccessSparseVector(length);
		for (int i = 0; i < length; i++) {
			vec.setQuick(i,randomDouble());
		}
		return vec;
	}
}
