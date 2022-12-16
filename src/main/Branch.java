package main;

// a branch is a straight line path
public class Branch {
    private String direction;
    private Position pos;
    private int steps;
    private boolean end;

    public Branch(Position pos, String direction) {
        this.pos = pos;
        this.direction = direction;
        this.end = false;
        this.steps = 0;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Position getPos() {
        return pos;
    }

    public void setPos(Position pos) {
        this.pos = pos;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public boolean isEnd() {
        return end;
    }

    public void setEnd(boolean end) {
        this.end = end;
    }
}
