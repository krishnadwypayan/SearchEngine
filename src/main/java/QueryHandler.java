import org.javatuples.Triplet;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.util.*;

public class QueryHandler {

    private static final Scanner scanner = new Scanner(System.in);

    private static ArrayList<Triplet<String, String, String>> wordMapSecondaryMap = new ArrayList<>();

    private static ArrayList<Triplet<String, String, String>> docTitleSecondaryMap = new ArrayList<>();

    private static final int countPages = 17640866;

    private static void initializeSecondaryMapForWordMap() {
        try {
            BufferedReader wordMapFileReader = new BufferedReader(new FileReader(WordMapFileHandler.WORD_MAP_SECONDARY_FILENAME));
            String line;
            while ((line = wordMapFileReader.readLine()) != null) {
                String[] lineContents = line.split(":");
                String offsetFilename = lineContents[0];
                String firstWord = lineContents[1], lastWord = lineContents[2];

                wordMapSecondaryMap.add(new Triplet<>(firstWord, lastWord, offsetFilename));
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private static void initializeDocTitleMap() {
        try {
            BufferedReader wordMapFileReader = new BufferedReader(new FileReader(DocumentsTitleFileHandler.DOCUMENTS_TITLE_SECONDARY_INDEX));
            String line;
            while ((line = wordMapFileReader.readLine()) != null) {
                String[] lineContents = line.split(":");
                String offsetFilename = lineContents[0];
                String firstWord = lineContents[1], lastWord = lineContents[2];

                docTitleSecondaryMap.add(new Triplet<>(firstWord, lastWord, offsetFilename));
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param offsetFilename Name of the offsetFilename.
     * @param termId The termId that needs to be looked up for in the index file.
     * @return offset at which the index file will contain the postingsList/termId.
     */
    private static long getOffset(String offsetFilename, Long termId) {

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
    private static String getPostingsList(String term) {

        // Match the term with the firstWord and lastWord of each triplet in the secondary map to
        // get the name of the offset file from where we will get the termId for the given term.

        String wordMapFileName = "";

        for (Triplet<String, String, String> triplet : wordMapSecondaryMap) {
            int low = triplet.getValue0().compareTo(term);
            int high = triplet.getValue1().compareTo(term);

            if (low == 0 || high == 0 || (low < 0 && high > 0)) {
                wordMapFileName = triplet.getValue2();
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

    // Field queries would be of the form (t:abc i:pqrs r:xyz)
    private static ArrayList<String> getQueryFields(String query) {
        ArrayList<String> results = new ArrayList<>();

        String[] queryFields = query.split(" ");

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
                String postingsList = getPostingsList(token);

                if (postingsList.equals("$#$ No Results Found $#$")) {
                    results.add("No Results Found");
                    return results;
                }

                String[] docs = postingsList.split("\\|");
                int numDocs = docs.length;
                double idf = Math.log(((double) countPages / numDocs));

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

    private static String getDocTitle(String docId) {
        // (firstDocId, lastDocId, fileName) from docTitleSecondaryMap
        String docTitleFilename = "";
        for (Triplet<String, String, String> triplet : docTitleSecondaryMap) {
            // compare the docId with the firstDocId and secondDocId
            int low = Integer.parseInt(triplet.getValue0());
            int high = Integer.parseInt(triplet.getValue1());

            int docIdInt = Integer.parseInt(docId);

            if (low == docIdInt || high == docIdInt || (low < docIdInt && high > docIdInt)) {
                docTitleFilename = triplet.getValue2();
                break;
            }
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(docTitleFilename));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineContents = line.split(":", 2);
                if (lineContents[0].equals(docId)) {
                    return lineContents[1];
                }
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void main(String[] args) {

        // Split wordMap file into multiple files.
//        WordMapFileHandler.splitWordMapFile(SearchEngineMain.WORD_MAP_FILE_NAME);

        // Open each file and sort it internally.
//        WordMapFileHandler.sortSplitFiles(SearchEngineMain.SPLIT_WORD_MAP_PATH);

        // Merge them into single file
//        WordMapFileHandler wordMapFileHandler = new WordMapFileHandler();
//        wordMapFileHandler.merge(SearchEngineMain.SPLIT_WORD_MAP_PATH);

        // Split the merged file
//        WordMapFileHandler.splitWordMapFile(SearchEngineMain.SPLIT_WORD_MAP_PATH + "word_map_merged.txt");

//        System.out.println("Word Map initialized");

        // -------------------------------------------------------------------------------------------


        // Split docTitleMap file into multiple files
//        DocumentsTitleFileHandler.splitDocTitleFile(SearchEngineMain.DOCUMENT_TITLE_PATH);

        // Bring the contents of the secondaryMap for wordMap into memory.
        // We use an ArrayList of Triplets where each Triplet is of the form
        // (FirstWord, LastWord, offsetFilename)
        initializeSecondaryMapForWordMap();

        initializeDocTitleMap();


        while (true) {
            System.out.println("Query : ");
            String query = scanner.nextLine();

            if (query.equalsIgnoreCase("exit")) {
                break;
            }

            long start = System.currentTimeMillis();

            ArrayList<String> results = getQueryFields(query);
            System.out.println("Fetched " + results.size() + " results in: " + String.valueOf(System.currentTimeMillis() - start));

            Collections.reverse(results);
            for (int i = 0; i < Integer.min(10, results.size()) ; i++) {
                if (results.get(i).equals("No Results Found")) {
                    System.out.println(results.get(i));
                    break;
                }
                String docId = results.get(i).split(":")[0];
                System.out.println(getDocTitle(docId));
            }

        }

    }

}
