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
		assertEquals("Appending a single vector should result that same vector.", vec, result);
	}

	@Test
	@Repeat(iterations = 10)
	public void testAppendTwo() {
		Vector vecA = randomVector();
		Vector vecB = randomVector();
		Vector result = Vectors.append(vecA, vecB);
		double sum = Math.pow(vecA.norm(2), 2) + Math.pow(vecB.norm(2), 2);
		double length = Math.sqrt(sum);
		assertEquals("Appending two vectors should result in a vector of added length.", length, result.norm(2), 0.00001);
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
