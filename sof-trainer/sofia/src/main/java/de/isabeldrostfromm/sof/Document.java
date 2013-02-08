package de.isabeldrostfromm.sof;

import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Bean to represent one document to train on.
 * 
 * Usual bean methods are generated through lombok framework.
 * */
@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "of")
@ToString
public class Document {
	/** Body of the Stackoverflow posting (unfiltered)*/
	@NonNull @Getter private String body;
	/** State of the Stackoverflow thread (open, too specific, closed etc)*/
	@NonNull @Getter private String state;
	/** Thread title */
	@NonNull @Getter private String title;
	/** Reputation of poster when publishing the content */
	@NonNull @Getter private double reputation;
	/** Set of tags the poster provided */
	// TODO there really should be an ordered view on this collection
	@NonNull @Getter private Set<String> tags;
}
