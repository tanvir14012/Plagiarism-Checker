# Plagiarism-Detector
Plagiarism detection of texts is closely associated with natural language processing methods such as term analysis, filtering, weighting, vector representation of document, similarity measurement etc in most cases. This application focuses on external plagiarism detection systems, having a collection of source documents and a suspected document, the task is to find signs of plagiarism in the suspected document.

Usage:
The user can add any number of reference text documents to check against a suspected document. There is only one option for suspected document, the user can either pick a suspected text document or put some text on the textbox. Upon query, the application will compute plagiarism in percentage and show the result to user for each combination of a reference document and the suspected text.


Supported Text Documents: 
The application supports PDF, Microsoft Word (docx) and raw text files.

Technique Summary:
-	Tokenization: Simple punctuation mark based tokenization.
-	Stemming: Lookup table, using Wordnet stemmer.
-	Weighting: TF-IDF weighting. IDF is computed from 100000 Wikipedia articles.
-	Vector Representation: Each document is represented by a vector containing tf-idf score of words, then words are stored in a bag containing document info and frequency.
-	Similarity Metric: Cosine similarity.

 Java APIs and libraries:
-	Apache OpenNLP
-	Apache POI
-	Apache PDFBox
-	JWI ( The MIT Java Wordnet Interface )
-	JavaFX (GUI)
 

![Alt text](https://github.com/tanvir14012/Plagiarism-Detector/blob/master/Screenshots/CAPD1.PNG)
![Alt text](https://github.com/tanvir14012/Plagiarism-Detector/blob/master/Screenshots/CAPD2.PNG)
![Alt text](https://github.com/tanvir14012/Plagiarism-Detector/blob/master/Screenshots/CAPD3.PNG)
 
