package io.github.ccs.assets;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;

import io.github.ccs.ui.BoardTheme;

/** Helper for loading shared board and piece textures through LibGDX's AssetManager. */
public class AssetManagerHelper extends AssetManager {
    private static final String[] NAMES = {"pawn", "knight", "bishop", "rook", "queen", "king"};

    private static final String CLASSIC_BOARD = "board/new chessboard.png";
    private static final String STANDARD_BOARD = "pieces/board.png";
    private static final String DEFAULT_BACKGROUND = "board/new bg.png";
    private static final String NETHER_BACKGROUND = "maps/nether_landscape.jpeg";

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

        // Minecraft-style paths for the classic and nether maps - using front variant
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
        piecePaths.put(BoardTheme.CLASSIC, mc);
        piecePaths.put(BoardTheme.NETHER, new HashMap<>(mc));
    }

    static String boardPath(BoardTheme theme) {
        return (theme == BoardTheme.STANDARD) ? STANDARD_BOARD : CLASSIC_BOARD;
    }

    static String backgroundPath(BoardTheme theme) {
        return (theme == BoardTheme.NETHER) ? NETHER_BACKGROUND : DEFAULT_BACKGROUND;
    }

    /**
     * Queues and synchronously loads all board, piece, and background texture assets for the given map.
     */
    public void loadBoardAssets(BoardTheme theme) {
        load(boardPath(theme), Texture.class);
        load(backgroundPath(theme), Texture.class);

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
        return get(boardPath(theme), Texture.class);
    }

    /** Retrieves the arena background texture for the given map. */
    public Texture getBackgroundTexture(BoardTheme theme) {
        return get(backgroundPath(theme), Texture.class);
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
