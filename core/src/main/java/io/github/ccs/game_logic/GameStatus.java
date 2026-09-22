package io.github.ccs.game_logic;

/** Calculates the user-facing status after each turn. */
public final class GameStatus {
    private String text = "White to move";
    private boolean gameOver;

    public String getText() {
        return text;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void reset() {
        text = "White to move";
        gameOver = false;
    }

    public void update(Position position, MoveValidator validator, boolean whiteTurn) {
        boolean inCheck = validator.isKingAttacked(position, whiteTurn);
        if (!validator.hasLegalMove(position, whiteTurn)) {
            gameOver = true;
            text = inCheck
                ? (whiteTurn ? "Checkmate - Black wins" : "Checkmate - White wins")
                : "Stalemate - draw";
        } else {
            text = inCheck
                ? (whiteTurn ? "White is in check" : "Black is in check")
                : (whiteTurn ? "White to move" : "Black to move");
        }
    }
}
