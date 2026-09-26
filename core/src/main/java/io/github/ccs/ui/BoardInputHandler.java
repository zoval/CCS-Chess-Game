package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;

import io.github.ccs.MainGame;
import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;
import io.github.ccs.sound.SoundManager;

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
                Gdx.graphics.setWindowedMode(MainGame.GAME_WINDOW_WIDTH, MainGame.GAME_WINDOW_HEIGHT);
            } else {
                Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
            }
            return true;
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
     * Translates screen coordinates to board squares and performs piece selection or movement.
     */

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        float boardSize = gameScreen.getBoardSize();
        if (board.isGameOver() || animation.isAnimating() || boardSize <= 1) {
            return true;
        }

        // Use direct screen coordinates because rendering is no longer using a viewport transformation
        float size = gameScreen.getBoardSize();
        float boardX = gameScreen.getBoardX();
        float boardY = gameScreen.getBoardY();

        // Invert Y because screen coordinates are 0,0 at top-left, but Chessboard is 0,0 at bottom-left.
        // Map into the playable grid, which is inset within the board artwork by a decorative frame.
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

        int targetPiece = board.getPiece(row, column);
        int piece = board.getPiece(selectedRow, selectedColumn);
        boolean isCapture = targetPiece != Piece.EMPTY
            || (Piece.typeOf(piece) == Piece.PAWN && selectedColumn != column);

        if (board.move(selectedRow, selectedColumn, row, column)) {
            animation.start(piece, selectedRow, selectedColumn, row, column);

            if (board.isGameOver()) {
                String status = board.getStatusText();
                if (status != null && status.startsWith("Checkmate")) {
                    SoundManager.getInstance().playCheckmate();
                } else {
                    SoundManager.getInstance().playStalemate();
                }
            } else {
                String status = board.getStatusText();
                if (status != null && status.contains("check")) {
                    SoundManager.getInstance().playLose();
                } else if (isCapture) {
                    SoundManager.getInstance().playPieceCapture();
                } else {
                    SoundManager.getInstance().playPieceMove();
                }
            }
        }
        clearSelection();
        return true;
    }

    public void clearSelection() {
        selectedRow = -1;
        selectedColumn = -1;
    }
}
