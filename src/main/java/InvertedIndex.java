import org.javatuples.Triplet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

class InvertedIndex {

    // Map the token with numbers and store the numbers instead of the token into the file
    private static Long wordCount = 0L;
    private static HashMap<String, Long> wordMap = new HashMap<>();

    static HashMap<Long, HashMap<Integer, HashMap<String, Integer>>> invertedIndex = new HashMap<>();
    static HashMap<Integer, Triplet<String, Integer, String>> docMetadataMap = new HashMap<>();

    private static Long getWordIndex(String word) {
        if (wordMap.containsKey(word)) {
            return wordMap.get(word);
        }

        Long termId = wordCount;
        wordMap.put(word, wordCount);
        wordCount++;

        return termId;
    }

    static void createInvertedIndex(Integer docId, String word, String indexChar) {

        Long termId = getWordIndex(word);

        HashMap<Integer, HashMap<String, Integer>> wordMap = invertedIndex.get(termId);
        if (wordMap != null) {
            HashMap<String, Integer> wordCountMap = wordMap.get(docId);
            if (wordCountMap != null) {
                wordCountMap.merge(indexChar, 1, (a, b) -> a + b);
            }
            else {
                HashMap<String, Integer> newWordCountMap = new HashMap<String, Integer>();
                newWordCountMap.put(indexChar, 1);
                wordMap.put(docId, newWordCountMap);
            }
        }
        else {
            HashMap<Integer, HashMap<String, Integer>> newWordMap = new HashMap<>();
            HashMap<String, Integer> wordCountMap = new HashMap<>();
            wordCountMap.put(indexChar, 1);
            newWordMap.put(docId, wordCountMap);
            invertedIndex.put(termId, newWordMap);
        }

    }

    static void writeInvertedIndex(String outputFilePath) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFilePath));

            // Sort the invertedIndex by keys before writing to file
            ArrayList<Long> sortedKeySet = new ArrayList<>(invertedIndex.keySet());
            Collections.sort(sortedKeySet);

            for (Long termId : sortedKeySet) {
                StringBuilder line = new StringBuilder();
                line.append(termId.toString()).append(":");
                for (Integer docId : invertedIndex.get(termId).keySet()) {
                    line.append(docId).append("-");
                    for (String idx : invertedIndex.get(termId).get(docId).keySet()) {
                        line.append(idx).append(invertedIndex.get(termId).get(docId).get(idx));
                    }
                    line.append("|");
                }
                line.append("\n");
                bufferedWriter.write(line.toString());
            }

            bufferedWriter.close();

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void printInvertedIndex() {
        for (Long termId : invertedIndex.keySet()) {
            StringBuilder line = new StringBuilder();
            line.append(termId.toString()).append(":");
            for (Integer docId : invertedIndex.get(termId).keySet()) {
                line.append(docId).append("-");
                for (String idx : invertedIndex.get(termId).get(docId).keySet()) {
                    line.append(idx).append(invertedIndex.get(termId).get(docId).get(idx));
                }
                line.append("|");
            }
            line.append("\n");
            System.out.println(line.toString());
        }

    }

}
