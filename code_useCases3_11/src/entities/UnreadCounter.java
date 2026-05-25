package entities;
import java.util.List;

public class UnreadCounter {
    private int count;
    public UnreadCounter(int initial) { this.count = initial; }
    public int getCount() { return count; }
    public void decrement() { if (count > 0) count--; }
    public void reset() { count = 0; }
    public void update(List<Notification> list) {
        count = (int) list.stream().filter(n -> !n.read).count();
    }
}