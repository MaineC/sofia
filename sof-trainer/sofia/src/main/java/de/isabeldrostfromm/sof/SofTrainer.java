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
package de.isabeldrostfromm.sof;

import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;

import de.isabeldrostfromm.sof.es.RESTProvider;
import de.isabeldrostfromm.sof.mahout.ModelTrainer;
import de.isabeldrostfromm.sof.mahout.StandardTrainer;

/**
 * Utility to start training and testing in one go. Demonstrates document vectorisation with Mahout
 *
 * TODO add tests
 * 
 * TODO add some interaction features to deal with off_topic etc. states.
 * */
public class SofTrainer {
	
	/** Field to predict */
	private static final String field = "open_status";
	/** Number of training examples to use */
	private static final int numTrain = 50 * StandardTrainer.STATEVALUES.length;	
	/** Number of examples to use for testing */
	private static final int numTest = 50;
	
	/**
	 * First run a round of training (currently on the top-k documents returned by ES - 
	 * - resulting in an unbalanced training set wrt. to posting status).
	 * 
	 * Second run one round of testing and output testing results.
	 * 
	 * Third store the resulting model in /tmp.
	 * */
	public static void main (String args[]) throws Exception {
		ModelTrainer trainer = new StandardTrainer();

		DocumentProvider train = RESTProvider.negatedFilterInstance(field, "invalid_status_string", 0, numTrain);
		OnlineLogisticRegression model = trainer.train(train);

		for (int i = 0; i < StandardTrainer.STATEVALUES.length; i++) {
			DocumentProvider test = RESTProvider.filterInstance(field, StandardTrainer.STATEVALUES[i], numTrain, numTest);
			trainer.apply(model, test);
		}

		trainer.store(model);
	}
}
