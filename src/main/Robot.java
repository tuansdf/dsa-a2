package main;

import java.util.Arrays;

// Decision choosing branch for DFS:
// - Default: Up
// - Horizontal: Right then Left
// - Vertical: Up then Down
// The virtual map size for the robot is 1997x1997
public class Robot {

    private static final int VIRTUAL_MAP_ONE_SIDE = 998;
    private static final int VIRTUAL_MAP_SIZE = VIRTUAL_MAP_ONE_SIDE * 2 + 1;

    private static final String UP = "UP";
    private static final String DOWN = "DOWN";
    private static final String LEFT = "LEFT";
    private static final String RIGHT = "RIGHT";

    private static final String TRUE_SIGNAL = "true";
    private static final String FALSE_SIGNAL = "false";
    private static final String WIN_SIGNAL = "win";

    private static final char EMPTY_CELL = ' ';
    private static final char PATH_CELL = '*';
    private static final char WALL_CELL = '.';

    private final String[] virtualMap;
    private int virtualCurrentCol;
    private int virtualCurrentRow;

    private final Maze maze;

    public Robot() {
        // initialize the virtual map for robot
        char[] row = new char[VIRTUAL_MAP_SIZE];
        Arrays.fill(row, EMPTY_CELL);
        String rowString = new String(row);
        virtualMap = new String[VIRTUAL_MAP_SIZE];
        Arrays.fill(virtualMap, rowString);

        // assume the robot is in the position (0, 0) in its virtual map,
        // so index 0 + the length of one half to put it at the center of the 2d array
        virtualCurrentCol = VIRTUAL_MAP_ONE_SIDE;
        virtualCurrentRow = VIRTUAL_MAP_ONE_SIDE;

        // mark the starting point
        replaceCellAt(virtualCurrentRow, virtualCurrentCol, PATH_CELL);

        maze = new Maze();
    }

    public void navigate() {
        LinkedListStack<Branch> branches = new LinkedListStack<>();

        // register all four directions for back-tracking
        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), DOWN));
        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), LEFT));
        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), RIGHT));
        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), UP));

        Branch currentBranch;
        String currentDirection;
        String currentResult = "";

        while (!currentResult.equals(WIN_SIGNAL)) {
            currentBranch = branches.peek();
            if (currentBranch == null) {
                break;
            }
            currentDirection = currentBranch.getDirection();

            System.out.println(currentDirection);

            // check the next cell and consider own path as obstacle
            currentResult = virtualCheck(currentDirection, false);
            // only when the virtual map does not have record of such obstacle,
            // advance to the next cell
            if (currentResult.equals(TRUE_SIGNAL)) {
                currentResult = adapterGo(currentDirection);
            }
            // if it can not go to the next cell in the real map:
            if (currentResult.equals(FALSE_SIGNAL)) {
                // if the current branch already branched out earlier,
                // back-track to the root and terminate that branch
                if (currentBranch.isEnd()) {
                    backtrack(currentBranch);
                    branches.pop();
                }
                // if the current position is the root of a branch,
                // and cannot go further, terminate that branch
                else if (currentBranch.getPos().getCol() == virtualCurrentCol && currentBranch.getPos().getRow() == virtualCurrentRow) {
                    branches.pop();
                }
                // the branch just hits the wall, so split into 2 branches
                else {
                    currentBranch.setEnd(true);
                    switch (currentDirection) {
                        case UP, DOWN -> {
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), RIGHT));
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), LEFT));
                        }
                        case RIGHT, LEFT -> {
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), UP));
                            branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), DOWN));
                        }
                    }
                }
            }
            // if it can actually go to the next cell in the real map:
            else {
                switch (currentDirection) {
                    case UP, DOWN -> {
                        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), RIGHT));
                        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), LEFT));
                    }
                    case RIGHT, LEFT -> {
                        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), UP));
                        branches.push(new Branch(new Position(virtualCurrentRow, virtualCurrentCol), DOWN));
                    }
                }
                currentBranch.setSteps(currentBranch.getSteps() + 1);
            }
        }

        // for testing only
        if (currentResult.equals(WIN_SIGNAL)) {
            replaceCellAt(virtualCurrentRow, virtualCurrentCol, 'G');
        }
        System.out.println(maze.steps);
        for (int i = VIRTUAL_MAP_ONE_SIDE - 50; i < VIRTUAL_MAP_SIZE - VIRTUAL_MAP_ONE_SIDE + 50; i++) {
            System.out.println(virtualMap[i]);
        }
    }

    private void backtrack(Branch b) {
        String direction = getOppositeDirection(b.getDirection());
        for (int i = 0; i < b.getSteps(); i++) {
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

        if (result.equals(FALSE_SIGNAL)) {
            // prepare to mark wall in virtual map
            String virtualResult = virtualGo(direction);
            if (virtualResult.equals(TRUE_SIGNAL)) {
                // mark wall
                replaceCellAt(virtualCurrentRow, virtualCurrentCol, WALL_CELL);
                // go back to the original place
                virtualGo(getOppositeDirection(direction));
            }
        } else {
            // move in virtual map and mark path
            virtualGo(direction);
            replaceCellAt(virtualCurrentRow, virtualCurrentCol, PATH_CELL);
        }

        return result;
    }

    private String getOppositeDirection(String direction) {
        switch (direction) {
            case UP -> {
                return DOWN;
            }
            case DOWN -> {
                return UP;
            }
            case LEFT -> {
                return RIGHT;
            }
            case RIGHT -> {
                return LEFT;
            }
            default -> {
                return "";
            }
        }
    }

    // maze.go() but for virtual map only
    private String virtualGo(String direction) {
        String result = virtualCheck(direction, true);
        if (result.equals(TRUE_SIGNAL)) {
            switch (direction) {
                case UP -> {
                    virtualCurrentRow--;
                }
                case DOWN -> {
                    virtualCurrentRow++;
                }
                case LEFT -> {
                    virtualCurrentCol--;
                }
                case RIGHT -> {
                    virtualCurrentCol++;
                }
            }
        }
        return result;
    }

    // mirror of maze.go() for easy checking surrounding on virtual map
    private String virtualCheck(String direction, boolean isOverride) {
        if (!direction.equals(UP) && !direction.equals(DOWN) && !direction.equals(LEFT) && !direction.equals(RIGHT)) {
            // invalid direction
            return FALSE_SIGNAL;
        }

        int currentRow = virtualCurrentRow;
        int currentCol = virtualCurrentCol;

        switch (direction) {
            case UP -> {
                currentRow--;
            }
            case DOWN -> {
                currentRow++;
            }
            case LEFT -> {
                currentCol--;
            }
            case RIGHT -> {
                currentCol++;
            }
        }

        switch (virtualMap[currentRow].charAt(currentCol)) {
            case WALL_CELL -> {
                return FALSE_SIGNAL;
            }
            case PATH_CELL -> {
                if (isOverride) return TRUE_SIGNAL;
                return FALSE_SIGNAL;
            }
            default -> {
                return TRUE_SIGNAL;
            }
        }
    }
}
