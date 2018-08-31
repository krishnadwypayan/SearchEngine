import org.javatuples.Triplet;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;

class InvertedIndex {

    private static HashMap<String, HashMap<Integer, HashMap<String, Integer>>> invertedIndex = new HashMap<>();
    static HashMap<Integer, Triplet<String, Integer, String>> docMetadataMap = new HashMap<>();

    static void createInvertedIndex(Integer docId, String word, String indexChar) {

        HashMap<Integer, HashMap<String, Integer>> wordMap = invertedIndex.get(word);
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
            invertedIndex.put(word, newWordMap);
        }

    }

    static void writeInvertedIndex(String outputFilePath) {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputFilePath));

            for (String token : invertedIndex.keySet()) {
                StringBuilder line = new StringBuilder();
                line.append(token).append(":");
                for (Integer docId : invertedIndex.get(token).keySet()) {
                    line.append(docId).append("-");
                    for (String idx : invertedIndex.get(token).get(docId).keySet()) {
                        line.append(idx).append(invertedIndex.get(token).get(docId).get(idx));
                    }
                    line.append("|");
                }
                line.append("\n");
                bufferedWriter.write(line.toString());
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    static void printInvertedIndex() {
        for (String token : invertedIndex.keySet()) {
            StringBuilder line = new StringBuilder();
            line.append(token).append(":");
            for (Integer docId : invertedIndex.get(token).keySet()) {
                line.append(docId).append("-");
                for (String idx : invertedIndex.get(token).get(docId).keySet()) {
                    line.append(idx).append(invertedIndex.get(token).get(docId).get(idx));
                }
                line.append("|");
            }
            line.append("\n");
            System.out.println(line.toString());
        }

    }

}
