# Search Engine for Wikipedia

A complete search engine for the Wikipedia corpus(62GB) that gives search results in the form of Wikipedia page titles that relate to the given search words.

### The types of queries supported by the Search engine are:
*** Normal queries  : simple line queries that would search for each of the words of the line.
*** Field queries   : Fields include Title, Infobox, Body, Category, Links, and References of a Wikipedia page.
*** Boolean queries : Supported boolean operations include AND, OR, NOT on the words of the query.

### Index Creation
The index would be about 1/4th the size of the corpus (~16GB).
SearchEngineMain.java handles the process of index creation. To create an index on the wikipedia corpus, give the path for the corpus. Subsequently give the path for the folders where the files need to be placed. It is required to give separate folder paths for each of the index creation steps just like the paths stated in the file.
Subsequently, run the SearchEngineMain class. (Note: The index creation, merging of the index and splitting of the index into multiple files might take a lot of time.)

### Search For Query
* Normal Query should be like "what is in a name?"
* Field Query should be like "b:superhero c:cartoon i:superman r:man of steel".
	(Note: Fields to be in ["i" (infobox), "r" (references), "e" (external links), "c" (category), "t" (title), "b" (text body)])
* Boolean Query should be like "sachin AND dhoni OR kohli NOT world cup"
	(Note: Boolean operations must be in capital case like mentioned in the example query)

### Steps to run the Search Engine:
=> javac *.java
=> java QueryHandler

Enter the query when prompted for!