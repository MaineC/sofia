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
package de.isabeldrostfromm.sof.util;

import org.apache.mahout.math.SequentialAccessSparseVector;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.Vector.Element;

/**
 * Inspired by the Arrays, Collections etc. classes this class provides
 * common utitilities to deal with Mahout vectors.
 * 
 * TODO tests missing
 * */
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