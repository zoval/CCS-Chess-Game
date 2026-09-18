package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;

/** Handles drawing the chessboard, pieces, animations, and game status. */
public class BoardRenderer implements Disposable {
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Texture chessboard = new Texture(Gdx.files.internal("board.png"));
    private final Texture[][] pieceTextures = new Texture[2][6];

    private float boardX;
    private float boardY;
    private float boardSize;

    public BoardRenderer() {
        String[] colors = {"white", "black"};
        String[] names = {"pawn", "knight", "bishop", "rook", "queen", "king"};
        for (int color = 0; color < 2; color++) {
            for (int type = 0; type < 6; type++) {
                pieceTextures[color][type] = new Texture(
                    Gdx.files.internal(colors[color] + "-" + names[type] + ".png"));
            }
        }
    }

    public void updateLayout() {
        boardSize = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - 70);
        boardSize = Math.max(1, boardSize - 20);
        boardX = (Gdx.graphics.getWidth() - boardSize) / 2f;
        boardY = (Gdx.graphics.getHeight() - boardSize - 40) / 2f;
    }

    public void render(Board board, PieceAnimation animation) {
        updateLayout();

        batch.begin();
        batch.draw(chessboard, boardX, boardY, boardSize, boardSize);
        drawPieces(board, animation);
        font.setColor(Color.WHITE);
        font.draw(batch, board.getStatusText(), 20, Gdx.graphics.getHeight() - 20);
        batch.end();
    }

    private void drawPieces(Board board, PieceAnimation animation) {
        float squareSize = getSquareSize();
        int animTargetRow = animation.getToRow();
        int animTargetColumn = animation.getToColumn();

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                int piece = board.getPiece(row, column);
                if (piece == Piece.EMPTY) continue;
                if (animation.isAnimating() && row == animTargetRow && column == animTargetColumn) continue;

                drawPiece(piece, boardX + column * squareSize, boardY + row * squareSize, squareSize);
            }
        }

        if (animation.isAnimating()) {
            float startX = boardX + animation.getFromColumn() * squareSize;
            float startY = boardY + animation.getFromRow() * squareSize;
            float targetX = boardX + animation.getToColumn() * squareSize;
            float targetY = boardY + animation.getToRow() * squareSize;
            float currentX = startX + (targetX - startX) * animation.getProgress();
            float currentY = startY + (targetY - startY) * animation.getProgress();

            drawPiece(animation.getPiece(), currentX, currentY, squareSize);
        }
    }

    private void drawPiece(int piece, float x, float y, float size) {
        int colorIndex = Piece.isWhite(piece) ? 0 : 1;
        int typeIndex = Piece.typeOf(piece) - 1;
        batch.draw(pieceTextures[colorIndex][typeIndex], x, y, size, size);
    }

    public float getBoardX() {
        return boardX;
    }

    public float getBoardY() {
        return boardY;
    }

    public float getBoardSize() {
        return boardSize;
    }

    public float getSquareSize() {
        return boardSize / 8f;
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        chessboard.dispose();
        for (Texture[] colorGroup : pieceTextures) {
            for (Texture texture : colorGroup) {
                if (texture != null) texture.dispose();
            }
        }
    }
}
