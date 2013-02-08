package de.isabeldrostfromm.sof;

import java.util.Iterator;

/**
 * Iteration logic given a method that parses documents from an internal
 * document provider.
 * */
public abstract class ProviderIterator implements Iterator<Document> {

	protected Document parsed;
	
	protected abstract Document parse();

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
	public Document next() {
		if (this.parsed == null) {
			this.parsed = parse();
		}
		Document current = this.parsed;
		this.parsed = null;
		return current;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"No removal of documents from search results.");
	}

}
