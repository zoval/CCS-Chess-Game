package io.github.ccs.assets;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

/** Helper for loading shared board and piece textures through LibGDX's AssetManager. */
public class AssetManagerHelper extends AssetManager {
    private static final String[] COLORS = {"white", "black"};
    private static final String[] NAMES = {"pawn", "knight", "bishop", "rook", "queen", "king"};

    public AssetManagerHelper() {
        super();
    }

    /**
     * Queues and synchronously loads all board and standard piece texture assets.
     */
    public void loadBoardAssets() {
        load("pieces/board.png", Texture.class);
        for (String color : COLORS) {
            for (String name : NAMES) {
                load("pieces/" + color + "_standard/" + color + "-" + name + ".png", Texture.class);
            }
        }
        finishLoading();
    }

    /**
     * Retrieves the loaded chessboard texture.
     *
     * @return chessboard {@link Texture}
     */
    public Texture getBoardTexture() {
        return get("pieces/board.png", Texture.class);
    }

    /**
     * Retrieves a loaded piece texture for the given color and piece name.
     *
     * @param color piece color ("white" or "black")
     * @param name  piece name ("pawn", "knight", "bishop", "rook", "queen", "king")
     * @return piece {@link Texture}
     */
    public Texture getPieceTexture(String color, String name) {
        return get("pieces/" + color + "_standard/" + color + "-" + name + ".png", Texture.class);
    }
}
