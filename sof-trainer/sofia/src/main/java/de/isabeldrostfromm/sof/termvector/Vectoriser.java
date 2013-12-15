package de.isabeldrostfromm.sof.termvector;

import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.map.OpenObjectDoubleHashMap;
import org.apache.mahout.vectorizer.encoders.StaticWordValueEncoder;

import de.isabeldrostfromm.sof.util.Vectors;

public class Vectoriser {

    public Vector vectorise(ParsedDocument doc) {
        Vector body = encode(doc.getBody(), de.isabeldrostfromm.sof.naive.Vectoriser.bodyCard);
        Vector title = encode(doc.getTitle(), de.isabeldrostfromm.sof.naive.Vectoriser.titleCard);
        return Vectors.append(body, title);
    }
    
    public Vector encode(OpenObjectDoubleHashMap<String> termVector, int card) {
        Vector vector = new SequentialAccessSparseVector(card);

        StaticWordValueEncoder encoder = new StaticWordValueEncoder("name");
        // TODO why on earth is the typical keySet missing???
        for (String term : termVector.keys()) {
            for (int i = 0; i < termVector.get(term); i++)
                encoder.addToVector(term, vector);
        }
        return vector;
    }
}
