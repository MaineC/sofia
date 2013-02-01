package de.isabeldrostfromm.sof;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.LuceneTextValueEncoder;
import org.elasticsearch.common.Strings;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import de.isabeldrostfromm.sof.util.Vectors;

public class SofTrainer {

	private static final int numTest = 50;
	
	private static final int bodyCard = 1000000;
	private static final int titleCard = 1000000;
	private static final int tagCard = 200;
	private static final int doubles = 0;
	
	private static final String field = "open_status";
	private static final HashMap<String, Integer> states = new HashMap<String, Integer>();
	private static final String[] stateValues = {"open", "not_a_real_question", "not_constructive", "off_topic", "too_localized"};

	private static final int numTrain = 550 * stateValues.length;			
	
	public static void main (String args[]) throws Exception {
		OnlineLogisticRegression logReg = new OnlineLogisticRegression(stateValues.length, bodyCard + titleCard + tagCard + doubles, new L1());

		for (int i = 0; i < stateValues.length; i++)
			states.put(stateValues[i], new Integer(i));

		DocumentProvider train = ESProvider.pagedInstance(field, "invalid_status_string", 0, numTrain);
		train(logReg, train);
		
		for (int i = 0; i < stateValues.length; i++) {
			DocumentProvider test = ESProvider.pagedInstance(field, stateValues[i], numTrain, numTest);
			test(logReg, test);
		}
		storeModel(logReg);	
	}

	public static Vector vectorise(Document document) {
		Vector body = luceneEncode(bodyCard, document.getBody());
		Vector title = luceneEncode(titleCard, document.getTitle());
		Vector tags = luceneEncode(tagCard, Strings.collectionToCommaDelimitedString(document.getTags()));
		//Vector reputation = Vectors.newSequentialAccessSparseVector(document.getReputation());

		return Vectors.append(body, title, tags);//, reputation);
	}
	
	public static Vector luceneEncode(int probes, String text) {
		LuceneTextValueEncoder encoder = new LuceneTextValueEncoder("sof");
		encoder.setAnalyzer(new StandardAnalyzer(Version.LUCENE_36));
		encoder.setProbes(probes);
		encoder.addText(text);
		Vector vector = new SequentialAccessSparseVector(probes);
		encoder.flush(1, vector);
		return vector;
	}

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

	private static void test(OnlineLogisticRegression logReg,
			DocumentProvider negativeTest) {
		int total = 0;
		int found = 0;
		for (Document document : negativeTest) {
			Vector instance = vectorise(document);
			Vector result = logReg.classify(instance);
			total++;
			if (result.get(states.get(document.getState())) > 0) 
				found++;
		}
		System.out.println("Of " + total + " we found " + found + " instances.");
	}
	
}
