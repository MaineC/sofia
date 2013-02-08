package de.isabeldrostfromm.sof.mahout;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.LuceneTextValueEncoder;
import org.elasticsearch.common.Strings;

import de.isabeldrostfromm.sof.Document;
import de.isabeldrostfromm.sof.util.Vectors;

/**
 * Vectorisation based on LuceneTextValueEncoder for body, title and tags.
 * */
public class StandardVectoriser implements DocumentVectoriser {
	/** Cardinality of the vector portion to use for encoding posting bodies. */
	private static final int bodyCard = 1000000;
	/** Cardinality of the vector portion to use for encoding posting titles. */
	private static final int titleCard = 1000000;
	/** Cardinality of the vector portion to use for encoding posting tags. */
	private static final int tagCard = 200;
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
	@Override
	public Vector vectorise(Document document) {
		Vector body = luceneEncode(bodyCard, document.getBody());
		Vector title = luceneEncode(titleCard, document.getTitle());
		Vector tags = luceneEncode(tagCard, Strings.collectionToCommaDelimitedString(document.getTags()));
		//Vector reputation = Vectors.newSequentialAccessSparseVector(document.getReputation());

		return Vectors.append(body, title, tags);//, reputation);
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
