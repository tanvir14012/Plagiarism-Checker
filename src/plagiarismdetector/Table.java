package plagiarismdetector;


public class Table {

    public String getRefText() {
        return refText;
    }

    public String getQueryText() {
        return queryText;
    }

    public String getResultPercentage() {
        return resultPercentage;
    }
    private String refText, queryText, resultPercentage;

    public Table(String refText, String queryText, String resultPercentage) {
        this.refText = refText;
        this.queryText = queryText;
        this.resultPercentage = resultPercentage;
    }
    
}
