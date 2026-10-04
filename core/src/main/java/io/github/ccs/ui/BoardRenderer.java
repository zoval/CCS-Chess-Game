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
    private static final float SELECT_TINT_ALPHA = 0.40f;
    private static final float HOVER_TINT_ALPHA = 0.14f;
    private static final float DOT_SIZE = 0.34f;
    private static final float RING_SIZE = 0.94f;
    private static final float CHECK_BASE_ALPHA = 0.30f;
    private static final float CHECK_PULSE_ALPHA = 0.18f;
    private static final float CHECK_PULSE_SPEED = 5f;

    private static final Color SELECT_TINT = new Color(1f, 0.82f, 0.25f, 1f);
    private static final Color HOVER_TINT = new Color(1f, 1f, 1f, 1f);
    private static final Color LIGHT_SQUARE_DOT = new Color(0.05f, 0.05f, 0.05f, 1f);
    private static final Color DARK_SQUARE_DOT = new Color(0.95f, 0.95f, 0.95f, 1f);
    private static final Color RING_TINT = new Color(0.95f, 0.28f, 0.20f, 1f);
    private static final Color CHECK_TINT = new Color(1f, 0.12f, 0.08f, 1f);
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
    private final Texture pixelTexture = ProceduralTextures.whitePixel();
    private final Texture dotTexture = ProceduralTextures.softDot(64);
    private final Texture ringTexture = ProceduralTextures.softRing(64);
    private float aidTime;

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
     * Renders the chessboard, visual aids, all active and animating pieces, and status text.
     *
     * @param board          current game board state
     * @param animation      piece movement animation state
     * @param x              board x coordinate
     * @param y              board y coordinate
     * @param size           board size
     * @param visualAids     whether move hints and highlights should be drawn
     * @param selectedRow    currently selected piece row, -1 if none
     * @param selectedColumn currently selected piece column, -1 if none
     * @param hoverRow       row under the mouse cursor, -1 if none
     * @param hoverColumn    column under the mouse cursor, -1 if none
     * @param delta          seconds since the last frame, drives the check pulse
     */
    public void render(Board board, PieceAnimation animation, float x, float y, float size,
                       boolean visualAids, int selectedRow, int selectedColumn,
                       int hoverRow, int hoverColumn, float delta) {
        aidTime += delta;
        batch.begin();
        // Draw background to fill the screen
        batch.draw(assetManager.getBackgroundTexture(), 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.draw(chessboard, x, y, size, size);
        if (visualAids) {
            drawVisualAids(board, x, y, size, selectedRow, selectedColumn, hoverRow, hoverColumn, false);
        }
        resetBatchColor();
        drawPieces(board, animation, x, y, size);
        if (visualAids) {
            drawVisualAids(board, x, y, size, selectedRow, selectedColumn, hoverRow, hoverColumn, true);
        }
        resetBatchColor();
        font.setColor(Color.WHITE);
        font.draw(batch, board.getStatusText(), x, y + size + 20);
        batch.end();
    }

    /**
     * Draws the aid overlays. {@code topLayer} false draws square tints, move dots, and the
     * check pulse under the pieces; true draws capture rings over the pieces.
     */
    private void drawVisualAids(Board board, float boardX, float boardY, float size,
                                int selectedRow, int selectedColumn,
                                int hoverRow, int hoverColumn, boolean topLayer) {
        if (!topLayer) {
            if (hoverRow >= 0 && (hoverRow != selectedRow || hoverColumn != selectedColumn)) {
                drawSquareTint(hoverRow, hoverColumn, boardX, boardY, size, HOVER_TINT, HOVER_TINT_ALPHA);
            }
            if (selectedRow >= 0) {
                drawSquareTint(selectedRow, selectedColumn, boardX, boardY, size, SELECT_TINT, SELECT_TINT_ALPHA);
                for (int[] move : board.getLegalMoves(selectedRow, selectedColumn)) {
                    int row = move[0];
                    int column = move[1];
                    if (board.getPiece(row, column) == Piece.EMPTY) {
                        drawMoveDot(row, column, boardX, boardY, size);
                    }
                }
            }
            if (board.isCurrentKingInCheck()) {
                int[] king = board.getKingPosition(board.isWhiteTurn());
                if (king != null) {
                    float pulse = CHECK_BASE_ALPHA
                        + CHECK_PULSE_ALPHA * (0.5f + 0.5f * (float) Math.sin(aidTime * CHECK_PULSE_SPEED));
                    drawSquareTint(king[0], king[1], boardX, boardY, size, CHECK_TINT, pulse);
                }
            }
        } else if (selectedRow >= 0) {
            int selectedPiece = board.getPiece(selectedRow, selectedColumn);
            boolean pawnDiagonal = Piece.typeOf(selectedPiece) == Piece.PAWN;
            for (int[] move : board.getLegalMoves(selectedRow, selectedColumn)) {
                int row = move[0];
                int column = move[1];
                boolean capture = board.getPiece(row, column) != Piece.EMPTY
                    || (pawnDiagonal && column != selectedColumn);
                if (capture) {
                    drawCaptureRing(row, column, boardX, boardY, size);
                }
            }
        }
    }

    private void drawSquareTint(int row, int column, float boardX, float boardY, float size,
                                Color tint, float alpha) {
        batch.setColor(tint.r, tint.g, tint.b, alpha);
        batch.draw(pixelTexture,
            squareX(column, boardX, size), squareY(row, boardY, size),
            squareW * size, squareH * size);
    }

    private void drawMoveDot(int row, int column, float boardX, float boardY, float size) {
        Color dotColor = (row + column) % 2 == 0 ? DARK_SQUARE_DOT : LIGHT_SQUARE_DOT;
        float dotSize = squareW * size * DOT_SIZE;
        float centerX = squareX(column, boardX, size) + squareW * size / 2f;
        float centerY = squareY(row, boardY, size) + squareH * size / 2f;
        batch.setColor(dotColor.r, dotColor.g, dotColor.b, 0.45f);
        batch.draw(dotTexture, centerX - dotSize / 2f, centerY - dotSize / 2f, dotSize, dotSize);
    }

    private void drawCaptureRing(int row, int column, float boardX, float boardY, float size) {
        float ringSize = squareW * size * RING_SIZE;
        float centerX = squareX(column, boardX, size) + squareW * size / 2f;
        float centerY = squareY(row, boardY, size) + squareH * size / 2f;
        batch.setColor(RING_TINT.r, RING_TINT.g, RING_TINT.b, 0.85f);
        batch.draw(ringTexture, centerX - ringSize / 2f, centerY - ringSize / 2f, ringSize, ringSize);
    }

    private void resetBatchColor() {
        batch.setColor(Color.WHITE);
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
        pixelTexture.dispose();
        dotTexture.dispose();
        ringTexture.dispose();
    }
}
