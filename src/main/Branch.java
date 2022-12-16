package main;

// a branch is a straight line path
public class Branch {
    String direction;
    Position pos;
    int steps;
    boolean end;

    public Branch(Position pos, String direction) {
        this.pos = pos;
        this.direction = direction;
        this.end = false;
        this.steps = 0;
    }
}
