package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

import io.github.ccs.assets.AssetManagerHelper;
import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;

/** Handles drawing the chessboard, pieces, animations, and game status with responsive scaling. */
public class BoardRenderer implements Disposable {
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final AssetManagerHelper assetManager = new AssetManagerHelper();
    private final Texture[][] pieceTextures = new Texture[2][6];
    private final Texture chessboard;

    public BoardRenderer(BoardTheme theme) {
        String[] colors = {"white", "black"};
        String[] names = {"pawn", "knight", "bishop", "rook", "queen", "king"};

        assetManager.loadBoardAssets(theme);
        chessboard = assetManager.getBoardTexture(theme);

        for (int color = 0; color < 2; color++) {
            for (int type = 0; type < 6; type++) {
                pieceTextures[color][type] = assetManager.getPieceTexture(theme, colors[color], names[type]);
            }
        }
    }

    /**
     * Renders the chessboard, all active and animating pieces, and status text.
     *
     * @param board     current game board state
     * @param animation piece movement animation state
     * @param x         board x coordinate
     * @param y         board y coordinate
     * @param size     board size
     */
    public void render(Board board, PieceAnimation animation, float x, float y, float size) {
        batch.begin();
        // Draw background to fill the screen
        batch.draw(assetManager.getBackgroundTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        
        batch.draw(chessboard, x, y, size, size);
        drawPieces(board, animation, x, y, size / 8f);
        font.setColor(Color.WHITE);
        font.draw(batch, board.getStatusText(), x, y + size + 20);
        batch.end();
    }

    private void drawPieces(Board board, PieceAnimation animation, float boardX, float boardY, float squareSize) {
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

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        assetManager.dispose();
    }
}
