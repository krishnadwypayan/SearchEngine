import java.io.*;
import java.util.ArrayList;
import java.util.Objects;
import java.util.PriorityQueue;

public class MergeInvertedIndex {

    static int mergedIndexCount;

    private class QueueNode implements Comparable<QueueNode> {

        BufferedReader bufferedReader;
        int termId;
        StringBuffer postingsList;

        private QueueNode(BufferedReader br, int key, StringBuffer value) {
            this.bufferedReader = br;
            this.termId = key;
            this.postingsList = new StringBuffer(value);
        }

        @Override
        public int compareTo(QueueNode node) {
            return Integer.compare(termId, node.termId);
        }
    }

    private void mergeHandler(ArrayList<BufferedReader> filePointersList, BufferedWriter bufferedWriter)
            throws IOException {
        PriorityQueue<QueueNode> pq = new PriorityQueue<>();

        for (BufferedReader br : filePointersList) {
            String line = "";

            try {
                line = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!line.equals("")) {
                String[] lineContents = line.split(":");
                int termId = Integer.parseInt(lineContents[0]);
                StringBuffer postingsList = new StringBuffer(lineContents[1]);
                pq.add(new QueueNode(br, termId, postingsList));
            }
        }

        while (!pq.isEmpty()) {

            QueueNode node;
            ArrayList<QueueNode> queueNodes = new ArrayList<>();

            if (!pq.isEmpty()) {
                node = pq.poll();
                queueNodes.add(node);
            }

            while (!pq.isEmpty() && pq.peek().termId == queueNodes.get(0).termId) {
                node = pq.poll();
                queueNodes.add(node);
            }

            // Write to file
            int key = queueNodes.get(0).termId;
            StringBuilder line = new StringBuilder();
            for (QueueNode queueNode : queueNodes) {
                line.append(queueNode.postingsList);

                String nextLine;
                if ((nextLine = queueNode.bufferedReader.readLine()) != null) {
                    String[] nextLineContents = nextLine.split(":");
                    int nextLineTermId = Integer.parseInt(nextLineContents[0]);
                    StringBuffer nextLinePostingsList = new StringBuffer(nextLineContents[1]);
                    pq.add(new QueueNode(queueNode.bufferedReader, nextLineTermId, nextLinePostingsList));
                }
            }

            bufferedWriter.write(key + ":" + line.toString() + "\n");
            bufferedWriter.flush();
        }
    }

    // Create file pointers for 10 files at a time and then merge them
    void merge(String folderName) throws IOException {
        File folder = new File(folderName);
        File[] listOfFiles = folder.listFiles();

        if (Objects.requireNonNull(listOfFiles).length != 0) {
            int fileCounter = 0;
            ArrayList<BufferedReader> filePointersList = new ArrayList<>();

            for (File listOfFile : listOfFiles) {

                if (listOfFile.getName().contains(".DS_Store")) {
                    continue;
                }

                if (fileCounter == 10) {
                    // Call merge here
                    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(SearchEngineMain.MERGE_INDEX_PATH + mergedIndexCount + "_level_" + SearchEngineMain.level + ".txt", true));
                    mergeHandler(filePointersList, bufferedWriter);
                    fileCounter = 0;
                    mergedIndexCount++;
                    filePointersList.clear();
                }

                try {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(folderName + listOfFile.getName()));
                    filePointersList.add(bufferedReader);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                fileCounter++;

            }

            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(SearchEngineMain.MERGE_INDEX_PATH + mergedIndexCount + "_level_" + SearchEngineMain.level + ".txt", true));
            mergeHandler(filePointersList, bufferedWriter);
        }

        // Delete the files that have been merged
        for (int i = 0; i < listOfFiles.length; i++) {
            listOfFiles[i].delete();
        }
    }

}
