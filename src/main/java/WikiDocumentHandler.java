import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.CharArrayWriter;
import java.util.HashMap;
import java.util.regex.Pattern;

public class WikiDocumentHandler extends DefaultHandler {

    private Document document;
    private HashMap<String, Pattern> regexPatterns;

    private boolean contributorFlag = false;
    private boolean revisionFlag = false;

    private CharArrayWriter contents = new CharArrayWriter();

    WikiDocumentHandler(HashMap<String, Pattern> regexPatterns) {
        this.regexPatterns = regexPatterns;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException {
        if (qName.equalsIgnoreCase("page")) {
            this.document = new Document();
        }
        else if (qName.equalsIgnoreCase("contributor")) {
            contributorFlag = true;
        }
        else if (qName.equalsIgnoreCase("revision")) {
            revisionFlag = true;
        }

        contents.reset();

    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("title")) {
            document.setTitle(contents.toString());
        }
        else if (!contributorFlag && !revisionFlag && qName.equalsIgnoreCase("id")) {
            document.setId(Integer.parseInt(contents.toString()));
        }
        else if (qName.equalsIgnoreCase("text")) {
            document.setText(contents.toString());
        }
        else if (qName.equalsIgnoreCase("username")) {
            document.setContributorUsername(contents.toString());
        }
        else if (qName.equalsIgnoreCase("id") && contributorFlag) {
            document.setContributorId(Integer.parseInt(contents.toString()));
            contributorFlag = false;
        }
        else if (qName.equalsIgnoreCase("revision")) {
            revisionFlag = false;
        }
        else if (qName.equalsIgnoreCase("page")) {
//            System.out.println(document.toString());
            DocumentParser documentParser = new DocumentParser(document, regexPatterns);
            documentParser.parseDocument();
        }
    }

    @Override
    public void characters(char ch[], int start, int length) throws SAXException {
        contents.write(ch, start, length);
    }

}
