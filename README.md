Sofia - Mahout/Elastic Search test bed
======================================

Single machine training
-----------------------

Text - Lucene analyzers - Mahout classifiers: The goal of the project is to
get familiar with both Elastic Search and the Mahout logistic regression
implementation and figure out to what extend ES can help getting an idea
about a set of unknown text documents (e.g. through facetting).

The project needs (a sample of) the StackOverflow dump as textual basis. It
reads the dump, posts it to a running ES instance using pyes. The documents
can then be retrieved, parsed and used as training input to the Mahout logistic
regression classifier.

Ultimately training and classification most likely should happen as part of the
indexing process. This is work for a future project.

Scaling out
-----------
As a second stage the project should show how classifier training would look
like in a pure Hadoop setup w/o ES.
