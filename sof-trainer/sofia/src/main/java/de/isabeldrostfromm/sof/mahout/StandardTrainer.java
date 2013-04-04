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
package de.isabeldrostfromm.sof.mahout;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.mahout.classifier.sgd.L1;
import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;
import org.apache.mahout.math.Vector;
import org.apache.mahout.vectorizer.encoders.LuceneTextValueEncoder;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

import de.isabeldrostfromm.sof.Document;
import de.isabeldrostfromm.sof.DocumentProvider;

/**
 * Implements training an {@link OnlineLogisticRegression} model based on
 * a document turning all text into vector fractions via the {@link LuceneTextValueEncoder}
 * provided by Mahout
 * 
 * TODO fix logging
 * TODO fix documentation
 * */
public class StandardTrainer implements ModelTrainer {

	/** Possible prediction results */
	public static final HashMap<String, Integer> STATES = new HashMap<String, Integer>();
	public static final List<String> INDECES = new ArrayList<String>();
	public static final String[] STATEVALUES = {"open", "not a real question", "not constructive", "off topic", "too localized"};
	static {
		for (int i = 0; i < STATEVALUES.length; i++) {
			STATES.put(STATEVALUES[i], new Integer(i));
			INDECES.add(STATEVALUES[i]);
		}
	}
	/** Vectoriser to turn documents into vectors. */
	private final StandardVectoriser v = new StandardVectoriser();

	@Override
	public OnlineLogisticRegression train(DocumentProvider provider) {
		OnlineLogisticRegression logReg = new OnlineLogisticRegression(
				STATEVALUES.length, StandardVectoriser.getCardinality(), new L1());

		Multiset<String> set = HashMultiset.create();
		for (Document document : provider) {
			set.add(document.getState());
			Vector instance = v.vectorise(document);  
			logReg.train(STATES.get(document.getState()), instance);
		}

		return logReg;
	}

	@Override
	public List<String> apply(OnlineLogisticRegression model, DocumentProvider provider) {
		List<String> result = new ArrayList<String>();
		for (Document document : provider) {
			Vector instance = v.vectorise(document);
			Vector labeled = model.classify(instance);
			double max = -1;
			int maxIndex = -1;
			for (Vector.Element element : labeled) {
				if (element.get() > max) {
					max = element.get();
					maxIndex = element.index();
				}
			}
			result.add(INDECES.get(maxIndex));
		}
		return result;
	}

	@Override
	public void store(OnlineLogisticRegression model) throws IOException {
		File outFile = new File("/tmp/softrainer.model");
		DataOutputStream stream = new DataOutputStream(new FileOutputStream(outFile));
		DataOutput output = stream; 
		model.write(output);
		stream.flush();
		stream.close();
	}

}
