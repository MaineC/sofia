package de.isabeldrostfromm.sof;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;
import org.apache.mahout.vectorizer.encoders.LuceneTextValueEncoder;
import org.elasticsearch.common.Strings;

public class SofTrainer {

	private static final int trainPos = 550;
	private static final int trainNeg = 550;
	private static final int testPos = 50;
	private static final int testNeg = 50;
	
	private static final int bodyCard = 1000000;
	private static final int titleCard = 1000000;
	private static final int tagCard = 200;
	private static final int doubles = 0;
	
	private static final int negative = 0;
	private static final int positive = 1;
	
	public static void main (String args[]) throws Exception {
		OnlineLogisticRegression logReg = new OnlineLogisticRegression(2, bodyCard + titleCard + tagCard + doubles, new L1());

		DocumentProvider positiveTrain = ESProvider.pagedInstance(true, 0, trainPos);
		DocumentProvider negativeTrain = ESProvider.pagedInstance(false, 0, trainNeg);
		train(logReg, positiveTrain, positive);
		train(logReg, negativeTrain, negative);
			
		DocumentProvider negativeTest = ESProvider.pagedInstance(false, trainNeg, testNeg);
		int zeroCounterNeg = test(logReg, negativeTest);
		DocumentProvider positiveTest = ESProvider.pagedInstance(true, trainPos, testPos);
		int zeroCounterPos = test(logReg, positiveTest);

		System.out.println("Neg zeros: " + zeroCounterNeg + " Pos zeros: " + zeroCounterPos);
		storeModel(logReg);	
	}

	public static Vector vectorise(Document document) {
		Vector body = Vectors.luceneEncode(bodyCard, document.getBody());
		Vector title = Vectors.luceneEncode(titleCard, document.getTitle());
		Vector tags = Vectors.luceneEncode(tagCard, Strings.collectionToCommaDelimitedString(document.getTags()));
		//Vector reputation = Vectors.newSequentialAccessSparseVector(document.getReputation());

		return Vectors.append(body, title, tags);//, reputation);
	}
	
	private static class Vectors {
		public static Vector luceneEncode(int probes, String text) {
			LuceneTextValueEncoder encoder = new LuceneTextValueEncoder("sof");
			encoder.setAnalyzer(new StandardAnalyzer(Version.LUCENE_36));
			encoder.setProbes(probes);
			encoder.addText(text);
			Vector vector = new SequentialAccessSparseVector(probes);
			encoder.flush(1, vector);
			return vector;
		}
		
		/**
		 * Appends two vectors directly after one another, leaving all non set elements zero.
		 * */
		public static Vector append(Vector... vectors) {
			int totalSize = 0;
			for (Vector vec : vectors) {
				totalSize += vec.size();
			}

			Vector result = new SequentialAccessSparseVector(totalSize);
			result.assign(0);

			int lastIndex = 0;
			for (Vector vector : vectors) {
				for (Element elem : vector) {
					result.setQuick(lastIndex + elem.index(), elem.get());
				}
				lastIndex += vector.size();
			}
			return result;
		}
		
		/**
		 * Creates a new SequentialSparseAccessVector and assigns the given values one after another to it.
		 * */
		public static SequentialAccessSparseVector newSequentialAccessSparseVector(double... ds) {
			SequentialAccessSparseVector result = new SequentialAccessSparseVector(ds.length);
			for (int i = 0; i < ds.length; i++) {
				result.setQuick(i, ds[i]);
			}
			return result;
		}
	}
	
	private static void storeModel(OnlineLogisticRegression logReg)
			throws FileNotFoundException, IOException {
		File outFile = new File("/tmp/softrainer.model");
		DataOutputStream stream = new DataOutputStream(new FileOutputStream(outFile));
		DataOutput output = stream; 
		logReg.write(output);
		stream.flush();
		stream.close();
	}

	private static int test(OnlineLogisticRegression logReg,
			DocumentProvider negativeTest) {
		int zeroCounterNeg = 0;
		for (Document document : negativeTest) {
			Vector instance = vectorise(document);
			Vector result = logReg.classify(instance);
			if (Math.round(result.get(0) * 1000000000) > 0) zeroCounterNeg++;
		}
		return zeroCounterNeg;
	}

	private static void train(OnlineLogisticRegression logReg,
			DocumentProvider positive, int target) {
		for (Document document : positive) {
			Vector instance = vectorise(document);  
			logReg.train(target, instance);
		}
	}
	
}
