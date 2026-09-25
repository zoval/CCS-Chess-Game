package io.github.ccs.assets;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

import io.github.ccs.ui.BoardTheme;

/** Helper for loading shared board and piece textures through LibGDX's AssetManager. */
public class AssetManagerHelper extends AssetManager {
    // ...existing code...
    private static final String[] NAMES = {"pawn", "knight", "bishop", "rook", "queen", "king"};
    
    private final Map<BoardTheme, Map<String, String>> piecePaths = new HashMap<>();

    public AssetManagerHelper() {
        super();
        initializePaths();
    }

    private void initializePaths() {
        // Standard paths
        Map<String, String> standard = new HashMap<>();
        for (String c : new String[]{"white", "black"}) {
            for (String n : NAMES) {
                standard.put(c + "-" + n, "pieces/" + c + "_standard/" + c + "-" + n + ".png");
            }
        }
        piecePaths.put(BoardTheme.STANDARD, standard);

        // Minecraft paths - using front variant
        Map<String, String> mc = new HashMap<>();
        mc.put("white-pawn", "pieces/white_mc/white_pawn[front]-64x64.png");
        mc.put("white-knight", "pieces/white_mc/white_horse[front]-64x64.png");
        mc.put("white-bishop", "pieces/white_mc/white_bishop[front]-64x64.png");
        mc.put("white-rook", "pieces/white_mc/white_rook[front]-64x64.png");
        mc.put("white-queen", "pieces/white_mc/white_queen[front]-64x64.png");
        mc.put("white-king", "pieces/white_mc/white_king[front]-64x64.png");

        mc.put("black-pawn", "pieces/black_mc/Black_pawn[front]-64x64 .png");
        mc.put("black-knight", "pieces/black_mc/Black_horse[front]-64x64 (2).png");
        mc.put("black-bishop", "pieces/black_mc/Black_Bishop[front]-64x64 (1).png");
        mc.put("black-rook", "pieces/black_mc/Black_rook[front]-64x64 .png");
        mc.put("black-queen", "pieces/black_mc/Black_queen[front]-64x64 .png");
        mc.put("black-king", "pieces/black_mc/Black_King[front]-64x64 (2).png");
        piecePaths.put(BoardTheme.MINECRAFT, mc);
    }

    /**
     * Queues and synchronously loads all board and piece texture assets for the given theme.
     */
    public void loadBoardAssets(BoardTheme theme) {
        String boardPath = (theme == BoardTheme.MINECRAFT) ? "board/new chessboard.png" : "pieces/board.png";
        load(boardPath, Texture.class);
        load("board/new bg.png", Texture.class); // Load the new background
        
        Map<String, String> paths = piecePaths.get(theme);
        for (String path : paths.values()) {
            load(path, Texture.class);
        }
        finishLoading();
    }

    /**
     * Retrieves the loaded chessboard texture.
     *                
     * @return chessboard {@link Texture}
     */
    public Texture getBoardTexture(BoardTheme theme) {
        String boardPath = (theme == BoardTheme.MINECRAFT) ? "board/new chessboard.png" : "pieces/board.png";
        return get(boardPath, Texture.class);
    }
    
    public Texture getBackgroundTexture() {
        return get("board/new bg.png", Texture.class);
    }

    /**
     * Retrieves a loaded piece texture for the given theme, color, and piece name.
     *                
     * @param theme board theme
     * @param color piece color ("white" or "black")
     * @param name  piece name ("pawn", "knight", "bishop", "rook", "queen", "king")
     * @return piece {@link Texture}
     */
    public Texture getPieceTexture(BoardTheme theme, String color, String name) {
        String path = piecePaths.get(theme).get(color + "-" + name);
        return get(path, Texture.class);
    }
}
