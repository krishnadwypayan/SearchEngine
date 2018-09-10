import java.io.BufferedReader;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QueryResultsHandler {
    
    static Pattern fieldPattern;
    
    static Pattern booleanQueryPattern;
    
    /**
    * This method will return the type of the query according to normal, boolean 
    * or fields query.
    * -1 : field query
    * 0 : boolean query
    * 1 : normal query
    */
    public int getQueryType(String query) {
        if (fieldPattern.matcher(query).find()) {
            return -1;
        }
        else if (booleanQueryPattern.matcher(query).find()) {
            return 0;
        }
        
        return 1;
    }

    /**
     * @param offsetFilename Name of the offsetFilename.
     * @param termId The termId that needs to be looked up for in the index file.
     * @return offset at which the index file will contain the postingsList/termId.
     */
    private long getOffset(String offsetFilename, Long termId) {

        long offset = -1, prevOffset = -1;
        int offsetLineCount = 0;

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(offsetFilename));

            String offsetLine;
            while ((offsetLine = bufferedReader.readLine()) != null) {
                String[] offsetLineContents = offsetLine.split(":");
                offset = Long.parseLong(offsetLineContents[1]);

                if (termId == Long.parseLong(offsetLineContents[0])) {
                    break;
                }

                offsetLineCount++;
                prevOffset = offset;
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        return (offsetLineCount == 0) ? 0 : prevOffset;
    }

    /**
     * For a given term, we first get the termId from the wordMap.
     * Then we get the postingsList from the invertedIndex.
     *
     * To get the postingsList, we need to first go to the secondaryIndex to fetch the offsetsFile.
     * From the offsetsFile, we will get the postingsList stored in the corresponding indexFile by
     * jumping to the line directly in which the word is stored.
     *
     * @param term given term, we fetch the postingsList for it.
     */
    private String getPostingsList(String term) {

        // Match the term with the firstWord and lastWord of each triplet in the secondary map to
        // get the name of the offset file from where we will get the termId for the given term.

        String wordMapFileName = "";

        for (ArrayList<String> arrayList : QueryHandler.wordMapSecondaryMap) {
            int low = arrayList.get(1).compareTo(term);
            int high = arrayList.get(2).compareTo(term);

            if (low == 0 || high == 0 || (low < 0 && high > 0)) {
                wordMapFileName = arrayList.get(0);
                break;
            }
        }

        String postingsList = "$#$ No Results Found $#$";

        // Secondary index will give the name of the word_map split file that will contain
        // the term:termId. From the word_map file, find the termId of the term.
        long termId = -1;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(wordMapFileName));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineContents = line.split(":");
                if (lineContents[0].equals(term)) {
                    termId = Long.parseLong(lineContents[1]);
                    break;
                }
            }
            bufferedReader.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        if (termId == -1) {
            return postingsList;
        }

        // After fetching the termId, we have to look into the secondaryMap of the offsetFiles of the
        // primary index that we created for the corpus. From the secondaryMap file, get the name of
        // the offset file.
        String offsetFilename = "";
        String secondaryFileName = SearchEngineMain.SECONDARY_INDEX_PATH + "secondary_index.txt";
        try {
            BufferedReader bufferedReaderForOffset = new BufferedReader(new FileReader(secondaryFileName));

            String secondaryIndexLine;
            while ((secondaryIndexLine = bufferedReaderForOffset.readLine()) != null) {
                String[] lineContents = secondaryIndexLine.split(":");

                String[] lineContentsOffsets = lineContents[1].split("-");
                int low = Integer.parseInt(lineContentsOffsets[0]);
                int high = Integer.parseInt(lineContentsOffsets[1]);

                if (termId == low || termId == high || termId > low && termId < high) {
                    offsetFilename = lineContents[0];
                    break;
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        // From the offsetFilename, we get the offset where the termId's postingsList is stored.
        // Pass the termId as a String instead of long to getOffset().
        // offsetFilename is like : /Volumes/KrishnaDwypayan_HD/IIIT/IRE/Offsets/offset_(count).txt
        // indexFilename will be like : /Volumes/KrishnaDwypayan_HD/IIIT/IRE/Index Split Files/(count).txt
        long offset = getOffset(offsetFilename, termId);
        String indexFilename = SearchEngineMain.SPLIT_INDEX_PATH + offsetFilename.split("offset_")[1];

        // Get the postingsList from the index file for the termId at the found offset
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(indexFilename, "r");
            if (offset != 0) {
                randomAccessFile.seek(offset);
            }
            String postingsListLine = randomAccessFile.readLine();

            if (postingsListLine != null) {
                postingsList = postingsListLine.split(":")[1];
            }

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        return postingsList;

    }

    // Method for sorting the TreeMap based on values
    private static <K, V extends Comparable<V>> Map<K, V> sortByValues(final Map<K, V> map) {
        Comparator<K> valueComparator = new Comparator<K>() {
                    public int compare(K k1, K k2) {
                        int compare = map.get(k1).compareTo(map.get(k2));
                        if (compare == 0)
                            return 1;
                        else
                            return compare;
                    }
                };

        Map<K, V> sortedByValues = new TreeMap<K, V>(valueComparator);
        sortedByValues.putAll(map);
        return sortedByValues;
    }

    // Field queries would be of the form "t:abc def i:pqrs tuv r:xyz"
    public ArrayList<String> getQueryResultsFields(String query) {
        ArrayList<String> results = new ArrayList<>();
        
        Matcher tokenMatcher = fieldPattern.matcher(query);
        ArrayList<String> fieldsList = new ArrayList<>();
        
        while (tokenMatcher.find()) {
            fieldsList.add(tokenMatcher.group());
        }
        
        String[] textTokens = query.split("[A-Za-z]:");
        String[] queryFields = new String[fieldsList.size()];

        int i_ = 0;
        for (int i = 1; i < textTokens.length; i++) {
            if (i_ < fieldsList.size()) {
                String q = fieldsList.get(i_) + textTokens[i];
                queryFields[i_] = q;
                i_++;
            }
        }

        // queryFields now has the query as {"t:abc", "i:pqrs", "r:xyz"}
        // For every entity in queryFields, find the respective documents and find the intersection
        // of the documents retrieved.

		HashSet<Integer> allDocsSet = new HashSet<>();
		ArrayList<HashMap<Integer, Double>> allDocs_tfidf_List = new ArrayList<>();

        for (String queryField : queryFields) {

			HashMap<Integer, Double> wordOccurences = new HashMap<>();

            String[] queryElement = queryField.trim().split(":");
			String tag = queryElement[0];
			String term = queryElement[1];

			ArrayList<String> termTokens = DocumentParser.tokenize(term);

			// Get the postingsList for the term
			// Calculate the tf-idf for each of the entities in postingsList

            for (String token : termTokens) {
                if (token.equals("")) {
                    continue;
                }

                String postingsList = getPostingsList(token);

                if (postingsList.equals("$#$ No Results Found $#$")) {
                    results.add("No Results Found");
                    return results;
                }

                String[] docs = postingsList.split("\\|");
                int numDocs = docs.length;
                double idf = Math.log(((double) QueryHandler.countPages / numDocs));

                for (String doc : docs) {
                    // If the doc contains the tag, then only is it relevant
                    if (!doc.contains(tag)) {
                        continue;
                    }

                    // Split the doc by "-" to get (docId, termFreq, postingsList)
                    String[] docElements = doc.split("-");

                    double tf_idf = Integer.parseInt(docElements[1]) * idf;

                    wordOccurences.put(Integer.parseInt(docElements[0]), tf_idf);
                    allDocsSet.add(Integer.parseInt(docElements[0]));
                }

                allDocs_tfidf_List.add(wordOccurences);
            }

        }				

		// Find the intersection of all the docs in each doc_tfidf_Map and rank the results
        TreeMap<Integer, Double> resultsMap = new TreeMap<>();

        for (Integer docId : allDocsSet) {
            boolean existsFlag = true;
            double tfidf = 0;
            for (HashMap<Integer, Double> map : allDocs_tfidf_List) {
                if (!map.containsKey(docId)) {
                    existsFlag = false;
                    break;
                }
                tfidf = Double.max(tfidf, map.get(docId));
            }

            if (existsFlag) {
                resultsMap.put(docId, tfidf);
            }
        }

        Map sortedResultsMap = sortByValues(resultsMap);
        Set set = sortedResultsMap.entrySet();
        Iterator i = set.iterator();

        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            results.add(me.getKey() + ":" + me.getValue());
        }

        return results;

    }
    
    // Normal queries will be of the form "abc def pqrs xyz"
    public ArrayList<String> getQueryResultsNormal(String query) {
        ArrayList<String> results = new ArrayList<>();

        HashSet<Integer> allDocsSet = new HashSet<>();
		ArrayList<HashMap<Integer, Double>> allDocs_tfidf_List = new ArrayList<>();
        
        String[] queryTerms = query.split(" ");

        for (String term : queryTerms) {
			HashMap<Integer, Double> wordOccurences = new HashMap<>();
			ArrayList<String> termTokens = DocumentParser.tokenize(term);

			// Get the postingsList for the term
			// Calculate the tf-idf for each of the entities in postingsList
            for (String token : termTokens) {

                if (token.equals("")) {
                    continue;
                }

                String postingsList = getPostingsList(token);

                if (postingsList.equals("$#$ No Results Found $#$")) {
                    results.add("No Results Found");
                    return results;
                }

                String[] docs = postingsList.split("\\|");
                int numDocs = docs.length;
                double idf = Math.log(((double) QueryHandler.countPages / numDocs));

                for (String doc : docs) {
                    // Split the doc by "-" to get (docId, termFreq, postingsList)
                    String[] docElements = doc.split("-");

                    double tf_idf = Integer.parseInt(docElements[1]) * idf;

                    wordOccurences.put(Integer.parseInt(docElements[0]), tf_idf);
                    allDocsSet.add(Integer.parseInt(docElements[0]));
                }

                allDocs_tfidf_List.add(wordOccurences);
            }

        }				

		// Find the intersection of all the docs in each doc_tfidf_Map and rank the results
        TreeMap<Integer, Double> resultsMap = new TreeMap<>();

        for (Integer docId : allDocsSet) {
            boolean existsFlag = true;
            double tfidf = 0;
            for (HashMap<Integer, Double> map : allDocs_tfidf_List) {
                if (!map.containsKey(docId)) {
                    existsFlag = false;
                    break;
                }
                tfidf = Double.max(tfidf, map.get(docId));
            }

            if (existsFlag) {
                resultsMap.put(docId, tfidf);
            }
        }

        Map sortedResultsMap = sortByValues(resultsMap);
        Set set = sortedResultsMap.entrySet();
        Iterator i = set.iterator();

        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            results.add(me.getKey() + ":" + me.getValue());
        }

        return results;

    }

    private TreeMap<Integer, Double> evaluateBooleanQuery(HashMap<Integer, Double> firstMap,
                                      HashMap<Integer, Double> secondMap,
                                      String boolExp) {

        TreeMap<Integer, Double> result = new TreeMap<>();

        switch (boolExp) {
            case "and":

                for (Integer docId : firstMap.keySet()) {
                    if (secondMap.keySet().contains(docId)) {
                        result.put(docId, Double.max(firstMap.get(docId), secondMap.get(docId)));
                    }
                }

                break;
            case "or":

                result.putAll(firstMap);

                for (Integer docId : secondMap.keySet()) {
                    if (result.keySet().contains(docId)) {
                        result.put(docId, Double.max(secondMap.get(docId), result.get(docId)));
                    }
                    else {
                        result.put(docId, secondMap.get(docId));
                    }
                }

                break;
            case "not":

                for (Integer docId : firstMap.keySet()) {
                    if (!secondMap.keySet().contains(docId)) {
                        result.put(docId, firstMap.get(docId));
                    }
                }

                break;
            default:

                break;
        }

        return result;
    }
    
    // Boolean queries will be of the form "abc and pqrs or xyz"
    public ArrayList<String> getQueryResultsBoolean(String query) {
        ArrayList<String> results = new ArrayList<>();

        Matcher booleanMatcher = booleanQueryPattern.matcher(query);

        ArrayList<String> boolExpressions = new ArrayList<>();
        while (booleanMatcher.find()) {
            boolExpressions.add(booleanMatcher.group());
        }

        String[] queryTerms = query.split("\b and \b|\b or \b|\b not \b");

        // Store each queryTerm's postingsList in this list and then pick two
        // postingsList at a time from this list and apply the boolean condition.
        ArrayList<HashMap<Integer, Double>> queryTermPostingsList = new ArrayList<>();

        for (String queryTerm : queryTerms) {
            if (queryTerm.equals("")) {
                continue;
            }

            String postingsList = getPostingsList(queryTerm);

            if (postingsList.equals("$#$ No Results Found $#$")) {
                results.add("No Results Found");
                return results;
            }

            String[] docs = postingsList.split("\\|");
            int numDocs = docs.length;
            double idf = Math.log(((double) QueryHandler.countPages / numDocs));

            HashMap<Integer, Double> docsList = new HashMap<>();

            for (String doc : docs) {
                // Split the doc by "-" to get (docId, termFreq, postingsList)
                String[] docElements = doc.split("-");

                double tf_idf = Integer.parseInt(docElements[1]) * idf;
                docsList.put(Integer.parseInt(docElements[0]), tf_idf);
            }

            queryTermPostingsList.add(docsList);
        }

        TreeMap<Integer, Double> boolExpEvalResult = null;
        int boolExpressionsIdx = 0;
        for (int i = 0; i < queryTermPostingsList.size(); i++) {
            String boolExp = boolExpressions.get(boolExpressionsIdx);
            boolExpressionsIdx++;

            if (boolExpEvalResult == null) {
                HashMap<Integer, Double> firstMap = queryTermPostingsList.get(i);
                HashMap<Integer, Double> secondMap = queryTermPostingsList.get(i+1);
                i++;

                boolExpEvalResult = evaluateBooleanQuery(firstMap, secondMap, boolExp);
            }
            else {
                boolExpEvalResult = evaluateBooleanQuery(new HashMap<>(boolExpEvalResult),
                                                            queryTermPostingsList.get(i), boolExp);
            }
        }

        Map sortedResultsMap = sortByValues(boolExpEvalResult);
        Set set = sortedResultsMap.entrySet();
        Iterator i = set.iterator();

        while(i.hasNext()) {
            Map.Entry me = (Map.Entry)i.next();
            results.add(me.getKey() + ":" + me.getValue());
        }

        return results;
    }

}
