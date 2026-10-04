package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import io.github.ccs.MainGame;
import io.github.ccs.ai_networking.AIFactory;
import io.github.ccs.ai_networking.AIUtils;
import io.github.ccs.ai_networking.ChessAI;
import io.github.ccs.ai_networking.Difficulty;
import io.github.ccs.ai_networking.Move;
import io.github.ccs.backend.SaveGame;
import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.GameClock;
import io.github.ccs.game_logic.GameState;
import io.github.ccs.game_logic.Piece;
import io.github.ccs.sound.SoundManager;

/** Screen managing the chess game session and coordinating renderer and input components. */
public class ChessGameScreen extends ScreenAdapter {
    private final Board board = new Board();
    private final PieceAnimation animation = new PieceAnimation();
    private final MainGame game;
    private GameState loadedState;
    private BoardRenderer renderer;
    private BoardInputHandler inputHandler;
    private Stage hudStage;
    private ChessHud hud;
    private SettingsDialog settingsDialog;
    private PromotionDialog promotionDialog;
    private GameOverOverlay gameOverOverlay;
    private boolean gameOverAnnounced;
    private GameClock clock;
    private ChessAI bot;
    private boolean aiThinking;
    private boolean aiMovePending;
    private float aiMoveDelay;
    private float boardX, boardY, boardSize;

    public ChessGameScreen(MainGame game) {
        this(game, null);
    }

    public ChessGameScreen(MainGame game, GameState loadedState) {
        this.game = game;
        this.loadedState = loadedState;
    }

