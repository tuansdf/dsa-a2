package main;

import java.io.FileWriter;
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
        long start = System.currentTimeMillis();

        LinkedListStack<Branch> branches = new LinkedListStack<>();

        // register all four directions into the wait-list
        branches.push(new Branch(LEFT));
        branches.push(new Branch(RIGHT));
        branches.push(new Branch(DOWN));
        branches.push(new Branch(UP));

        Branch currentBranch;
        String currentDirection;
        String currentResult = "";

        while (!currentResult.equals(WIN_SIGNAL)) {
            currentBranch = branches.peek();
            if (currentBranch == null) {
                break;
            }
            currentDirection = currentBranch.getDirection();

            // if the current branch already branched out earlier,
            // back-track to the previous branch and terminate the current branch
            if (currentBranch.isEnd()) {
                // backtrack to the previous branch
                adapterGo(getOppositeDirection(currentDirection));
                branches.pop();
                continue;
            }

            // check the next cell and consider own path as obstacle
            currentResult = virtualCheck(currentDirection, false);
            // only when the virtual map does not have record of such obstacle,
            // advance to the next cell
            if (currentResult.equals(TRUE_SIGNAL)) {
                currentResult = adapterGo(currentDirection);
            }

            // if there is an obstacle, remove that branch
            if (currentResult.equals(FALSE_SIGNAL)) {
                branches.pop();
            }
            // if there is no obstacle, end that branch,
            // and then create 2 new branches
            else {
                currentBranch.setEnd(true);
                splitBranch(branches, currentDirection);
            }
        }

        // * --- *
        // stats for testing only
        // time taken
        long end = System.currentTimeMillis();
        System.out.println("Time in millis: " + (end - start));

        // TODO: HELLO MYSELF, REMEMBER TO REMOVE THIS
        System.out.println("Steps: " + maze.steps);

        // make the gate in the map
        replaceCellAt(VIRTUAL_MAP_ONE_SIDE, VIRTUAL_MAP_ONE_SIDE, 'R');
        if (currentResult.equals(WIN_SIGNAL)) {
            replaceCellAt(virtualCurrentRow, virtualCurrentCol, 'G');
        }

        // write the map into a separate file
        try {
            FileWriter fw = new FileWriter("./resources/virtual-maze.txt");
            for (int i = 0; i < VIRTUAL_MAP_SIZE; i++) {
                fw.write(virtualMap[i] + "\n");
            }
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void splitBranch(LinkedListStack<Branch> branches, String currentDirection) {
        switch (currentDirection) {
            case UP, DOWN -> {
                branches.push(new Branch(LEFT));
                branches.push(new Branch(RIGHT));
            }
            case RIGHT, LEFT -> {
                branches.push(new Branch(DOWN));
                branches.push(new Branch(UP));
            }
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
            case EMPTY_CELL -> {
                return TRUE_SIGNAL;
            }
            default -> {
                if (isOverride) return TRUE_SIGNAL;
                return FALSE_SIGNAL;
            }
        }
    }
}
