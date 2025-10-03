package org.example;

public class Main {
    public static void main(String[] args) {
        RobotMovement robotMovement = new RobotMovement("C:\\Users\\muhab\\Documents\\robot_movements_input.txt",
                "C:\\Users\\muhab\\Documents\\robot_movements_output.txt");
        robotMovement.processCommands();
    }
}