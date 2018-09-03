import java.io.*;

class IndexFileHandler {

    private static int outputIndexCount;

    /**
     * Split the index file which would be of a huge size into smaller files of 30MB each.
     * For each split index file, we create an offset file where we will maintain the termId
     * and the corresponding length of the line.
     *
     * @param pathToIndexFile is the path of the index file which is to be split into smaller files
     */
    void splitIndexFile(String pathToIndexFile) {

        String outputIndexFileName = SearchEngineMain.SPLIT_INDEX_PATH + outputIndexCount + ".txt";
        String secondaryFileName = SearchEngineMain.SECONDARY_INDEX_PATH + "secondary_index.txt";

        try {

            BufferedReader bufferedReader = new BufferedReader(new FileReader(pathToIndexFile));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputIndexFileName));

            BufferedWriter bufferedWriterForSecondaryIndex = new BufferedWriter(new FileWriter(secondaryFileName, true));

            String line;
            while ((line = bufferedReader.readLine()) != null) {

                // Create smaller files of 30MB each from the large index file
                if (new File(outputIndexFileName).length() > 30000000) {

                    // Close the current BufferedWriter
                    bufferedWriter.close();

                    // Create the offsetFile for the new index file
                    String offsetFileName = SearchEngineMain.OFFSETS_PATH + "offset_" + outputIndexCount + ".txt";
                    createOffsetFile(outputIndexFileName, offsetFileName);

                    // Update the secondary index file for the new offset file
                    createSecondaryIndex(offsetFileName, bufferedWriterForSecondaryIndex);

                    // Increment the file index count and create a new BufferedWriter for a new index file.
                    outputIndexCount++;
                    outputIndexFileName = SearchEngineMain.SPLIT_INDEX_PATH + outputIndexCount + ".txt";

                    bufferedWriter = new BufferedWriter(new FileWriter(outputIndexFileName));
                }

                // Write the line to the small index file and the line offset to the offset file.
                bufferedWriter.write(line + "\n");
            }

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    private void createOffsetFile(String indexFileName, String offsetFileName) {

        try {

            // RandomAccessFile will be used for creating the offset file
            RandomAccessFile randomAccessFile = new RandomAccessFile(indexFileName, "r");

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

    /**
     * Create the secondary index for the specified file
     * (Here, we create the secondary index for the offset files.)
     */
    private void createSecondaryIndex(String fileName, BufferedWriter bufferedWriter) {

        try {

            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));

            String line, previousLine = "";
            boolean firstTermIdFlag = false;
            int firstTermId = -1, lastTermId;

            while ((line = bufferedReader.readLine()) != null) {

                if (!firstTermIdFlag) {
                    firstTermIdFlag = true;
                    firstTermId = Integer.parseInt(line.split(":")[0]);
                }

                previousLine = line;
            }

            lastTermId = Integer.parseInt(previousLine.split(":")[0]);
            bufferedWriter.write(fileName + ":" + firstTermId + "-" + lastTermId);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
