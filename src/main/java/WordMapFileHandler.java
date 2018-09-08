import java.io.*;
import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.TreeMap;

public class WordMapFileHandler {

    private static int outputIndexCount;

    static final String WORD_MAP_SECONDARY_FILENAME = SearchEngineMain.SECONDARY_INDEX_PATH + "word_map_secondary_index.txt";


    private class WordMapNode implements Comparable<WordMapNode> {

        String term;
        int termId;
        BufferedReader bufferedReader;

        private WordMapNode(String term, int termId, BufferedReader bufferedReader) {
            this.term = term;
            this.termId = termId;
            this.bufferedReader = bufferedReader;
        }

        @Override
        public int compareTo(WordMapNode node) {
            if (term.compareTo(node.term) <= -1) {
                return -1;
            }
            else if (term.compareTo(node.term) >= 1) {
                return 1;
            }
            return 0;
        }
    }

    static void splitWordMapFile(String pathToWordMapFile) {
        String outputIndexFileName = SearchEngineMain.SPLIT_WORD_MAP_PATH + "word_map_" + outputIndexCount + ".txt";
        String wordMapSecondaryIndex = SearchEngineMain.SECONDARY_INDEX_PATH + "word_map_secondary_index.txt";

        try {

            BufferedReader bufferedReader = new BufferedReader(new FileReader(pathToWordMapFile));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputIndexFileName));

            // ---------Comment this line if splitting for first time on unsorted word map file---------
            BufferedWriter secondaryIndexBufferedWriter = new BufferedWriter(new FileWriter(wordMapSecondaryIndex));

            String line;
            while ((line = bufferedReader.readLine()) != null) {

                line = line.replaceAll("[^\\x00-\\x7F]", "");

                if (new File(outputIndexFileName).length() < 30000000) {
                    // Write the line to the small index file and the line offset to the offset file.
                    bufferedWriter.write(line + "\n");
                }
                else {
                    // Close the current BufferedWriter
                    bufferedWriter.close();

                    // ---------Comment this line if splitting for first time on unsorted word map file---------
                    createSecondaryIndex(outputIndexFileName, secondaryIndexBufferedWriter);

                    // Increment the file index count and create a new BufferedWriter for a new index file.
                    outputIndexCount++;
                    outputIndexFileName = SearchEngineMain.SPLIT_WORD_MAP_PATH + "word_map_" + outputIndexCount + ".txt";

                    bufferedWriter = new BufferedWriter(new FileWriter(outputIndexFileName));

                }

            }

            bufferedWriter.close();

            // ---------Comment this line if splitting for first time on unsorted word map file---------
            createSecondaryIndex(outputIndexFileName, secondaryIndexBufferedWriter);


            // ---------Comment this line if splitting for first time on unsorted word map file---------
            secondaryIndexBufferedWriter.close();

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    static void sortSplitFiles(String folderName) {
        File file = new File(folderName);

        for (File fileName : file.listFiles()) {

            if (fileName.getName().contains(".DS_Store")) {
                continue;
            }

            try {

                TreeMap<String, Integer> treeMap = new TreeMap<>();
                BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    String[] lineContents = line.split(":");
                    treeMap.put(lineContents[0], Integer.parseInt(lineContents[1]));
                }

                BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileName));
                for (String term : treeMap.keySet()) {
                    bufferedWriter.write(term + ":" + treeMap.get(term) + "\n");
                }
                bufferedWriter.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    void merge(String folderName) {
        File folder = new File(folderName);
        PriorityQueue<WordMapNode> pq = new PriorityQueue<>();
        ArrayList<BufferedReader> fileReaders = new ArrayList<>();

        for (File file : folder.listFiles()) {
            if (file.getName().contains(".DS_Store")) {
                continue;
            }

            try {
                BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
                fileReaders.add(bufferedReader);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        for (BufferedReader bufferedReader : fileReaders) {
            String line;

            try {
                if ((line = bufferedReader.readLine()) != null) {
                    String[] lineContents = line.split(":");
                    pq.add(new WordMapNode(lineContents[0], Integer.parseInt(lineContents[1]), bufferedReader));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String mergedOutputFile = folderName + "word_map_merged.txt";
        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(mergedOutputFile));
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (!pq.isEmpty()) {
            WordMapNode node = pq.poll();
            String line = node.term + ":" + node.termId + "\n";
            try {
                bufferedWriter.write(line);

                String nextLine;
                if ((nextLine = node.bufferedReader.readLine()) != null) {
                    String[] nextLineContents = nextLine.split(":");
                    pq.add(new WordMapNode(nextLineContents[0], Integer.parseInt(nextLineContents[1]), node.bufferedReader));
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void createOffsetFile(String wordMapFileName, String offsetFileName) {
        try {

            // RandomAccessFile will be used for creating the offset file
            RandomAccessFile randomAccessFile = new RandomAccessFile(wordMapFileName, "r");

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(offsetFileName));

            String line;
            while ((line = randomAccessFile.readLine()) != null) {
                bufferedWriter.write(line.split(":")[0] + ":" + String.valueOf(randomAccessFile.getFilePointer()) + "\n");
            }

            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void createSecondaryIndex(String fileName, BufferedWriter bufferedWriter) {
        try {

            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));

            String line, previousLine = "";
            boolean firstTermIdFlag = false;
            String firstTerm = "", lastTerm;

            while ((line = bufferedReader.readLine()) != null) {

                if (!firstTermIdFlag) {
                    firstTermIdFlag = true;
                    firstTerm = line.split(":")[0];
                }

                previousLine = line;
            }

            lastTerm = previousLine.split(":")[0];
            bufferedWriter.write(fileName + ":" + firstTerm + ":" + lastTerm + "\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
