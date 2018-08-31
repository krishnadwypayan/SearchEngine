import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

public class SearchEngineMain {

    private static final String SMALL_WIKI_FILE_NAME = "/Users/krishnadwypayan/Documents/IIIT/IRE/WikipediaSearchEngine/res/small_wiki.xml";
    private static final String WIKI_FILE_NAME = "/Users/krishnadwypayan/Documents/IIIT/IRE/WikipediaSearchEngine/res/wiki-search-small.xml";

    private static HashMap<String, Pattern> regexPatterns;

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

    public static void main(String...args) {

        long start = System.currentTimeMillis();

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();

        try {
            makePatterns();

            SAXParser saxParser = saxParserFactory.newSAXParser();
            WikiDocumentHandler handler = new WikiDocumentHandler(regexPatterns);
            saxParser.parse(WIKI_FILE_NAME, handler);

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

        long end = System.currentTimeMillis();
        System.out.println(end - start);

        InvertedIndex.writeInvertedIndex("/Users/krishnadwypayan/Documents/IIIT/IRE/WikipediaSearchEngine/res/inverted_index.txt");
//        InvertedIndex.printInvertedIndex();
    }

}
