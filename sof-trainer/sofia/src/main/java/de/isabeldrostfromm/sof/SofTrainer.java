package de.isabeldrostfromm.sof;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.LuceneTextValueEncoder;
import org.elasticsearch.common.Strings;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import de.isabeldrostfromm.sof.util.Vectors;

/**
 * Utility to start training and testing in one go. Demonstrates document vectorisation with Mahout
 *
 * TODO add tests
 * 
 * TODO factor Mahout vectorisation out of this class
 * 
 * TODO add some interaction features to deal with off_topic etc. states.
 * */
public class SofTrainer {
	/** Cardinality of the vector portion to use for encoding posting bodies. */
	private static final int bodyCard = 1000000;
	/** Cardinality of the vector portion to use for encoding posting titles. */
	private static final int titleCard = 1000000;
	/** Cardinality of the vector portion to use for encoding posting tags. */
	private static final int tagCard = 200;
	/** Number of single double values to encode */
	private static final int doubles = 0;
	
	/** Field to predict */
	private static final String field = "open_status";
	/** Possible prediction results */
	private static final HashMap<String, Integer> states = new HashMap<String, Integer>();
	private static final String[] stateValues = {"open", "not_a_real_question", "not_constructive", "off_topic", "too_localized"};
	static {
		for (int i = 0; i < stateValues.length; i++)
			states.put(stateValues[i], new Integer(i));
	}
	
	/** Number of training examples to use */
	private static final int numTrain = 50 * stateValues.length;	
	/** Number of examples to use for testing */
	private static final int numTest = 50;
	
	/**
	 * First run a round of training (currently on the top-k documents returned by ES - 
	 * TODO switch to top-k according to reverse sorting by posting date
	 * - resulting in an unbalanced training set wrt. to posting status).
	 * 
	 * Second run one round of testing and output testing results.
	 * 
	 * Third store the resulting model in /tmp.
	 * */
	public static void main (String args[]) throws Exception {
		OnlineLogisticRegression logReg = new OnlineLogisticRegression(stateValues.length, bodyCard + titleCard + tagCard + doubles, new L1());

		DocumentProvider train = RESTProvider.negatedFilterInstance(field, "invalid_status_string", 0, numTrain);
		train(logReg, train);
		
		for (int i = 0; i < stateValues.length; i++) {
			DocumentProvider test = RESTProvider.filterInstance(field, stateValues[i], numTrain, numTest);
			test(logReg, test);
		}
		storeModel(logReg);	
	}
	
	/**
	 * Turn a document bean into a vector.
	 * @param document the document to turn in a vector.
	 * @return the resulting vector.
	 * */
	public static Vector vectorise(Document document) {
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

	/**
	 * Iterate over document provider and use the resulting documents for training.
	 * @param logReg the model to train on.
	 * @param positive iterator providing documents to train on
	 * */
	private static void train(OnlineLogisticRegression logReg,
			DocumentProvider positive) {
		Multiset<String> set = HashMultiset.create();
		for (Document document : positive) {
			set.add(document.getState());
			Vector instance = vectorise(document);  
			logReg.train(states.get(document.getState()), instance);
		}
		for (String key : set.elementSet()) {
			System.out.println("Saw " + set.count(key) + " documents of type " + key);
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

	/**
	 * Iterate on test set, gather classification statistics and output them.
	 * @param logReg the model to use for prediction
	 * @param negativeTest the iterator providing the test examples
	 * */
	private static void test(OnlineLogisticRegression logReg,
			DocumentProvider negativeTest) {
		int total = 0;
		int found = 0;
		String target = "";
		for (Document document : negativeTest) {
			target = document.getState();
			Vector instance = vectorise(document);
			Vector result = logReg.classify(instance);
			total++;
			int index = states.get(document.getState());
			if (index < (result.size() -1)
					&& result.get(states.get(document.getState())) > 0) 
				found++;
		}
		System.out.println("Of " + total + " we found " + found + " instances for label " + target);
	}
	
}
