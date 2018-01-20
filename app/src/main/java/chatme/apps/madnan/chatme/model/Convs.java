package chatme.apps.madnan.chatme.model;

/**
 * Created by mahmoud adnan on 1/20/2018.
 */

public class Convs {

    private boolean seen;
    private long timestamp;

    public Convs(){}

    public Convs(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
