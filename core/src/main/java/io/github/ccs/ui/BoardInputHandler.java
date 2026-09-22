package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;

import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;

/** Processes touch input for tile selection and piece movement on the chess board. */
public class BoardInputHandler extends InputAdapter {
    private final Board board;
    private final BoardRenderer renderer;
    private final PieceAnimation animation;
    private int selectedRow = -1;
    private int selectedColumn = -1;

    public BoardInputHandler(Board board, BoardRenderer renderer, PieceAnimation animation) {
        this.board = board;
        this.renderer = renderer;
        this.animation = animation;
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
                Gdx.graphics.setWindowedMode(640, 480);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
            return true;
        }
        return false;
    }

    /**
     * Translates screen coordinates to board squares using viewport unprojection
     * and performs piece selection or movement.
     *
     * @param screenX screen x position of pointer
     * @param screenY screen y position of pointer
     * @param pointer pointer index
     * @param button  mouse button clicked
     * @return true if the input was processed
     */
    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (board.isGameOver() || animation.isAnimating() || renderer.getBoardSize() <= 1) {
            return true;
        }

        Vector2 worldCoords = renderer.unproject(screenX, screenY);
        float squareSize = renderer.getSquareSize();
        if (squareSize <= 0) {
            return true;
        }

        int column = (int) Math.floor((worldCoords.x - renderer.getBoardX()) / squareSize);
        int row = (int) Math.floor((worldCoords.y - renderer.getBoardY()) / squareSize);

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
