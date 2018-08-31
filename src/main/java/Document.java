public class Document {

    private int id;
    private String title;
    private String text;
    private String contributorUsername;
    private int contributorId;

    int getId() {
        return id;
    }

    String getTitle() {
        return title;
    }

    String getText() {
        return text;
    }

    String getContributorUsername() {
        return contributorUsername;
    }

    int getContributorId() {
        return contributorId;
    }

    void setId(int id) {
        this.id = id;
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setText(String text) {
        this.text = text;
    }

    void setContributorUsername(String contributorUsername) {
        this.contributorUsername = contributorUsername;
    }

    void setContributorId(int contributorId) {
        this.contributorId = contributorId;
    }

    @Override
    public String toString() {
        return "Title : " + this.title + "\nID : " + this.id + "\nContributor : "
                + contributorUsername + ", " + contributorId;
    }

}
