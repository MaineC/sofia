package de.isabeldrostfromm.sof.mahout;

import org.apache.mahout.math.Vector;

import de.isabeldrostfromm.sof.Document;

/**
 * Given a {@link Document} implemenations of this interface should
 * return a vector in Mahout format to use for classification.
 * */
public interface DocumentVectoriser {
	
	/** Returns a vector based on the document.*/
	public Vector vectorise(Document document);
}
