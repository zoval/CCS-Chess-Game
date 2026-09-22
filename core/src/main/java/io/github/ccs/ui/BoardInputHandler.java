package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;

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

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (board.isGameOver() || animation.isAnimating() || renderer.getBoardSize() <= 1) {
            return true;
        }

        float worldY = Gdx.graphics.getHeight() - screenY;
        float squareSize = renderer.getSquareSize();
        int column = (int) ((screenX - renderer.getBoardX()) / squareSize);
        int row = (int) ((worldY - renderer.getBoardY()) / squareSize);

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
