A Wikipedia Parser generating a [Darwin Core Archive](https://rs.tdwg.org/dwc/terms/guides/text/index.htm) 
for species pages using the [taxobox](https://en.wikipedia.org/wiki/Template:Taxobox) 
or [speciesbox](https://en.wikipedia.org/wiki/Template:Speciesbox/doc) template and their derivates. 
The parser focuses on the English, German, Spanish and French wikipedias currently 
and works on the [article xml dumps](https://dumps.wikimedia.org/backup-index.html)

[Multimedia](https://rs.gbif.org/extension/gbif/1.0/multimedia.xml), 
[vernacular names](https://rs.gbif.org/extension/gbif/1.0/vernacularname.xml) and 
[textual descriptions](https://rs.gbif.org/extension/gbif/1.0/description.xml) are extracted. 
Every section of a wiki page will become a distinct description record with the section title becoming the description "type". 


# How to run it
java -jar wikipedia.jar
Downloading and processing the entire english wikipedia takes a long time. 
Depending on your network and CPU expect the program to run for several days.

# Supported Wikitext Templates

## Taxon information
 * http://en.wikipedia.org/wiki/Template:Taxobox
 * http://de.wikipedia.org/wiki/Wikipedia:Taxoboxen
 * http://de.wikipedia.org/wiki/Wikipedia:Pal%C3%A4oboxen
 * http://en.wikipedia.org/wiki/Template:Automatic_taxobox/doc
 * http://en.wikipedia.org/wiki/Template:Speciesbox/doc
 * http://en.wikipedia.org/wiki/Template:Subspeciesbox/doc
 * http://en.wikipedia.org/wiki/Template:Infraspeciesbox/doc

For automatic taxonboxes the classification from the Taxonomy templates are scraped.

## Palaeo templates
 * http://simple.wikipedia.org/wiki/Template:Fossil_range/doc
 * http://en.wikipedia.org/wiki/Template:Long_fossil_range
 * http://en.wikipedia.org/wiki/Template:Geological_range

## List templates

 * http://en.wikipedia.org/wiki/Template:Species_list/doc
 * http://en.wikipedia.org/wiki/Template:Taxon_list
 * http://en.wikipedia.org/wiki/Template:Plainlist
 * http://en.wikipedia.org/wiki/Template:Flatlist
 * http://en.wikipedia.org/wiki/Template:Collapsible_list
 * http://en.wikipedia.org/wiki/Template:Listen

## Citation templates
 * http://en.wikipedia.org/wiki/Template:RFK6.1
 * http://en.wikipedia.org/wiki/Template:Cite
 * http://en.wikipedia.org/wiki/Template:Cite_journal
 * http://en.wikipedia.org/wiki/Template:Cite_book
 * http://en.wikipedia.org/wiki/Template:Cite_web
 * http://en.wikipedia.org/wiki/Template:Convert

## General templates
 * http://en.wikipedia.org/wiki/Template:Hybrid
