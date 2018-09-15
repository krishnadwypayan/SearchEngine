import java.io.*;

public class DocumentsTitleFileHandler {

    private static int outputIndexCount;

    static final String DOCUMENTS_TITLE_SECONDARY_INDEX = SearchEngineMain.SECONDARY_INDEX_PATH + "doc_title_secondary_index.txt";

    static void splitDocTitleFile(String pathToDocTitleFile) {
        String outputIndexFileName = SearchEngineMain.DOC_TITLE_SPLIT_FILES + "doc_title_" + outputIndexCount + ".txt";

        try {

            BufferedReader bufferedReader = new BufferedReader(new FileReader(pathToDocTitleFile));
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputIndexFileName));

            BufferedWriter secondaryIndexBufferedWriter = new BufferedWriter(new FileWriter(DOCUMENTS_TITLE_SECONDARY_INDEX));

            String line;
            while ((line = bufferedReader.readLine()) != null) {

                if (new File(outputIndexFileName).length() < 10000000) {
                    // Write the line to the small index file and the line offset to the offset file.
                    bufferedWriter.write(line + "\n");
                }
                else {

                    // Close the current BufferedWriter
                    bufferedWriter.close();

                    createSecondaryIndex(outputIndexFileName, secondaryIndexBufferedWriter);

                    // Increment the file index count and create a new BufferedWriter for a new index file.
                    outputIndexCount++;
                    outputIndexFileName = SearchEngineMain.DOC_TITLE_SPLIT_FILES + "doc_title_" + outputIndexCount + ".txt";

                    bufferedWriter = new BufferedWriter(new FileWriter(outputIndexFileName));
                }

            }

            // Close the current BufferedWriter
            bufferedWriter.close();

            createSecondaryIndex(outputIndexFileName, secondaryIndexBufferedWriter);

            secondaryIndexBufferedWriter.close();

        } catch (java.io.IOException e) {
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
