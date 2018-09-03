import java.io.*;

class SecondaryIndex {

    private static int outputIndexCount;

    private static final String INDEX_FILES_INDEX = "/Volumes/KrishnaDwypayan_HD/IIIT/IRE/index_files_index.txt";

    void createSecondaryIndex(String pathToIndexFile, String pathToOutputIndexFile) {

        String outputIndexFileName = pathToOutputIndexFile + outputIndexCount + ".txt";
        Integer firstTermId = 0, lastTermId = -1;

        try {

            BufferedReader bufferedReader = new BufferedReader(new FileReader(pathToIndexFile));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputIndexFileName));
            BufferedWriter bufferedWriterForIndexFiles = new BufferedWriter(new FileWriter(INDEX_FILES_INDEX));

            boolean fileLimitExceededFlag = false;

            String line, previousLine = "";
            while ((line = bufferedReader.readLine()) != null) {

                // Create smaller files of 30MB each from the large index file
                if (new File(outputIndexFileName).length() > 30000000) {

                    fileLimitExceededFlag = true;

                    // Get the lastTermId from the previous line contents and write to
                    // index of index files
                    String[] previousLineContents = previousLine.split(":");
                    lastTermId = Integer.parseInt(previousLineContents[0]);

                    bufferedWriterForIndexFiles.write(outputIndexFileName + ":"
                            + firstTermId + "-" + lastTermId + "\n");

                    // Close the current BufferedWriter and set the firstLineFlag to false
                    bufferedWriter.close();

                    // Get the firstTermId from the current line contents
                    String[] lineContents = line.split(":");
                    firstTermId = Integer.parseInt(lineContents[0]);
                    line = String.join(":", lineContents[0], lineContents[1]);

                    // Increment the file index count and create a new BufferedWriter for a
                    // new secondary index file
                    outputIndexCount++;
                    outputIndexFileName = pathToOutputIndexFile + outputIndexCount + ".txt";
                    bufferedWriter = new BufferedWriter(new FileWriter(outputIndexFileName));
                }
                else {
                    fileLimitExceededFlag = false;
                }

                bufferedWriter.write(line);
                previousLine = line;
            }

            if (!fileLimitExceededFlag) {
                String[] previousLineContents = previousLine.split(":");
                lastTermId = Integer.parseInt(previousLineContents[0]);
                bufferedWriterForIndexFiles.write(outputIndexFileName + ":"
                        + firstTermId + "-" + lastTermId + "\n");
            }

            bufferedWriterForIndexFiles.close();

        } catch (java.io.IOException e) {
            e.printStackTrace();
        }

    }

}
