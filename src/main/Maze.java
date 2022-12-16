package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Scanner;

// Singly Linked List-based implementation of stack
class LinkedListStack<T> {
    // this class is used as a container of data
    static class Node<T> {
        T data;
        Node<T> next;

        public Node(T data) {
            this.data = data;
            this.next = null;
        }
    }

    private int size;
    private Node<T> head;

    public LinkedListStack() {
        size = 0;
        head = null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public boolean push(T item) {
        // add a new node at the beginning
        Node<T> node = new Node<T>(item);
        if (!isEmpty()) {
            node.next = head;
        }
        head = node;
        size++;
        return true;
    }

    public boolean pop() {
        // remove the first node
        // make sure the stack is not empty
        if (isEmpty()) {
            return false;
        }
        // advance head
        head = head.next;
        size--;
        return true;
    }

    public T peek() {
        // make sure the stack is not empty
        if (isEmpty()) {
            return null;
        }
        return head.data;
    }
}

public class Maze {
    int rows;
    int cols;
    String[] map;
    int robotRow;
    int robotCol;
    int steps;

    public Maze() {
        // Note: in my real test, I will create much larger
        // and more complicated map
        rows = 30;
        cols = 100;
        map = new String[rows];

        // read maze from file
        int index = 0;
        try {
            File file = new File("./resources/maze.txt");
            Scanner reader = new Scanner(file);
            while (reader.hasNextLine()) {
                String data = reader.nextLine();
                map[index] = data;
                index++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

//        map[0] = "...............";
//        map[1] = ".        .    .";
//        map[2] = ".  X     .    .";
//        map[3] = ".             .";
//        map[4] = "...............";

        // robot position
        robotRow = 2;
        robotCol = 1;
        steps = 0;
    }

    public String go(String direction) {
        if (!direction.equals("UP") &&
                !direction.equals("DOWN") &&
                !direction.equals("LEFT") &&
                !direction.equals("RIGHT")) {
            // invalid direction
            steps++;
            return "false";
        }
        int currentRow = robotRow;
        int currentCol = robotCol;
        if (direction.equals("UP")) {
            currentRow--;
        } else if (direction.equals("DOWN")) {
            currentRow++;
        } else if (direction.equals("LEFT")) {
            currentCol--;
        } else {
            currentCol++;
        }

        // check the next position
        if (map[currentRow].charAt(currentCol) == 'X') {
            // Exit gate
            steps++;
            System.out.println("Steps to reach the Exit gate " + steps);
            return "win";
        } else if (map[currentRow].charAt(currentCol) == '.') {
            // Wall
            steps++;
            return "false";
        } else {
            // Space => update robot location
            steps++;
            robotRow = currentRow;
            robotCol = currentCol;
            return "true";
        }
    }

    public static void main(String[] args) {
        (new Robot()).navigate();
    }
}

class Position {
    int row;
    int col;

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }
}

// a branch is a straight line path
class Branch {
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

// Decision choosing branch for DFS:
// - Default: Up
// - Horizontal: Right then Left
// - Vertical: Up then Down
// Stack branches for DFS
// - A stack contains branch node
// - Each node contains a stack of cell leading to that node (for back-tracking)
// The virtual map size for the robot is 1997x1997
class Robot {

    static final int VIRTUAL_MAP_ONE_SIDE = 998;
    static final int VIRTUAL_MAP_SIZE = VIRTUAL_MAP_ONE_SIDE * 2 + 1;

    private String[] virtualMap;
    private int virtualCurrentCol;
    private int virtualCurrentRow;

    private Maze maze;
    private String currentResult;
    private String currentDirection;

    private LinkedListStack<Position> history;

    public Robot() {
        // initialize the virtual map for robot
        char[] row = new char[VIRTUAL_MAP_SIZE];
        Arrays.fill(row, ' ');
        String rowString = new String(row);
        virtualMap = new String[VIRTUAL_MAP_SIZE];
        Arrays.fill(virtualMap, rowString);

        // assume the robot is in the position (0, 0) in its virtual map,
        // so index 0 + the length of one half to put it in the center of the 2d array
        virtualCurrentCol = VIRTUAL_MAP_ONE_SIDE;
        virtualCurrentRow = VIRTUAL_MAP_ONE_SIDE;
        history = new LinkedListStack<>();

        replaceCellAt(virtualCurrentRow, virtualCurrentCol, 'R');

        maze = new Maze();

        currentResult = "";
        currentDirection = "";
    }

