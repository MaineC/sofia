package de.isabeldrostfromm.sof;

import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "of")
@ToString
public class Document {
	@NonNull @Getter private String body;
	@NonNull @Getter private String title;
	@NonNull @Getter private double reputation;
	@NonNull @Getter private Set<String> tags;
}
