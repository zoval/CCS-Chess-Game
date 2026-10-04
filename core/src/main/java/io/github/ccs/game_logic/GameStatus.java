package io.github.ccs.game_logic;

/** Calculates the user-facing status after each turn. */
public final class GameStatus {
    private String text = "White to move";
    private boolean gameOver;
    private boolean fiftyMoveDraw;

    public String getText() {
        return text;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean isFiftyMoveDraw() {
        return fiftyMoveDraw;
    }

    public void reset() {
        text = "White to move";
        gameOver = false;
        fiftyMoveDraw = false;
    }

    public void update(Position position, MoveValidator validator, boolean whiteTurn, boolean fiftyMoveDraw) {
        update(position, validator, whiteTurn, fiftyMoveDraw, false, false);
    }

    /**
     * Full status update. Draw checks run in order (fifty-move, threefold
     * repetition, insufficient material) before mate/stalemate so a draw is
     * never masked; the exact legacy strings are preserved.
     */
    public void update(Position position, MoveValidator validator, boolean whiteTurn,
            boolean fiftyMoveDraw, boolean threefoldRepetition, boolean insufficientMaterial) {
        this.fiftyMoveDraw = fiftyMoveDraw;
        if (fiftyMoveDraw) {
            gameOver = true;
            text = "Draw - fifty-move rule";
            return;
        }

        if (threefoldRepetition) {
            gameOver = true;
            text = "Draw - threefold repetition";
            return;
        }

        if (insufficientMaterial) {
            gameOver = true;
            text = "Draw - insufficient material";
            return;
        }

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

    /** Ends the game with the given draw reason (e.g., "fifty-move rule"). */
    public void declareDraw(String reason) {
        text = "Draw - " + reason;
        gameOver = true;
    }

    /** Ends the game by resignation; the non-resigning side wins. */
    public void declareResignation(boolean resignedWhite) {
        text = resignedWhite ? "Resignation - Black wins" : "Resignation - White wins";
        gameOver = true;
    }

    /** Ends the game by both players agreeing to a draw. */
    public void declareAgreedDraw() {
        text = "Draw - agreement";
        gameOver = true;
    }

    /** Ends the game on time; the side that did not flag wins. */
    public void declareTimeout(boolean whiteFlagged) {
        text = whiteFlagged ? "Timeout - Black wins" : "Timeout - White wins";
        gameOver = true;
    }

    /** Restores a previously stored status (history navigation, save/load). */
    public void restore(String restoredText, boolean restoredGameOver) {
        text = restoredText;
        gameOver = restoredGameOver;
    }
}
