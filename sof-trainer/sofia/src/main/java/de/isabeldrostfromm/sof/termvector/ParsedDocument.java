package de.isabeldrostfromm.sof.termvector;

import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import org.apache.mahout.math.map.OpenObjectDoubleHashMap;

/**
 * Bean to represent a parsed document. This is useful if your data backend is capable
 * of returning your documents in Lucene TermVector format.
 * */
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "of")
@ToString
public class ParsedDocument {
    /** Body of the Stackoverflow posting (unfiltered)*/
    @NonNull @Getter private OpenObjectDoubleHashMap<String> body;
    /** State of the Stackoverflow thread (open, too specific, closed etc)*/
    @NonNull @Getter private String state;
    /** Thread title */
    @NonNull @Getter private OpenObjectDoubleHashMap<String> title;
    /** Reputation of poster when publishing the content */
    @NonNull @Getter private double reputation;
    /** Set of tags the poster provided */
    @NonNull @Getter private Set<String> tags;
}
