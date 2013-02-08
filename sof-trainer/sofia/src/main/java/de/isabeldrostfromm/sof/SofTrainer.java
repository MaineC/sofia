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
 * TODO factor Mahout vectorisation out of this class
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
	 * TODO switch to top-k according to reverse sorting by posting date
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
			trainer.test(model, test);
		}

		trainer.store(model);
	}
}
