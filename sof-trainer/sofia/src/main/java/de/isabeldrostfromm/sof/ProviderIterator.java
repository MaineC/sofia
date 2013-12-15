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
package de.isabeldrostfromm.sof;

import java.util.Iterator;


/**
 * Iteration logic given a method that parses documents from an internal
 * document provider.
 * */
public abstract class ProviderIterator implements Iterator<Example> {

	protected Example parsed;
	protected abstract Example parse();

	@Override
	public boolean hasNext() {
		if (this.parsed != null) {
			return true;
		}

		this.parsed = parse();
		if (this.parsed != null)
			return true;

		return false;
	}

	@Override
	public Example next() {
		if (this.parsed == null) {
			this.parsed = parse();
		}
		Example current = this.parsed;
		this.parsed = null;
		return current;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"No removal of documents from search results.");
	}

}
