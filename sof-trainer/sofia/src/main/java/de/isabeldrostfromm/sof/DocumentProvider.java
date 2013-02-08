package de.isabeldrostfromm.sof;

import java.io.Closeable;

/**
 * Implement this interface in order to provide an implementation for
 * another document provider backend other than the ES REST and ES Java
 * API backends.
 * */
public interface DocumentProvider extends Iterable<Document>, Closeable {

}
