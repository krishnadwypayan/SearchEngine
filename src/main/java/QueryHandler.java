import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.util.regex.Pattern;

public class QueryHandler {

    static int countPages;

    private static final Scanner scanner = new Scanner(System.in);

    static ArrayList<ArrayList<String>> wordMapSecondaryMap = new ArrayList<>();

    private static ArrayList<ArrayList<String>> docTitleSecondaryMap = new ArrayList<>();

    private static void initializeSecondaryMapForWordMap() {
        try {
            BufferedReader wordMapFileReader = new BufferedReader(new FileReader(WordMapFileHandler.WORD_MAP_SECONDARY_FILENAME));
            String line;
            while ((line = wordMapFileReader.readLine()) != null) {
                String[] lineContents = line.split(":");

                wordMapSecondaryMap.add(new ArrayList<>(Arrays.asList(lineContents)));
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
                countPages++;

                String[] lineContents = line.split(":");
                docTitleSecondaryMap.add(new ArrayList<>(Arrays.asList(lineContents)));
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private static String getDocTitle(String docId) {
        // (fileName, firstDocId, lastDocId) from docTitleSecondaryMap
        String docTitleFilename = "";
        for (ArrayList<String> arrayList : docTitleSecondaryMap) {
            // compare the docId with the firstDocId and secondDocId
            int low = Integer.parseInt(arrayList.get(1));
            int high = Integer.parseInt(arrayList.get(2));

            int docIdInt = Integer.parseInt(docId);

            if (low == docIdInt || high == docIdInt || (low < docIdInt && high > docIdInt)) {
                docTitleFilename = arrayList.get(0);
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

//        // Split wordMap file into multiple files.
//        WordMapFileHandler.splitWordMapFile(SearchEngineMain.WORD_MAP_FILE_NAME);
//
//        // Open each file and sort it internally.
//        WordMapFileHandler.sortSplitFiles(SearchEngineMain.SPLIT_WORD_MAP_PATH);
//
//        // Merge them into single file
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

        QueryResultsHandler.fieldPattern = Pattern.compile("[A-Za-z]:");
        QueryResultsHandler.booleanQueryPattern = Pattern.compile("\b and \b|\b or \b|\b not \b");


        while (true) {
            System.out.println("\nQuery: ");
            String query = scanner.nextLine();

            if (query.equalsIgnoreCase("exit")) {
                break;
            }

            ArrayList<String> results = null;
            long start = System.currentTimeMillis();

            QueryResultsHandler handler = new QueryResultsHandler();

            int queryType = handler.getQueryType(query);
            if (queryType == -1) {
                // field query
                results = handler.getQueryResultsFields(query);
            }
            else if (queryType == 0) {
                // boolean query
                results = handler.getQueryResultsBoolean(query);
            }
            else {
                // normal query
                results = handler.getQueryResultsNormal(query);
            }

            float queryTime = (System.currentTimeMillis() - start)/1000f;

            if (results != null) {
                Collections.reverse(results);

                for (int i = 0; i < Integer.min(10, results.size()); i++) {
                    if (results.get(i).equals("No Results Found")) {
                        results.clear();
                        break;
                    }
                    String docId = results.get(i).split(":")[0];
                    System.out.println("[" + (i + 1) + "] " + getDocTitle(docId));
                }

                System.out.println("Fetched [" + results.size() + "] results in: " + String.valueOf(queryTime) + "sec");
            }
            else {
                System.out.println("Fetched [0] results in: " + String.valueOf(queryTime) + "sec");
            }

        }

    }

}
