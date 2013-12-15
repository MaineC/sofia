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
package de.isabeldrostfromm.sof.naive;

import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;

import de.isabeldrostfromm.sof.ExampleProvider;
import de.isabeldrostfromm.sof.ModelTargets;
import de.isabeldrostfromm.sof.ModelTrainer;
import de.isabeldrostfromm.sof.Trainer;

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
	private static final int numTrain = 50 * ModelTargets.STATEVALUES.length;	
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
		ModelTrainer trainer = new Trainer();

		ExampleProvider train = RESTProvider.negatedFilterInstance(field, "invalid_status_string", 0, numTrain);
		OnlineLogisticRegression model = trainer.train(train);

		for (int i = 0; i < ModelTargets.STATEVALUES.length; i++) {
			ExampleProvider test = RESTProvider.filterInstance(field, ModelTargets.STATEVALUES[i], numTrain, numTest);
			trainer.apply(model, test);
		}

		trainer.store(model);
	}
}
