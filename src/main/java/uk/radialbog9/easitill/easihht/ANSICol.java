package uk.radialbog9.easitill.easihht;

public enum ANSICol {
    RESET("\u001b[0m"),
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    PURPLE("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[37m"),
    BLACK_BG("\u001B[40m"),
    RED_BG("\u001B[401"),
    GREEN_BG("\u001B[42m"),
    YELLOW_BG("\u001B[43m"),
    BLUE_BG("\u001B[44m"),
    PURPLE_BG("\u001B[45m"),
    CYAN_BG("\u001B[46m"),
    WHITE_BG("\u001B[47m");


    private final String colCode;
    ANSICol(String colCode) { this.colCode = colCode; }
    public String toString() { return this.colCode; }
}
