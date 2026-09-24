package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;

/** Processes touch input for tile selection and piece movement on the chess board. */
public class BoardInputHandler extends InputAdapter {
    private final Board board;
    private final ChessGameScreen gameScreen;
    private final PieceAnimation animation;
    private int selectedRow = -1;
    private int selectedColumn = -1;

    public BoardInputHandler(Board board, PieceAnimation animation, ChessGameScreen gameScreen) {
        this.board = board;
        this.animation = animation;
        this.gameScreen = gameScreen;
    }

    /**
     * Handles keyboard shortcuts including F11 for toggling fullscreen mode.
     *
     * @param keycode the keycode of the pressed key
     * @return true if the event was handled
     */
    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.F11) {
            if (Gdx.graphics.isFullscreen()) {
                Gdx.graphics.setWindowedMode(1024, 572);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
            return true;
        }
        return false;
    }

    /**
     * Translates screen coordinates to board squares and performs piece selection or movement.
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float boardSize = gameScreen.getBoardSize();
        if (board.isGameOver() || animation.isAnimating() || boardSize <= 1) {
            return true;
        }

        // Use direct screen coordinates because rendering is no longer using a viewport transformation
        float squareSize = boardSize / 8f;
        float boardX = gameScreen.getBoardX();
        float boardY = gameScreen.getBoardY();

        // Invert Y because screen coordinates are 0,0 at top-left, but Chessboard is 0,0 at bottom-left
        int column = (int) Math.floor((screenX - boardX) / squareSize);
        int row = (int) Math.floor((Gdx.graphics.getHeight() - screenY - boardY) / squareSize);

        if (row < 0 || row >= 8 || column < 0 || column >= 8) {
            return true;
        }

        if (selectedRow < 0) {
            int piece = board.getPiece(row, column);
            if (piece != Piece.EMPTY && Piece.isWhite(piece) == board.isWhiteTurn()) {
                selectedRow = row;
                selectedColumn = column;
            }
            return true;
        }

        if (row == selectedRow && column == selectedColumn) {
            clearSelection();
            return true;
        }

        int piece = board.getPiece(selectedRow, selectedColumn);
        if (board.move(selectedRow, selectedColumn, row, column)) {
            animation.start(piece, selectedRow, selectedColumn, row, column);
        }
        clearSelection();
        return true;
    }

    public void clearSelection() {
        selectedRow = -1;
        selectedColumn = -1;
    }
}
