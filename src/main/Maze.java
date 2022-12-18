package main;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

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
        rows = 1000;
        cols = 1000;
        map = new String[rows];

        // read maze from file
        int index = 0;
        try {
            File file = new File("./resources/maze-30x100.txt");
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
        robotCol = 2;
        steps = 0;
    }

    public String go(String direction) {
        if (!direction.equals("UP") && !direction.equals("DOWN") && !direction.equals("LEFT") && !direction.equals("RIGHT")) {
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


