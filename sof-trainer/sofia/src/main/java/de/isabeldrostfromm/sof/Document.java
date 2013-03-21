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