    @Override
    public void show() {
        applyLoadedState();
        clock = new GameClock(game.getTimeControlMinutes() * 60000L);
        if (loadedState != null) {
            clock.setWhiteMillis(loadedState.getWhiteMillis());
            clock.setBlackMillis(loadedState.getBlackMillis());
        }
        renderer = new BoardRenderer(game.getSelectedMap());
        renderer.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        calculateLayout();
        SoundManager.getInstance().initialize();
        SoundManager.getInstance().playGameMusic();

        hudStage = new Stage(new ScreenViewport());
        settingsDialog = new SettingsDialog(game);
        promotionDialog = new PromotionDialog();
        inputHandler = new BoardInputHandler(board, this);
        hud = new ChessHud(board, game, renderer, settingsDialog);
        renderer.setStatusTextVisible(false);
        hud.layout(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), boardX, boardY, boardSize);
        hud.setTimers(clock.getWhiteMillis(), clock.getBlackMillis());
        hud.setNavActions(this::stepHistoryBack, this::stepHistoryForward);
        promotionDialog.layout(boardX, boardY, boardSize);
        hudStage.addActor(hud);
        hudStage.addActor(settingsDialog);
        hudStage.addActor(promotionDialog);
        gameOverOverlay = new GameOverOverlay();
        gameOverOverlay.layout();
        gameOverOverlay.setActions(this::playAgain, this::backToMenu);
        hudStage.addActor(gameOverOverlay);
        if (game.getGameMode() == MainGame.GameMode.P_V_AI) {
            bot = AIFactory.create(game.getDifficulty());
            settingsDialog.setSaveHandler(this::saveGame);
            settingsDialog.showSaveButton(true);
        }

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(hudStage);
        multiplexer.addProcessor(inputHandler);
        Gdx.input.setInputProcessor(multiplexer);
    }

    /** Restores a saved game before any renderer or input sees the board state. */
    private void applyLoadedState() {
        if (loadedState == null) {
            return;
        }
        try {
            game.setGameMode(MainGame.GameMode.valueOf(loadedState.getMode()));
            game.setDifficulty(Difficulty.valueOf(loadedState.getDifficulty()));
            game.setSelectedMap(BoardTheme.valueOf(loadedState.getMapName()));
        } catch (IllegalArgumentException e) {
            loadedState = null;
            return;
        }
        game.setTimeControlMinutes(loadedState.getTimeControlMinutes());
        board.restoreState(loadedState.getSnapshots(), loadedState.getHistoryIndex());
    }

    /** Panels are 92 px tall with 6 px gaps; the board fills the space between them. */
    private void calculateLayout() {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        boardSize = Math.min(width, height - 2 * ChessHud.PANEL_H - 2 * ChessHud.GAP);
        boardX = (width - boardSize) / 2f;
        boardY = (height - boardSize) / 2f;
    }

    @Override
    public void render(float delta) {
        if (Gdx.graphics.getWidth() <= 0 || Gdx.graphics.getHeight() <= 0) {
            return;
        }

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        animation.update(delta);
        boolean inputBlocked = (settingsDialog != null && settingsDialog.isOpen())
            || (promotionDialog != null && promotionDialog.isOpen())
            || aiThinking || aiMovePending || board.isViewingHistory();
        if (renderer != null) {
            inputHandler.setInputBlocked(inputBlocked);
            renderer.render(board, animation, boardX, boardY, boardSize,
                game.isVisualAidsEnabled(),
                inputHandler.getSelectedRow(), inputHandler.getSelectedColumn(),
                inputHandler.getHoverRow(), inputHandler.getHoverColumn(), delta);
        }
        updateAiDriver(delta);
        if (isLive() && !board.isGameOver()) {
            clock.update(delta, board.isWhiteTurn());
            if (board.isWhiteTurn() ? clock.isWhiteFlagged() : clock.isBlackFlagged()) {
                board.endByTimeout(board.isWhiteTurn());
            }
        }
        if (hud != null) {
            hud.tick(delta);
            hud.refreshState();
            hud.setTimers(clock.getWhiteMillis(), clock.getBlackMillis());
            boolean navAllowed = !aiThinking && !aiMovePending && !animation.isAnimating()
                && !(settingsDialog != null && settingsDialog.isOpen())
                && !(promotionDialog != null && promotionDialog.isOpen());
            hud.setNavEnabled(navAllowed && board.canStepBack(),
                navAllowed && board.canStepForward());
        }
        if (gameOverOverlay != null) {
            boolean over = board.isGameOver();
            if (over && isLive() && !gameOverAnnounced) {
                gameOverAnnounced = true;
                announceGameOver();
            }
            if (!over) {
                gameOverAnnounced = false;
            }
            gameOverOverlay.setVisible(over && isLive());
        }
        if (hudStage != null) {
            hudStage.act(delta);
            hudStage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        calculateLayout();
        if (renderer != null) {
            renderer.resize(width, height);
        }
        if (hud != null) {
            hud.layout(width, height, boardX, boardY, boardSize);
        }
        if (promotionDialog != null) {
            promotionDialog.layout(boardX, boardY, boardSize);
        }
        if (gameOverOverlay != null) {
            gameOverOverlay.layout();
        }
        if (hudStage != null) {
            hudStage.getViewport().update(width, height, true);
        }
        if (settingsDialog != null) {
            settingsDialog.layout();
        }
    }

    /** The clock and AI run only on the live (newest) position outside AI turns. */
    private boolean isLive() {
        return !board.isViewingHistory() && !aiThinking && !aiMovePending;
    }

    /** View-only review: rewinds one position; input and clock stay paused. */
    private void stepHistoryBack() {
        if (aiThinking || aiMovePending || animation.isAnimating() || !board.canStepBack()) {
            return;
        }
        inputHandler.clearSelection();
        board.stepBack();
    }

    /** View-only review: advances one position until back to the live game. */
    private void stepHistoryForward() {
        if (aiThinking || aiMovePending || animation.isAnimating() || !board.canStepForward()) {
            return;
        }
        inputHandler.clearSelection();
        board.stepForward();
    }

    /** Opens the game-over overlay once per finished game with its art and outcome sound. */
    private void announceGameOver() {
        String status = board.getStatusText();
        GameOverOverlay.Result result;
        if (status != null && status.endsWith("White wins")) {
            result = GameOverOverlay.Result.WIN;
        } else if (status != null && status.endsWith("Black wins")) {
            result = GameOverOverlay.Result.LOSE;
        } else {
            result = GameOverOverlay.Result.DRAW;
        }
        boolean showArtText = false;
        String detail;
        if (result == GameOverOverlay.Result.DRAW) {
            detail = drawDetail(status);
        } else if (status.startsWith("Resignation")) {
            detail = "RESIGNED";
        } else if (status.startsWith("Timeout")) {
            detail = "TIME OUT";
        } else {
            detail = "";
            showArtText = true;
        }
        if (result == GameOverOverlay.Result.WIN) {
            SoundManager.getInstance().playCheckmate();
        } else if (result == GameOverOverlay.Result.DRAW) {
            SoundManager.getInstance().playStalemate();
        } else {
            SoundManager.getInstance().playLose();
        }
        gameOverOverlay.open(result, detail, showArtText);
    }

    private static String drawDetail(String status) {
        if (status == null) {
            return "DRAW";
        }
        if (status.contains("Stalemate")) {
            return "STALEMATE";
        }
        if (status.contains("fifty")) {
            return "FIFTY-MOVE RULE";
        }
        if (status.contains("threefold")) {
            return "THREEFOLD REPETITION";
        }
        if (status.contains("insufficient")) {
            return "INSUFFICIENT MATERIAL";
        }
        return "DRAW";
    }

    /** PLAY AGAIN: fresh board and clock, same bot and settings. */
    private void playAgain() {
        board.reset();
        clock = new GameClock(game.getTimeControlMinutes() * 60000L);
        inputHandler.clearSelection();
        gameOverAnnounced = false;
        hud.setTimers(clock.getWhiteMillis(), clock.getBlackMillis());
    }

    /** Schedules and dispatches the AI reply whenever it is the AI's turn on the live board. */
    private void updateAiDriver(float delta) {
        if (bot == null) {
            return;
        }
        boolean aiTurn = !board.isWhiteTurn() && !board.isGameOver()
            && !animation.isAnimating() && !aiThinking && isLive();
        if (aiTurn && !aiMovePending) {
            aiMovePending = true;
            aiMoveDelay = 0.35f;
        }
        if (aiMovePending && !aiTurn) {
            aiMovePending = false;
            return;
        }
        if (aiMovePending) {
            aiMoveDelay -= delta;
            if (aiMoveDelay <= 0f) {
                aiMovePending = false;
                dispatchAiMove();
            }
        }
    }

    /** Runs the search on a worker thread; the result lands on the GL thread. */
    private void dispatchAiMove() {
        aiThinking = true;
        if (hud != null) {
            hud.setAiThinking(true);
        }
        final long budget = Math.min(ChessAI.DEFAULT_SOFT_BUDGET_MILLIS,
            Math.max(200, clock.getBlackMillis() - 200));
        Thread worker = new Thread(() -> {
            Move chosen = null;
            try {
                chosen = bot.computeMove(board, budget);
            } catch (RuntimeException e) {
                Gdx.app.error("ChessAI", "AI search failed", e);
            }
            final Move picked = chosen;
            Gdx.app.postRunnable(() -> applyAiMove(picked));
        }, "ai-worker");
        worker.setDaemon(true);
        worker.start();
    }

    /** Attempts the human move and plays its animation and sound feedback. */
    boolean applyHumanMove(int fromRow, int fromColumn, int toRow, int toColumn) {
        int piece = board.getPiece(fromRow, fromColumn);
        if (piece == Piece.EMPTY) {
            return false;
        }
        int target = board.getPiece(toRow, toColumn);
        boolean isCapture = target != Piece.EMPTY
            || (Piece.typeOf(piece) == Piece.PAWN && fromColumn != toColumn);
        boolean castling = board.isCastlingMove(fromRow, fromColumn, toRow, toColumn);
        boolean enPassant = board.isEnPassantMove(fromRow, fromColumn, toRow, toColumn);
        boolean whiteMove = board.isWhiteTurn();
        if (!board.move(fromRow, fromColumn, toRow, toColumn)) {
            return false;
        }
        startMoveFeedback(piece, fromRow, fromColumn, toRow, toColumn,
            isCapture, castling, enPassant, whiteMove);
        return true;
    }

    /** Opens the promotion picker; the pawn stays put until a piece is chosen. */
    public void promptPromotion(int fromRow, int fromColumn, int toRow, int toColumn) {
        promotionDialog.open(toColumn,
            type -> applyPromotion(fromRow, fromColumn, toRow, toColumn, type),
            inputHandler::clearSelection);
    }

    /** Commits the promotion with its lightning flash once the player picks a piece. */
    private void applyPromotion(int fromRow, int fromColumn, int toRow, int toColumn, int promotionType) {
        inputHandler.clearSelection();
        int piece = board.getPiece(fromRow, fromColumn);
        boolean isCapture = board.getPiece(toRow, toColumn) != Piece.EMPTY;
        boolean whiteMove = board.isWhiteTurn();
        if (!board.move(fromRow, fromColumn, toRow, toColumn, promotionType)) {
            return;
        }
        startMoveFeedback(piece, fromRow, fromColumn, toRow, toColumn,
            isCapture, false, false, whiteMove);
    }

    /**
     * Shared post-move feedback: move/rook/ghost animations, the promotion
     * lightning flash, and the SFX chain (check beats capture beats move).
     */
    private void startMoveFeedback(int piece, int fromRow, int fromColumn, int toRow, int toColumn,
                                   boolean isCapture, boolean castling, boolean enPassant,
                                   boolean whiteMove) {
        animation.start(board.getPiece(toRow, toColumn), fromRow, fromColumn, toRow, toColumn);
        if (castling) {
            int rookFromColumn = toColumn == 6 ? 7 : 0;
            int rookToColumn = toColumn == 6 ? 5 : 3;
            animation.startSecondary(board.getPiece(toRow, rookToColumn),
                toRow, rookFromColumn, toRow, rookToColumn);
        } else if (enPassant) {
            animation.startRemoval(Piece.forColor(Piece.PAWN, !whiteMove),
                toRow + (whiteMove ? -1 : 1), toColumn);
        }

        boolean promotion = Piece.typeOf(piece) == Piece.PAWN && (toRow == 0 || toRow == 7);
        String status = board.getStatusText();
        if (board.isGameOver()) {
            // Outcome sounds belong to the game-over overlay; keep the flash cosmetic.
            if (promotion) {
                animation.startPromotionFlash();
            }
            return;
        }
        if (promotion) {
            animation.startPromotionFlash();
            SoundManager.getInstance().playPromotionFlash();
            if (status != null && status.contains("check")) {
                SoundManager.getInstance().playLose();
            }
        } else if (status != null && status.contains("check")) {
            SoundManager.getInstance().playLose();
        } else if (isCapture) {
            SoundManager.getInstance().playPieceCapture();
        } else {
            SoundManager.getInstance().playPieceMove();
        }
    }

    /** Applies the AI's chosen move through the same SFX path as human moves. */
    private void applyAiMove(Move move) {
        aiThinking = false;
        if (hud != null) {
            hud.setAiThinking(false);
        }
        if (move == null || board.isGameOver() || !isLive()) {
            return;
        }
        int movedPiece = board.getPiece(move.fromRow, move.fromColumn);
        int target = board.getPiece(move.toRow, move.toColumn);
        boolean isCapture = target != Piece.EMPTY || move.isCapture();
        boolean castling = board.isCastlingMove(move.fromRow, move.fromColumn, move.toRow, move.toColumn);
        boolean enPassant = board.isEnPassantMove(move.fromRow, move.fromColumn, move.toRow, move.toColumn);
        boolean whiteMove = board.isWhiteTurn();
        if (!AIUtils.play(board, move)) {
            return;
        }
        startMoveFeedback(movedPiece, move.fromRow, move.fromColumn, move.toRow, move.toColumn,
            isCapture, castling, enPassant, whiteMove);
    }

    /** Persists the current session to the single save slot. */
    private void saveGame() {
        try {
            GameState state = new GameState();
            state.setMode(game.getGameMode().name());
            state.setDifficulty(game.getDifficulty().name());
            state.setMapName(game.getSelectedMap().name());
            state.setTimeControlMinutes(game.getTimeControlMinutes());
            state.setWhiteMillis(clock.getWhiteMillis());
            state.setBlackMillis(clock.getBlackMillis());
            state.setSnapshots(board.getHistory());
            state.setHistoryIndex(board.getHistoryIndex());
            SaveGame.write(state);
        } catch (Exception e) {
            Gdx.app.error("ChessGameScreen", "Saving failed", e);
        }
    }

    public float getBoardX() { return boardX; }
    public float getBoardY() { return boardY; }
    public float getBoardSize() { return boardSize; }

    boolean isAnimating() {
        return animation.isAnimating();
    }

    public float getGridX() { return renderer.getGridX(); }
    public float getGridY() { return renderer.getGridY(); }
    public float getSquareW() { return renderer.getSquareW(); }
    public float getSquareH() { return renderer.getSquareH(); }

    /** Restores the menu window size when windowed and returns to the main menu. */
    public void backToMenu() {
        if (!Gdx.graphics.isFullscreen()) {
            Gdx.graphics.setWindowedMode(MainGame.MENU_WINDOW_WIDTH, MainGame.MENU_WINDOW_HEIGHT);
        }
        game.setScreen(new FirstScreen(game));
    }

    /** ESC during play returns to the menu immediately. */
    void handleEscape() {
        backToMenu();
    }


    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
        SoundManager.getInstance().stopMusic();
    }

    @Override
    public void dispose() {
        SoundManager.getInstance().stopMusic();
        if (renderer != null) {
            renderer.dispose();
        }
        if (settingsDialog != null) {
            settingsDialog.dispose();
        }
        if (promotionDialog != null) {
            promotionDialog.dispose();
        }
        if (gameOverOverlay != null) {
            gameOverOverlay.dispose();
        }
        if (hud != null) {
            hud.dispose();
        }
        if (hudStage != null) {
            hudStage.dispose();
        }
    }
}
