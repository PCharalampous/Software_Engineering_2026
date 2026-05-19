public class Notification {
    public String category, text, detail, target, tagColor;
    public boolean read;

    public Notification(String cat, String txt, String det, String tgt, String tc) {
        this.category = cat; this.text = txt; this.detail = det; this.target = tgt; this.tagColor = tc;
    }
}