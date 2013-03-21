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

import java.io.IOException;

import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;

import de.isabeldrostfromm.sof.DocumentProvider;

/**
 * Classes implementing this interface encapsulate training a classification
 * model and using it for predicting document labels as well as storing
 * the model on disk.
 * */
public interface ModelTrainer {
	/**
	 * Given a document provider train a model.
	 *@param provider the provider of training documents
	 *@return a trained model
	 * */
	public OnlineLogisticRegression train(DocumentProvider provider);

	/** Given a model and document provider test the model performance. 
	 * @param model the model to test
	 * @param provider the provider to take test documents from
	 **/
	public void test(OnlineLogisticRegression model, DocumentProvider provider);
	
	/** Store the model to disk.
	 * @throws IOException  in case storing model fails*/
	public void store(OnlineLogisticRegression model) throws IOException;
}
