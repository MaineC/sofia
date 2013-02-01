package de.isabeldrostfromm.sof.util;

import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;

public class Vectors {
	
	/**
	 * Appends two vectors directly after one another, leaving all non set elements zero.
	 * */
	public static Vector append(Vector... vectors) {
		int totalSize = 0;
		for (Vector vec : vectors) {
			totalSize += vec.size();
		}

		Vector result = new SequentialAccessSparseVector(totalSize);
		result.assign(0);

		int lastIndex = 0;
		for (Vector vector : vectors) {
			for (Element elem : vector) {
				result.setQuick(lastIndex + elem.index(), elem.get());
			}
			lastIndex += vector.size();
		}
		return result;
	}
	
	/**
	 * Creates a new SequentialSparseAccessVector and assigns the given values one after another to it.
	 * */
	public static SequentialAccessSparseVector newSequentialAccessSparseVector(double... ds) {
		SequentialAccessSparseVector result = new SequentialAccessSparseVector(ds.length);
		for (int i = 0; i < ds.length; i++) {
			result.setQuick(i, ds[i]);
		}
		return result;
	}
}