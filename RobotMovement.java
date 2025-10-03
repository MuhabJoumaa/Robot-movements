package org.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class RobotMovement {
    private final int K = 100;
    private final String inputFilename, outputFilename;
    private int x = 0, y = 0;

    public RobotMovement(final String inputFilename, final String outputFilename) {
        this.inputFilename = inputFilename;
        this.outputFilename = outputFilename;
    }

    public void processCommands() {
        List<String> output = new ArrayList<>();
        try (var reader = new BufferedReader(new FileReader(this.inputFilename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.equals("Q")) {
                    break;
                }
                this.processCommand(line, output);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        this.writeOutput(output);
    }

    private void processCommand(String command, final List<String> output) {
        boolean needsCorrection = true;
        while (needsCorrection) {
            try {
                this.validateAndExecuteCommand(command, output);
                needsCorrection = false;
            } catch (InvalidCommandException ex) {
                String correctedCommand = command;
                boolean correctionApplied = false;
                if (ex instanceof NoSpaceSeparatorException) {
                    try {
                        correctedCommand = this.handleMissingSpace(command, output);
                        correctionApplied = true;
                    } catch (UninterpretableCommandException e) {
                        output.add("Error: " + e.getMessage());
                        needsCorrection = false;
                    }
                } else if (ex instanceof LowercaseLetterException) {
                    correctedCommand = this.handleLowercaseLetter(command, output);
                    correctionApplied = true;
                } else if (ex instanceof MultipleSymbolsException) {
                    this.handleMultipleSymbols(command, output);
                    needsCorrection = false;
                } else if (ex instanceof NegativeNumberException) {
                    correctedCommand = this.handleNegativeNumber(command, output);
                    correctionApplied = true;
                } else if (ex instanceof ZeroNumberException) {
                    output.add("Removed command with zero number: " + command + ".");
                    needsCorrection = false;
                } else {
                    output.add("Error: " + ex.getMessage());
                    needsCorrection = false;
                }
                if (correctionApplied) {
                    command = correctedCommand;
                }
            }
        }
    }

    private String handleMissingSpace(final String command, final List<String> output) throws UninterpretableCommandException {
        String correctedCommand = this.correctMissingSpace(command);
        output.add("Corrected missing space: " + command + " -> " + correctedCommand + ".");
        return correctedCommand;
    }

    private String handleLowercaseLetter(final String command, final List<String> output) {
        String correctedCommand = command.toUpperCase();
        output.add("Corrected lowercase letter: " + command + " -> " + correctedCommand + ".");
        return correctedCommand;
    }

    private void handleMultipleSymbols(final String command, final List<String> output) {
        output.add("Split multiple symbols into separate commands.");
        String[] parts = command.split(" ");
        for (char ch : parts[0].toCharArray()) {
            String innerCommand = ch + " " + parts[1];
            try {
                this.validateAndExecuteCommand(innerCommand, output);
            } catch (InvalidCommandException ex) {
                this.processCommand(innerCommand, output);
            }
        }
    }

    private String handleNegativeNumber(final String command, final List<String> output) {
        String[] parts = command.split(" ");
        String newDirection = this.oppositeDirection(parts[0]);
        int absNumber = Math.abs(Integer.parseInt(parts[1]));
        String correctedCommand = newDirection + " " + absNumber;
        output.add("Corrected negative number: " + command + " -> " + correctedCommand + ".");
        return correctedCommand;
    }

    private void validateAndExecuteCommand(final String command, final List<String> output) throws InvalidCommandException {
        if (!command.contains(" ")) {
            throw new NoSpaceSeparatorException("No space separator in command: " + command + ".");
        }
        String[] parts = command.split(" ");
        if (parts.length != 2) {
            throw new UninterpretableCommandException("Cannot interpret command: " + command + ".");
        }
        String symbol = parts[0], numberStr = parts[1];
        if (symbol.matches("[nsew]")) {
            throw new LowercaseLetterException("Lowercase letter used: " + symbol + ".");
        }
        if (!symbol.matches("[NSEW]")) {
            if (symbol.matches("[A-Z]")) {
                throw new InvalidCapitalLetterException("Invalid capital letter used: " + symbol + ".");
            }
            if (symbol.length() == 1) {
                throw new InvalidSymbolException("Invalid symbol used: " + symbol + ".");
            }
        }
        if (symbol.length() > 1) {
            throw new MultipleSymbolsException("Multiple symbols used: " + symbol + ".");
        }
        int number;
        try {
            number = Integer.parseInt(numberStr);
        } catch (NumberFormatException e) {
            throw new InvalidNumberFormatException("Invalid number format used: " + numberStr + ".");
        }
        if (number < 0) {
            throw new NegativeNumberException("Negative number used: " + number + ".");
        }
        if (number == 0) {
            throw new ZeroNumberException("Zero number not allowed.");
        }
        if (number > this.K) {
            throw new NumberExceedsLimitException("Number exceeds limit " + this.K + ": " + number + ".");
        }
        this.executeCommand(symbol, number);
        output.add("Executed command: " + symbol + " " + number + " -> Position: (" + this.x + ", " + this.y + ").");
    }

    private String correctMissingSpace(final String command) throws UninterpretableCommandException {
        for (int i = 1; i < command.length(); i++) {
            if (Character.isLetter(command.charAt(i - 1)) && Character.isDigit(command.charAt(i))) {
                return command.substring(0, i) + " " + command.substring(i);
            }
        }
        throw new UninterpretableCommandException("Cannot interpret command: " + command + ".");
    }

    private void executeCommand(final String symbol, final int number) {
        switch (symbol) {
            case "N" -> this.y += number;
            case "S" -> this.y -= number;
            case "E" -> this.x += number;
            case "W" -> this.x -= number;
        }
    }

    private String oppositeDirection(final String direction) {
        return switch (direction) {
            case "N" -> "S";
            case "S" -> "N";
            case "E" -> "W";
            case "W" -> "E";
            default -> direction;
        };
    }

    private void writeOutput(final List<String> output) {
        try (var writer = new BufferedWriter(new FileWriter(this.outputFilename))) {
            for (int i = 0; i < output.size(); i++) {
                writer.write(output.get(i));
                if (i < output.size() - 1) {
                    writer.newLine();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
