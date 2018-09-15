import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SearchEngineMain {

//    private static final String SMALL_WIKI_FILE_NAME = "/Users/krishnadwypayan/Documents/IIIT/IRE/WikipediaSearchEngine/res/small_wiki.xml";

//    private static final String WIKI_FILE_NAME = "/Users/krishnadwypayan/Documents/IIIT/IRE/WikipediaSearchEngine/res/wiki-search-small.xml";

    private static final String WIKI_FILE_NAME_PATH = "/Volumes/KrishnaDwypayan_HD/IIIT/IRE/full_wiki.xml";

    static final String OUTPUT_FILE_PATH = "/Volumes/KrishnaDwypayan_HD/IIIT/IRE/Output/";

    static final String MERGE_INDEX_PATH = "/Volumes/KrishnaDwypayan_HD/IIIT/IRE/Index/";

    static final String SPLIT_INDEX_PATH = "/Volumes/KrishnaDwypayan_HD/IIIT/IRE/Index Split Files/";

    static final String OFFSETS_PATH = "/Volumes/KrishnaDwypayan_HD/IIIT/IRE/Offsets/";

    static final String SECONDARY_INDEX_PATH = "/Volumes/KrishnaDwypayan_HD/IIIT/IRE/Secondary Indexes/";

    static final String WORD_MAP_FILE_NAME = "/Volumes/KrishnaDwypayan_HD/IIIT/IRE/word_map.txt";

    static final String SPLIT_WORD_MAP_PATH = "/Volumes/KrishnaDwypayan_HD/IIIT/IRE/Word Map Split Files/";

    static final String WORD_MAP_FILES = "/Volumes/KrishnaDwypayan_HD/IIIT/IRE/Word Map Files/";

    static final String DOCUMENT_TITLE_PATH = "/Volumes/KrishnaDwypayan_HD/IIIT/IRE/doc_title_map.txt";

    static final String DOC_TITLE_SPLIT_FILES = "/Volumes/KrishnaDwypayan_HD/IIIT/IRE/Doc Title Split Files/";

    // This will maintain the level where the index files are to be merged
    static int level;

    // this is the count of all the pages in the corpus (To be used for calculating tf-idf)
    static int countPages;

    private static HashMap<String, Pattern> regexPatterns;

    static BufferedWriter bufferedWriterForDocTitleMap;

    private static void makePatterns() {

        regexPatterns = new HashMap<>();

        Pattern regex_external_links = Pattern.compile("==External links==.*?\\[\\[Category", Pattern.DOTALL);
        Pattern regex_infobox = Pattern.compile("\\{\\{Infobox.*?\\}\\}", Pattern.DOTALL);
        Pattern regex_references = Pattern.compile("== ?References ?==(.*?)==", Pattern.DOTALL);
        Pattern regex_category = Pattern.compile("\\[\\[Category:.*\\]\\]?", Pattern.DOTALL);

        regexPatterns.put("ext_links", regex_external_links);
        regexPatterns.put("infobox", regex_infobox);
        regexPatterns.put("references", regex_references);
        regexPatterns.put("category", regex_category);

    }

    public static void main(String...args) throws IOException {

        long start = System.currentTimeMillis();

         bufferedWriterForDocTitleMap = new BufferedWriter(new FileWriter(DOCUMENT_TITLE_PATH));

        makePatterns();

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        try {

            SAXParser saxParser = saxParserFactory.newSAXParser();
            WikiDocumentHandler handler = new WikiDocumentHandler(regexPatterns);
            saxParser.parse(WIKI_FILE_NAME_PATH, handler);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
//
        // The last set of files need to be written to an index file
        InvertedIndex.writeInvertedIndex(OUTPUT_FILE_PATH + "output" + WikiDocumentHandler.outputFileCount + ".txt");
        InvertedIndex.invertedIndex.clear();
        System.out.println(SearchEngineMain.countPages);
//
        // Close the bufferedWriterForDocTitleMap
        bufferedWriterForDocTitleMap.close();

        // Create the wordMap file
        InvertedIndex.writeWordMapToFile(WORD_MAP_FILE_NAME);

        // Merge the index files created into a single index file
        MergeInvertedIndex mergeInvertedIndex = new MergeInvertedIndex();
        mergeInvertedIndex.merge(OUTPUT_FILE_PATH);
        MergeInvertedIndex.mergedIndexCount = 0;
        level++;
//
        // Merge the merged files
        mergeInvertedIndex.merge(MERGE_INDEX_PATH);

         // Split the merged index file into multiple files and create offsets for each index
         // At index 0, we have the .DS_Store file
        String indexFilePath = null;

        File[] outputDirFileNames = new File(MERGE_INDEX_PATH).listFiles();
        for (File file : outputDirFileNames) {
            if (file.getName().contains(".DS_Store")) {
                continue;
            }
            indexFilePath = file.getPath();
            break;
        }

        // IndexFileHandler handles the splitting of merged index, creates the offset for each
        // smaller index file and subsequently creates a secondary index for the offset files.
        IndexFileHandler indexFileHandler = new IndexFileHandler();
        indexFileHandler.splitIndexFile(indexFilePath);

        long end = System.currentTimeMillis();
        System.out.println(end - start);

    }

}
