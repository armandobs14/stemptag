# Introduction #

The _SInteliGIS Temporal Expression Tagger_ is a Java-based system addressing the complete resolution of temporal expressions given over textual documents, through the usage of Machine Learning (ML) approaches. This resolution involves recognizing the temporal expressions, i.e. delimiting their occurrences over the textual documents, and normalizing the recognized expressions, i.e. explicitly associating the text chunks to calendar points or calendar intervals.

A web application for tagging temporal expressions in text, acting as a demonstrator for the system, is available at http://stemptag.appspot.com/

# Building from the source code #

The source code is available from the Mercurial repository. The Ant build file contains targets for compiling the source code, building models from [training data](TrainingData.md), building a web application, and deploying the application on a Tomcat servlet container.

Before compiling the source code, you need to download the required [external software libraries](SoftwareDependencies.md), and update the Ant build file with the path name to where the libraries are stored.

# Details about the algorithms #

Some publications detailing the algorithms behind the stemptag system include:

```
@inproceedings{Loureiro:2010:Machine,
 author = {Vitor Loureiro and Bruno Martins and Pavel Calado},
 title = {{A Machine Learning Method for Resolving Temporal References in Text}},
 booktitle = {Proceedings of the 15th Portuguese Conference on Artificial Intelligence},
 year = {2011},
 location = {Lisbon, Portugal}
}
```

For more information about the stemptag web application please contact the project members.