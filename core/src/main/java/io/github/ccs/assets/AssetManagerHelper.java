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

    public void loadBoardAssets() {
        load("pieces/board.png", Texture.class);
        for (String color : COLORS) {
            for (String name : NAMES) {
                load("pieces/" + color + "-" + name + ".png", Texture.class);
            }
        }
        finishLoading();
    }

    public Texture getBoardTexture() {
        return get("pieces/board.png", Texture.class);
    }

    public Texture getPieceTexture(String color, String name) {
        return get("pieces/" + color + "-" + name + ".png", Texture.class);
    }
}
