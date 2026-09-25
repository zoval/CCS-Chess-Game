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
    /**
     * Playable 8x8 grid geometry as fractions of the board texture, measured from the art
     * (both board images carry a decorative frame around the actual squares).
     */
    private final float gridX;
    private final float gridY;
    private final float squareW;
    private final float squareH;

    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final AssetManagerHelper assetManager = new AssetManagerHelper();
    private final Texture[][] pieceTextures = new Texture[2][6];
    private final Texture chessboard;

    public BoardRenderer(BoardTheme theme) {
        if (theme == BoardTheme.MINECRAFT) {
            // board/new chessboard.png (750x750): grid at (195,210 from bottom), square ~45.4 px
            gridX = 195f / 750f;
            gridY = 210f / 750f;
            squareW = 45.4f / 750f;
            squareH = 45.4f / 750f;
        } else {
            // pieces/board.png (2050x2050): grid at (23.5,26.5), square 250 px
            gridX = 23.5f / 2050f;
            gridY = 26.5f / 2050f;
            squareW = 250f / 2050f;
            squareH = 250f / 2050f;
        }

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
        drawPieces(board, animation, x, y, size);
        font.setColor(Color.WHITE);
        font.draw(batch, board.getStatusText(), x, y + size + 20);
        batch.end();
    }

    private void drawPieces(Board board, PieceAnimation animation, float boardX, float boardY, float size) {
        int animTargetRow = animation.getToRow();
        int animTargetColumn = animation.getToColumn();

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                int piece = board.getPiece(row, column);
                if (piece == Piece.EMPTY) continue;
                if (animation.isAnimating() && row == animTargetRow && column == animTargetColumn) continue;

                drawPiece(piece, squareX(column, boardX, size), squareY(row, boardY, size), squareW * size, squareH * size);
            }
        }

        if (animation.isAnimating()) {
            float currentX = squareX(animation.getFromColumn(), boardX, size)
                + (squareX(animation.getToColumn(), boardX, size) - squareX(animation.getFromColumn(), boardX, size)) * animation.getProgress();
            float currentY = squareY(animation.getFromRow(), boardY, size)
                + (squareY(animation.getToRow(), boardY, size) - squareY(animation.getFromRow(), boardY, size)) * animation.getProgress();

            drawPiece(animation.getPiece(), currentX, currentY, squareW * size, squareH * size);
        }
    }

    private float squareX(int column, float boardX, float size) {
        return boardX + (gridX + column * squareW) * size;
    }

    private float squareY(int row, float boardY, float size) {
        return boardY + (gridY + row * squareH) * size;
    }

    public float getGridX() { return gridX; }
    public float getGridY() { return gridY; }
    public float getSquareW() { return squareW; }
    public float getSquareH() { return squareH; }

    private void drawPiece(int piece, float x, float y, float width, float height) {
        int colorIndex = Piece.isWhite(piece) ? 0 : 1;
        int typeIndex = Piece.typeOf(piece) - 1;
        batch.draw(pieceTextures[colorIndex][typeIndex], x, y, width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        assetManager.dispose();
    }
}
