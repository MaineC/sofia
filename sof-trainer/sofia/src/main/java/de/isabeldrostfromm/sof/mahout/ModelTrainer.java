package de.isabeldrostfromm.sof.mahout;

import java.io.IOException;

import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;

import de.isabeldrostfromm.sof.DocumentProvider;

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
