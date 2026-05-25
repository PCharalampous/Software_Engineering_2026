package entities;

public class Issue {
    private String title;
    private String description;
    
    public Issue(String title, String description) {
        this.title = title;
        this.description = description;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
}