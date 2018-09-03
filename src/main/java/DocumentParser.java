import org.javatuples.Triplet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DocumentParser {

    private HashMap<String, Pattern> regexPatterns;

    private int docId;
    private String title;
    private String text;

    private static final Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    DocumentParser(Document document, HashMap<String, Pattern> regexPatterns) {
        this.regexPatterns = regexPatterns;

        this.docId = document.getId();
        this.title = document.getTitle();
        this.text = document.getText();
    }

    private ArrayList<String> tokenize(String text) {

        ArrayList<String> tokens = new ArrayList<>();

        String regexBasic = "[\\*\\(\\)\\+\\|\\{\\}\\[\\]!\"\\;\'<>,\\\n]";
        text = text.replaceAll(regexBasic, " ");
        text = text.toLowerCase();

        StopWords stopWords = StopWords.getInstance();
        Stemmer stemmer = new Stemmer();

        String[] tokensOnSplit = text.split(" ");

        for (String token : tokensOnSplit) {
            if (token.length() > 2 && !stopWords.isStopWord(token)) {

                if (token.length() > 7 && token.substring(0, 7).equals("http://")) {
                    tokens.add(token);
                    continue;
                }

                String stemmedWord = stemmer.stem(token);
                if (!stemmedWord.equals("notenglish")) {
                    tokens.add(stemmedWord);
                }
            }
        }

        return tokens;
    }

    void parseDocument() {

        ArrayList<String> titleTokens;
        ArrayList<String> externalLinksTokens;
        ArrayList<String> infoboxTokens;
        ArrayList<String> referenceTokens;
        ArrayList<String> categoryTokens;
        ArrayList<String> textBodyTokens;

        titleTokens = tokenize(title);
        for (String titleToken : titleTokens) {
            InvertedIndex.createInvertedIndex(docId, titleToken, "t");
        }

        Matcher infoboxMatcher = regexPatterns.get("infobox").matcher(text);
        StringBuilder infoboxTexts = new StringBuilder();
        while (infoboxMatcher.find()) {
            String infobox = infoboxMatcher.group();
            infobox = infobox.replaceAll("=", " ");
            infoboxTexts.append(infobox);
            text = text.replace(infobox, " ");
        }

        if (infoboxTexts.length() > 0) {
            infoboxTokens = tokenize(infoboxTexts.toString());
            for (int i = 0; i < infoboxTokens.size(); i++) {
                InvertedIndex.createInvertedIndex(docId, infoboxTokens.get(i), "i");
            }
        }

        Matcher referencesMatcher = regexPatterns.get("references").matcher(text);
        while (referencesMatcher.find()) {
            String referencesText = referencesMatcher.group();
            text = text.replace(referencesMatcher.group(), " ");
            referenceTokens = tokenize(referencesText);
            for (int i = 0; i < referenceTokens.size(); i++) {
                InvertedIndex.createInvertedIndex(docId, referenceTokens.get(i), "r");
            }
        }

        Matcher categoryMatcher = regexPatterns.get("category").matcher(text);
        StringBuilder categoryTexts = new StringBuilder();
        while (categoryMatcher.find()) {
            String category = categoryMatcher.group();
            category = category.replaceAll("=", " ");
            categoryTexts.append(category);
            text = text.replace(category, " ");
        }

        if (categoryTexts.length() > 0) {
            categoryTokens = tokenize(categoryTexts.toString());
            for (int i = 0; i < categoryTokens.size(); i++) {
                InvertedIndex.createInvertedIndex(docId, categoryTokens.get(i), "c");
            }
        }

        Matcher externalLinksMatcher = regexPatterns.get("ext_links").matcher(text);
        int externalLinksIndex = 0;
        while (externalLinksMatcher.find()) {
            String externalLinksText = externalLinksMatcher.group();
            externalLinksIndex = externalLinksMatcher.start();
            text = text.substring(0, externalLinksIndex);

            externalLinksTokens = tokenize(externalLinksText);
            for (int i = 0; i < externalLinksTokens.size(); i++) {
                InvertedIndex.createInvertedIndex(docId, externalLinksTokens.get(i), "e");
            }
        }

        text = text.replaceAll("\\{\\|.*?\\|\\}", " ");
        text = text.replaceAll("\\{\\{File.*\\}\\}", " ");
        text = text.replaceAll("\\<(.*)?\\>", " ");
        text = text.replaceAll("\\{\\{[vV]?[cC]ite.*\\}\\}", " ");
        text = text.replaceAll("=", " ");

        textBodyTokens = tokenize(text);
        for (int i = 0; i < textBodyTokens.size(); i++) {
            InvertedIndex.createInvertedIndex(docId, textBodyTokens.get(i), "b");
        }

        LOGGER.log(Level.INFO, "Page : " + docId);

    }

}
