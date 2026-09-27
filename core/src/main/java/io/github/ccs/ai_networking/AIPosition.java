package io.github.ccs.ai_networking;

public class AIPosition {

    private final int[][] board = new int[8][8];
    private int lastFromRow;
    private int lastToRow;
    private int lastFromColumn;
    private int lastToColumn;

    public AIPosition() {
    }

    public AIPosition(AIPosition other) {
        for (int row = 0; row < board.length; row++) {
            System.arraycopy(other.board[row], 0, board[row], 0, board[row].length);
        }

        lastFromRow = other.lastFromRow;
        lastToRow = other.lastToRow;
        lastFromColumn = other.lastFromColumn;
        lastToColumn = other.lastToColumn;
    }

    public int get(int row, int column) {
        return board[row][column];
    }

    public void set(int row, int column, int piece) {
        board[row][column] = piece;
    }

    public void apply(Move move) {
        int piece = board[move.fromRow][move.fromColumn];

        board[move.fromRow][move.fromColumn] = 0;
        board[move.toRow][move.toColumn] = piece;

        lastFromRow = move.fromRow;
        lastToRow = move.toRow;
        lastFromColumn = move.fromColumn;
        lastToColumn = move.toColumn;
    }
}