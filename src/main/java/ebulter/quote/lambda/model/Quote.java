package ebulter.quote.lambda.model;

public class Quote {
    private Long id;
    private String quoteText;
    private String author;

    public Quote() {
    }

    public Quote(Long id, String quoteText, String author) {
        this.id = id;
        this.quoteText = quoteText;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuoteText() {
        return quoteText;
    }

    public void setQuoteText(String quoteText) {
        this.quoteText = quoteText;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
