package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

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
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ScreenViewport viewport = new ScreenViewport(camera);
    private final Vector2 touchPoint = new Vector2();

    private float boardX;
    private float boardY;
    private float boardSize;

    public BoardRenderer() {
        String[] colors = {"white", "black"};
        String[] names = {"pawn", "knight", "bishop", "rook", "queen", "king"};

        assetManager.loadBoardAssets();
        chessboard = assetManager.getBoardTexture();

        for (int color = 0; color < 2; color++) {
            for (int type = 0; type < 6; type++) {
                pieceTextures[color][type] = assetManager.getPieceTexture(colors[color], names[type]);
            }
        }

        int width = Gdx.graphics.getWidth();
        int height = Gdx.graphics.getHeight();
        if (width > 0 && height > 0) {
            resize(width, height);
        }
    }

    /**
     * Updates viewport dimensions and recalculates board positioning upon window resize.
     * Guards against zero dimensions when the window is minimized.
     *
     * @param width  new screen width in pixels
     * @param height new screen height in pixels
     */
    public final void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        viewport.update(width, height, true);
        updateLayout(width, height);
    }

    /**
     * Recalculates board positioning using the current viewport or graphics dimensions.
     */
    public final void updateLayout() {
        float width = viewport.getWorldWidth() > 0 ? viewport.getWorldWidth() : Gdx.graphics.getWidth();
        float height = viewport.getWorldHeight() > 0 ? viewport.getWorldHeight() : Gdx.graphics.getHeight();
        updateLayout(width, height);
    }

    /**
     * Calculates the centered position and proportional dimensions of the chessboard.
     *
     * @param width  viewport world width
     * @param height viewport world height
     */
    private void updateLayout(float width, float height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        boardSize = Math.min(width, height - 70);
        boardSize = Math.max(1, boardSize - 20);
        boardX = (width - boardSize) / 2f;
        boardY = (height - boardSize - 40) / 2f;
    }

    /**
     * Renders the chessboard, all active and animating pieces, and status text.
     *
     * @param board     current game board state
     * @param animation piece movement animation state
     */
    public void render(Board board, PieceAnimation animation) {
        if (viewport.getScreenWidth() <= 0 || viewport.getScreenHeight() <= 0) {
            return;
        }

        viewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(chessboard, boardX, boardY, boardSize, boardSize);
        drawPieces(board, animation);
        font.setColor(Color.WHITE);
        float statusX = Math.max(20, boardX);
        float statusY = Math.min(viewport.getWorldHeight() - 15, boardY + boardSize + 30);
        font.draw(batch, board.getStatusText(), statusX, statusY);
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

    /**
     * Translates screen pixel coordinates to world coordinates using the active viewport.
     *
     * @param screenX screen x coordinate
     * @param screenY screen y coordinate
     * @return unprojected world coordinates vector
     */
    public Vector2 unproject(int screenX, int screenY) {
        touchPoint.set(screenX, screenY);
        return viewport.unproject(touchPoint);
    }

    public ScreenViewport getViewport() {
        return viewport;
    }

    public OrthographicCamera getCamera() {
        return camera;
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        assetManager.dispose();
    }
}