    public void navigate() {
        LinkedListStack<Branch> branches = new LinkedListStack<>();

        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "DOWN"));
        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "LEFT"));
        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "RIGHT"));
        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "UP"));

        Branch currentBranch = branches.peek();

        while (!currentResult.equals("win")) {
            currentResult = "";
            if (currentBranch == null) {
                return;
            }
            switch (currentBranch.direction) {
                case "DOWN" -> {
                    System.out.println("DOWN");
                    currentResult = virtualCheck("DOWN", false);
                    if (currentResult.equals("true")) {
                        currentResult = adapterGo("DOWN");
                    }
                    if (currentResult.equals("false")) {
                        if (currentBranch.end) {
                            backtrack(currentBranch);
                            branches.pop();
                            currentBranch = branches.peek();
                        } else if (currentBranch.pos.col == virtualCurrentCol && currentBranch.pos.row == virtualCurrentRow) {
                            branches.pop();
                            currentBranch = branches.peek();
                        } else {
                            currentBranch.end = true;
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "RIGHT"));
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "LEFT"));
                            currentBranch = branches.peek();
                        }
                    } else {
                        currentBranch.steps++;
                    }
                }
                case "LEFT" -> {
                    System.out.println("LEFT");
                    currentResult = virtualCheck("LEFT", false);
                    if (currentResult.equals("true")) {
                        currentResult = adapterGo("LEFT");
                    }
                    if (currentResult.equals("false")) {
                        if (currentBranch.end) {
                            backtrack(currentBranch);
                            branches.pop();
                            currentBranch = branches.peek();
                        } else if (currentBranch.pos.col == virtualCurrentCol && currentBranch.pos.row == virtualCurrentRow) {
                            branches.pop();
                            currentBranch = branches.peek();
                        } else {
                            currentBranch.end = true;
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "UP"));
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "DOWN"));
                            currentBranch = branches.peek();
                        }
                    } else {
                        currentBranch.steps++;
                    }
                }
                case "RIGHT" -> {
                    System.out.println("RIGHT");
                    currentResult = virtualCheck("RIGHT", false);
                    if (currentResult.equals("true")) {
                        currentResult = adapterGo("RIGHT");
                    }
                    if (currentResult.equals("false")) {
                        if (currentBranch.end) {
                            backtrack(currentBranch);
                            branches.pop();
                            currentBranch = branches.peek();
                        } else if (currentBranch.pos.col == virtualCurrentCol && currentBranch.pos.row == virtualCurrentRow) {
                            branches.pop();
                            currentBranch = branches.peek();
                        } else {
                            currentBranch.end = true;
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "UP"));
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "DOWN"));
                            currentBranch = branches.peek();
                        }
                    } else {
                        currentBranch.steps++;
                    }
                }
                default -> {
                    System.out.println("UP");
                    currentResult = virtualCheck("UP", false);
                    if (currentResult.equals("true")) {
                        currentResult = adapterGo("UP");
                    }
                    if (currentResult.equals("false")) {
                        if (currentBranch.end) {
                            backtrack(currentBranch);
                            branches.pop();
                            currentBranch = branches.peek();
                        } else if (currentBranch.pos.col == virtualCurrentCol && currentBranch.pos.row == virtualCurrentRow) {
                            branches.pop();
                            currentBranch = branches.peek();
                        } else {
                            currentBranch.end = true;
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "RIGHT"));
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "LEFT"));
                            currentBranch = branches.peek();
                        }
                    } else {
                        currentBranch.steps++;
                    }
                }
            }
            if (currentResult.equals("win")) {
                replaceCellAt(virtualCurrentRow, virtualCurrentCol, 'X');
            }
        }
        for (int i = VIRTUAL_MAP_ONE_SIDE - 50; i < VIRTUAL_MAP_SIZE - VIRTUAL_MAP_ONE_SIDE + 50; i++) {
            System.out.println(virtualMap[i]);
        }
    }

    private void backtrack(Branch b) {
        String direction = getOppositeDirection(b.direction);
        for (int i = 0; i < b.steps; i++) {
            adapterGo(direction);
        }
    }

    private void replaceCellAt(int row, int col, char newChar) {
        StringBuilder sb = new StringBuilder(virtualMap[row]);
        sb.setCharAt(col, newChar);
        virtualMap[row] = sb.toString();
    }

    // run both go at the same time
    private String adapterGo(String direction) {
        String result = maze.go(direction);
        if (result.equals("false")) {
            // mark the cell as wall
            String virtualResult = virtualGo(direction);
            if (virtualResult.equals("true")) {
                replaceCellAt(virtualCurrentRow, virtualCurrentCol, '.');
                virtualGo(getOppositeDirection(direction));
            }
        } else {
            virtualGo(direction);
            replaceCellAt(virtualCurrentRow, virtualCurrentCol, '*');
        }

        return result;
    }

    private String getOppositeDirection(String direction) {
        switch (direction) {
            case "UP" -> {
                return "DOWN";
            }
            case "DOWN" -> {
                return "UP";
            }
            case "LEFT" -> {
                return "RIGHT";
            }
            case "RIGHT" -> {
                return "LEFT";
            }
            default -> {
                return "";
            }
        }
    }

    // maze.go() but for virtual map only
    private String virtualGo(String direction) {
        String result = virtualCheck(direction, true);
        if (result.equals("true")) {
            switch (direction) {
                case "UP" -> {
                    virtualCurrentRow--;
                }
                case "DOWN" -> {
                    virtualCurrentRow++;
                }
                case "LEFT" -> {
                    virtualCurrentCol--;
                }
                case "RIGHT" -> {
                    virtualCurrentCol++;
                }
            }
        }
        return result;
    }

    // mirror of maze.go() for easy checking surrounding on virtual map
    private String virtualCheck(String direction, boolean isOverride) {
        if (!direction.equals("UP") &&
                !direction.equals("DOWN") &&
                !direction.equals("LEFT") &&
                !direction.equals("RIGHT")) {
            // invalid direction
            return "false";
        }

        int currentRow = virtualCurrentRow;
        int currentCol = virtualCurrentCol;

        switch (direction) {
            case "UP" -> {
                currentRow--;
            }
            case "DOWN" -> {
                currentRow++;
            }
            case "LEFT" -> {
                currentCol--;
            }
            case "RIGHT" -> {
                currentCol++;
            }
        }

        switch (virtualMap[currentRow].charAt(currentCol)) {
            case '.' -> {
                return "false";
            }
            case '*' -> {
                if (isOverride) return "true";
                return "false";
            }
            default -> {
                return "true";
            }
        }
    }
}
