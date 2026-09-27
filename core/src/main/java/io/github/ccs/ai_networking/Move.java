package io.github.ccs.ai_networking;

public class Move {

    public int fromRow;
    public int fromColumn;
    public int toRow;
    public int toColumn;

    public Move(int fromRow, int fromColumn, int toRow, int toColumn) {
        this.fromRow = fromRow;
        this.fromColumn = fromColumn;
        this.toRow = toRow;
        this.toColumn = toColumn;
    }

    public boolean isCapture() {
        return false;
    }
}