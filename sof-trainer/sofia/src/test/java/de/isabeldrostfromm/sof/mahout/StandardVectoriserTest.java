package de.isabeldrostfromm.sof.mahout;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.mahout.math.Vector;
import org.junit.Test;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.google.common.collect.Sets;

import de.isabeldrostfromm.sof.Document;

public class StandardVectoriserTest extends RandomizedTest {

	@Test
	public void testBodyVectorisation() {
		DocumentVectoriser vectorise = new StandardVectoriser();
		Document doc = Document.of("first", "", "", 0.0, new HashSet<String>());
		Vector vec = vectorise.vectorise(doc);
		assertEquals("Adding one term should result in two dimensions set to one.",
				2,
				vec.getNumNondefaultElements());

	}

	@Test
	public void testBodySingleWord() {
		DocumentVectoriser vectorise = new StandardVectoriser();
		Document doc = Document.of("first", "", "", 0.0, new HashSet<String>());
		Vector first = vectorise.vectorise(doc);
		Vector second = vectorise.vectorise(doc);
		assertEquals("Adding docs with same content should result in same vector.",
				first,
				second);

	}

	@Test
	public void testBodySingleDifferentWord() {
		DocumentVectoriser vectorise = new StandardVectoriser();
		Document firstDoc = Document.of("first", "", "", 0.0, new HashSet<String>());
		Document secondDoc = Document.of("second", "", "", 0.0, new HashSet<String>());
		Vector first = vectorise.vectorise(firstDoc);
		Vector second = vectorise.vectorise(secondDoc);
		assertNotEquals("Adding docs with same content should result in same vector.",
				first,
				second);

	}

	@Test
	public void testBodyVectorisation2Terms() {
		DocumentVectoriser vectorise = new StandardVectoriser();
		Document doc = Document.of("first second", "", "", 0.0, new HashSet<String>());
		Vector vec = vectorise.vectorise(doc);
		assertEquals("Adding one term should result in two dimensions set to one.",
				4,
				vec.getNumNondefaultElements());

	}

	@Repeat(iterations = 10)
	@Test
	public void testBodyUsage() throws IOException {
		String firstBody = randomText(10, 2000, 2, 100);
		String secondBody = randomText(10, 2000, 2, 100);;
		while (firstBody.equals(secondBody)) {
			secondBody = randomText(10, 2000, 2, 100);;
		}

		String title = randomText(10, 2000, 2, 10);;
		Set<String> tags = Sets.newHashSet(randomText(10, 2000, 1, 1));
		double reputation = randomDouble();
		
		Document firstDoc = Document.of(firstBody, "", title, reputation, tags);
		Document secondDoc = Document.of(secondBody, "", title, reputation, tags);
		DocumentVectoriser vectorise = new StandardVectoriser();
		Vector first = vectorise.vectorise(firstDoc);
		Vector second = vectorise.vectorise(secondDoc);

		assertNotEquals("Documents with different body should have different vectors.", 
				first,
				second);
	}

	private String randomText(int minTokenLength, int maxTokenLength, int minTokens, int maxTokens) {
		StringBuffer result = new StringBuffer();
		int tokens = randomIntBetween(minTokens, maxTokens);
		for (int i = 0; i < tokens; i++) {
			result.append(randomAsciiOfLengthBetween(minTokenLength, maxTokenLength));
			result.append(" ");
		}
		return result.toString();
	}
	
	@Repeat(iterations = 10)
	@Test
	public void testNoTargetLeakage() {
		String body = randomText(10, 2000, 2, 100);;
		String title = randomText(10, 2000, 2, 100);;
		Set<String> tags = Sets.newHashSet(randomText(10, 2000, 1, 1));
		double reputation = randomDouble();
		
		Document firstDoc = Document.of(body, "first", title, reputation, tags);
		Document secondDoc = Document.of(body, "second", title, reputation, tags);
		
		DocumentVectoriser vectorise = new StandardVectoriser();
		Vector first = vectorise.vectorise(firstDoc);
		Vector second = vectorise.vectorise(secondDoc);
		assertEquals("The state field should not be taken into consideration when creating vectors.",
				first,
				second);
	}
}
