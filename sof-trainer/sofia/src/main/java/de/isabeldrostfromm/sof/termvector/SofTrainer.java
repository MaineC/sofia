package de.isabeldrostfromm.sof.termvector;

import org.apache.mahout.classifier.sgd.OnlineLogisticRegression;

import de.isabeldrostfromm.sof.ExampleProvider;
import de.isabeldrostfromm.sof.ModelTargets;
import de.isabeldrostfromm.sof.ModelTrainer;
import de.isabeldrostfromm.sof.Trainer;

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
