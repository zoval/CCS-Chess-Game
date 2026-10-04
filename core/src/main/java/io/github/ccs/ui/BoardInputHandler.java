package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;
import io.github.ccs.sound.SoundManager;

/** Processes touch input for tile selection and piece movement on the chess board. */
public class BoardInputHandler extends InputAdapter {
    private final Board board;
    private final ChessGameScreen gameScreen;
    private int selectedRow = -1;
    private int selectedColumn = -1;
    private int hoverRow = -1;
    private int hoverColumn = -1;
    private boolean inputBlocked;

    public BoardInputHandler(Board board, ChessGameScreen gameScreen) {
        this.board = board;
        this.gameScreen = gameScreen;
    }

    /**
     * Handles keyboard shortcuts for sound toggle and returning to the menu.
     *
     * @param keycode the keycode of the pressed key
     * @return true if the event was handled
     */
    @Override
    public boolean keyDown(int keycode) {
        if (inputBlocked) {
            return false;
        }
        if (keycode == Input.Keys.M) {
            SoundManager.getInstance().toggleSound();
            return true;
        }
        if (keycode == Input.Keys.ESCAPE) {
            gameScreen.backToMenu();
            return true;
        }
        return false;
    }

    /**
     * Tracks the board square under the mouse cursor so the renderer can highlight it.
     */
    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        int[] square = screenToSquare(screenX, screenY);
        hoverRow = square[0];
        hoverColumn = square[1];
        return false;
    }

    /**
     * Translates screen coordinates to board squares and performs piece selection or movement.
     */

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (inputBlocked) {
            return true;
        }
        float boardSize = gameScreen.getBoardSize();
        if (board.isGameOver() || gameScreen.isAnimating() || boardSize <= 1) {
            return true;
        }

        float size = gameScreen.getBoardSize();
        float boardX = gameScreen.getBoardX();
        float boardY = gameScreen.getBoardY();

        // Invert Y because screen coordinates are 0,0 at top-left, but Chessboard is 0,0 at bottom-left.
        // ulol Map into the playable grid, which is inset within the board artwork by a decorative frame.
        float gridX = (screenX - boardX) / size;
        float gridY = (Gdx.graphics.getHeight() - screenY - boardY) / size;
        int column = (int) Math.floor((gridX - gameScreen.getGridX()) / gameScreen.getSquareW());
        int row = (int) Math.floor((gridY - gameScreen.getGridY()) / gameScreen.getSquareH());

        if (row < 0 || row >= 8 || column < 0 || column >= 8) {
            return true;
        }

        if (selectedRow < 0) {
            int piece = board.getPiece(row, column);
            if (piece != Piece.EMPTY && Piece.isWhite(piece) == board.isWhiteTurn()) {
                selectedRow = row;
                selectedColumn = column;
                SoundManager.getInstance().playUIClick();
            }
            return true;
        }

        if (row == selectedRow && column == selectedColumn) {
            clearSelection();
            SoundManager.getInstance().playUIClick();
            return true;
        }

        if (board.isPromotionMove(selectedRow, selectedColumn, row, column)) {
            gameScreen.promptPromotion(selectedRow, selectedColumn, row, column);
            return true;
        }

        gameScreen.applyHumanMove(selectedRow, selectedColumn, row, column);
        clearSelection();
        return true;
    }

    public void clearSelection() {
        selectedRow = -1;
        selectedColumn = -1;
    }

    public int getSelectedRow() {
        return selectedRow;
    }

    public int getSelectedColumn() {
        return selectedColumn;
    }

    public int getHoverRow() {
        return hoverRow;
    }

    public int getHoverColumn() {
        return hoverColumn;
    }

    /** Blocks board input (e.g. while the settings dialog is open) and clears hover state. */
    public void setInputBlocked(boolean inputBlocked) {
        this.inputBlocked = inputBlocked;
        if (inputBlocked) {
            hoverRow = -1;
            hoverColumn = -1;
        }
    }

    /**
     * Maps screen coordinates to board squares; returns {@code [-1, -1]} when off the grid.
     * Rendering uses raw screen coordinates (no viewport transform), so no camera math is needed.
     */
    private int[] screenToSquare(int screenX, int screenY) {
        float size = gameScreen.getBoardSize();
        float boardX = gameScreen.getBoardX();
        float boardY = gameScreen.getBoardY();

        // Invert Y because screen coordinates are 0,0 at top-left, but the chessboard is 0,0 at bottom-left.
        // Map into the playable grid, which is inset within the board artwork by a decorative frame.
        float gridX = (screenX - boardX) / size;
        float gridY = (Gdx.graphics.getHeight() - screenY - boardY) / size;
        int column = (int) Math.floor((gridX - gameScreen.getGridX()) / gameScreen.getSquareW());
        int row = (int) Math.floor((gridY - gameScreen.getGridY()) / gameScreen.getSquareH());
        if (row < 0 || row >= 8 || column < 0 || column >= 8) {
            return new int[]{-1, -1};
        }
        return new int[]{row, column};
    }
}
