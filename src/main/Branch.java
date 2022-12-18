package main;

// a one-step path
public class Branch {
    private String direction;
    private boolean end;

    public Branch(String direction) {
        this.direction = direction;
        this.end = false;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }
}
