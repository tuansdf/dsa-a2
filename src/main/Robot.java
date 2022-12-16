package main;

import java.util.Arrays;

// Decision choosing branch for DFS:
// - Default: Up
// - Horizontal: Right then Left
// - Vertical: Up then Down
// Stack branches for DFS
// - A stack contains branch node
// - Each node contains a stack of cell leading to that node (for back-tracking)
// The virtual map size for the robot is 1997x1997
public class Robot {

    static final int VIRTUAL_MAP_ONE_SIDE = 998;
    static final int VIRTUAL_MAP_SIZE = VIRTUAL_MAP_ONE_SIDE * 2 + 1;

    private final String[] virtualMap;
    private int virtualCurrentCol;
    private int virtualCurrentRow;

    private final Maze maze;

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

        replaceCellAt(virtualCurrentRow, virtualCurrentCol, 'R');

        maze = new Maze();
    }

    public void navigate() {
        LinkedListStack<Branch> branches = new LinkedListStack<>();

        // register all four directions for back-tracking
        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "DOWN"));
        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "LEFT"));
        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "RIGHT"));
        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "UP"));

        Branch currentBranch;
        String currentDirection;
        String currentResult = "";

        while (!currentResult.equals("win")) {
            currentBranch = branches.peek();
            if (currentBranch == null) {
                break;
            }
            currentDirection = currentBranch.direction;

            System.out.println(currentDirection);

            currentResult = virtualCheck(currentDirection, false);
            if (currentResult.equals("true")) {
                currentResult = adapterGo(currentDirection);
            }
            if (currentResult.equals("false")) {
                if (currentBranch.end) {
                    backtrack(currentBranch);
                    branches.pop();
                } else if (currentBranch.pos.col == virtualCurrentCol && currentBranch.pos.row == virtualCurrentRow) {
                    branches.pop();
                } else {
                    currentBranch.end = true;
                    switch (currentDirection) {
                        case "UP", "DOWN" -> {
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "RIGHT"));
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "LEFT"));
                        }
                        case "RIGHT", "LEFT" -> {
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "UP"));
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "DOWN"));
                        }
                    }
                }
            } else {
                switch (currentDirection) {
                    case "UP", "DOWN" -> {
                        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "RIGHT"));
                        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "LEFT"));
                    }
                    case "RIGHT", "LEFT" -> {
                        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "UP"));
                        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), "DOWN"));
                    }
                }
                currentBranch.steps++;
            }
        }

        // for testing only
        if (currentResult.equals("win")) {
            replaceCellAt(virtualCurrentRow, virtualCurrentCol, 'G');
        }
        System.out.println(maze.steps);
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
        if (!direction.equals("UP") && !direction.equals("DOWN") && !direction.equals("LEFT") && !direction.equals("RIGHT")) {
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
