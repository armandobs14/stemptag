The **SInteliGIS Temporal Expression Tagger (stemptag)** is a Java-based system addressing the complete resolution of temporal expressions given over textual documents, through the usage of Machine Learning (ML) approaches. This resolution involves recognizing the temporal expressions, i.e. delimiting their occurrences over the textual documents, and normalizing the recognized expressions, i.e. explicitly associating the text chunks to calendar points or calendar intervals.

In the past, temporal expression - or timex - recognition has been attempted with both human-generated rules and ML-based approaches, with both working reasonably well. However, the timex normalization problem has mostly been addressed through the usage of human-made rules. For more information about previous works on handling temporal expressions in text, please refer to the [TIMEXPortal](http://www.timexportal.info) or the [TimeML](http://www.timeml.org/) websites.

Some publications describing the stemptag system include:

```
@inproceedings{Loureiro:2011:Machine,
 author = {Vitor Loureiro and Bruno Martins and Pavel Calado},
 title = {{A Machine Learning Method for Resolving Temporal References in Text}},
 booktitle = {Proceedings of the 15th Portuguese Conference on Artificial Intelligence},
 year = {2011},
 location = {Lisbon, Portugal}
}
```

The stemptag system is being developed in the context of the [SInteliGIS](https://sites.google.com/site/sinteligis/) project financed by the [Portuguese Foundation for Science and Technology (FCT)](http://www.fct.mctes.pt/) through project grant PTDC/EIA-EIA/109840/2009. You might want to check the full list of [software packages developed in the context of the SInteliGIS project](http://code.google.com/p/trecgeo/wiki/SInteliGISSoftwarePackages).

A **demonstration of the system was [made available online](http://stemptag.appspot.com/)** through the Google App Engine and the system is also available from the [Downloads](http://code.google.com/p/stemptag/downloads/list) page. See the [Wiki](http://code.google.com/p/stemptag/w/list) for more information and tutorials.


---


<img src='http://www.ist.utl.pt/files/media/media-kit/Logo_IST_color.jpg' height='40'><code>     </code><img src='http://algos.inesc-id.pt/~lazzari/old/inesc_id_logo.gif' height='40'><code>     </code><img src='http://alfa.fct.mctes.pt/logotipos/fct/FCT_Vcolor250x84.jpg' height='40'>

<a href='Hidden comment: 
<img src="http://code.google.com/appengine/images/appengine-noborder-120x30.gif">
'></a><br>
