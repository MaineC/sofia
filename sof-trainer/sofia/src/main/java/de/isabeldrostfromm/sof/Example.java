package de.isabeldrostfromm.sof;

import org.apache.mahout.math.Vector;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@RequiredArgsConstructor(staticName = "of")
@ToString
public class Example {
    /**Vectorised version of a document to train on.*/
    @NonNull @Getter private Vector vector;
    /**Prediction target - in case of application set to empty string.*/
    @NonNull @Getter private String state;
}
