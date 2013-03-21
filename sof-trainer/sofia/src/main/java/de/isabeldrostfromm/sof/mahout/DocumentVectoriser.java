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
