package io.github.ccs.game_logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Coordinates board state, move validation, turns, and game status. */
public final class Board {
    /** Piece type definitions mapped from {@link Piece}. */
    public static final int EMPTY = Piece.EMPTY;
    public static final int PAWN = Piece.PAWN;
    public static final int KNIGHT = Piece.KNIGHT;
    public static final int BISHOP = Piece.BISHOP;
    public static final int ROOK = Piece.ROOK;
    public static final int QUEEN = Piece.QUEEN;
    public static final int KING = Piece.KING;

    /** Internal game state logic managers. */
    private final Position position;
    private final MoveValidator moveValidator = new MoveValidator();
    private final SpecialMoves specialMoves = moveValidator.getSpecialMoves();
    private final FiftymoveRule fiftyMoveRule = new FiftymoveRule();
    private final GameStatus gameStatus = new GameStatus();
    private final MoveHistory history = new MoveHistory();
    private final RepetitionRule repetitionRule = new RepetitionRule();
    private final List<Integer> capturedByWhite = new ArrayList<Integer>();
    private final List<Integer> capturedByBlack = new ArrayList<Integer>();
    private boolean whiteTurn = true;

    /** Creates a board with the standard starting position. */
    public Board() {
        this(new Position());
    }

    /** Creates a board from an existing position (used by tests and save/load). */
    Board(Position position) {
        this.position = position;
        repetitionRule.record(position, whiteTurn, specialMoves);
        history.reset(captureSnapshot());
    }

    /** @return piece ID at the specified coordinates. */
    public int getPiece(int row, int column) {
        return position.getPiece(row, column);
    }

    /** @return true if it is currently White's turn, false for Black. */
    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    /** @return true if checkmate or stalemate has been reached. */
    public boolean isGameOver() {
        return gameStatus.isGameOver();
    }

    /** @return A human-readable string indicating the game's current status (e.g., Turn info, Check, Mate). */
    public String getStatusText() {
        return gameStatus.getText();
    }

    /** Attempts a move for the current player, auto-promoting to a queen. */
    public boolean move(int fromRow, int fromColumn, int toRow, int toColumn) {
        return move(fromRow, fromColumn, toRow, toColumn, Piece.QUEEN);
    }

    /**
     * Attempts a move for the current player and advances the turn on
     * success. Pawns reaching the last rank promote to {@code promotionType}
     * (one of {@link Piece#QUEEN}, {@link Piece#ROOK}, {@link Piece#BISHOP},
     * {@link Piece#KNIGHT}).
     */
    public boolean move(int fromRow, int fromColumn, int toRow, int toColumn, int promotionType) {
        if (gameStatus.isGameOver()) return false;
        if (!moveValidator.isLegalMove(position, fromRow, fromColumn, toRow, toColumn, whiteTurn)) {
            return false;
        }

        int piece = position.getPiece(fromRow, fromColumn);
        int target = position.getPiece(toRow, toColumn);

        boolean castling = specialMoves.isCastlingMove(position, fromRow, fromColumn,
            toRow, toColumn, whiteTurn, moveValidator);

        boolean enPassant = specialMoves.isEnPassantMove(position, fromRow, fromColumn,
            toRow, toColumn, whiteTurn);

        int enPassantPiece = Piece.EMPTY;
        if (enPassant) {
            enPassantPiece = position.getPiece(toRow + (whiteTurn ? -1 : 1), toColumn);
        }

        // Validate the promotion choice up front so an invalid type fails
        // before any state changes.
        if (SpecialMoves.isPromotion(piece, toRow)) {
            SpecialMoves.promotedPiece(piece, promotionType);
        }

        position.setPiece(fromRow, fromColumn, Piece.EMPTY);
        position.setPiece(toRow, toColumn, piece);

        if (enPassant) {
            position.setPiece(toRow + (whiteTurn ? -1 : 1), toColumn, Piece.EMPTY);
        }

        if (castling) {
            int rookFromColumn = toColumn == 6 ? 7 : 0;
            int rookToColumn = toColumn == 6 ? 5 : 3;
            position.setPiece(toRow, rookToColumn, position.getPiece(toRow, rookFromColumn));
            position.setPiece(toRow, rookFromColumn, Piece.EMPTY);
        }
        promotePawn(toRow, toColumn, piece, promotionType);

        if (target != Piece.EMPTY) {
            (whiteTurn ? capturedByWhite : capturedByBlack).add(target);
        }
        if (enPassant) {
            (whiteTurn ? capturedByWhite : capturedByBlack).add(enPassantPiece);
        }

        boolean pawnMoved = Piece.typeOf(piece) == Piece.PAWN;
        boolean captureMade = target != Piece.EMPTY || enPassant;
        fiftyMoveRule.recordMove(pawnMoved, captureMade);
        specialMoves.recordMove(fromRow, fromColumn, toRow, toColumn, piece);
        whiteTurn = !whiteTurn;
        repetitionRule.truncate(history.getCurrentIndex() + 1);
        repetitionRule.record(position, whiteTurn, specialMoves);
        gameStatus.update(position, moveValidator, whiteTurn, fiftyMoveRule.isDraw(),
                repetitionRule.isThreefoldRepetition(), InsufficientMaterial.isInsufficient(position));
        history.push(captureSnapshot());
        return true;
    }

    public void reset() {
        position.reset();
        specialMoves.reset();
        fiftyMoveRule.reset();
        whiteTurn = true;
        gameStatus.reset();
        capturedByWhite.clear();
        capturedByBlack.clear();
        repetitionRule.reset();
        repetitionRule.record(position, whiteTurn, specialMoves);
        history.reset(captureSnapshot());
    }

    /**
     * Checks whether moving the piece at the source square to the destination square is legal
     * for the side to move, without applying the move.
     */
    public boolean isLegalMove(int fromRow, int fromColumn, int toRow, int toColumn) {
        return moveValidator.isLegalMove(position, fromRow, fromColumn, toRow, toColumn, whiteTurn);
    }

    /**
     * @return true if the side to move's pawn would promote by moving from
     * the source square to the destination square (pre-move popup hook).
     */
    public boolean isPromotionMove(int fromRow, int fromColumn, int toRow, int toColumn) {
        if (gameStatus.isGameOver()) return false;

        int piece = position.getPiece(fromRow, fromColumn);
        if (Piece.typeOf(piece) != Piece.PAWN || Piece.isWhite(piece) != whiteTurn) {
            return false;
        }

        if (toRow != (Piece.isWhite(piece) ? 7 : 0)) {
            return false;
        }

        return moveValidator.isLegalMove(position, fromRow, fromColumn, toRow, toColumn, whiteTurn);
    }

    /**
     * @return true if the side to move's king would castle by moving from the
     * source square to the destination square (pre-move animation hook).
     */
    public boolean isCastlingMove(int fromRow, int fromColumn, int toRow, int toColumn) {
        return specialMoves.isCastlingMove(position, fromRow, fromColumn, toRow, toColumn,
                whiteTurn, moveValidator);
    }

    /**
     * @return true if the side to move's pawn would capture en passant by
     * moving from the source square to the destination square (pre-move
     * animation hook).
     */
    public boolean isEnPassantMove(int fromRow, int fromColumn, int toRow, int toColumn) {
        return specialMoves.isEnPassantMove(position, fromRow, fromColumn, toRow, toColumn, whiteTurn);
    }

    /** @return all legal destination squares for the piece at the given square (empty if none). */
    public List<int[]> getLegalMoves(int fromRow, int fromColumn) {
        List<int[]> moves = new ArrayList<>();
        if (position.getPiece(fromRow, fromColumn) == Piece.EMPTY) {
            return moves;
        }
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                if (isLegalMove(fromRow, fromColumn, row, column)) {
                    moves.add(new int[]{row, column});
                }
            }
        }
        return moves;
    }

    /** @return true if the king of the given color is currently attacked. */
    public boolean isKingInCheck(boolean white) {
        return moveValidator.isKingAttacked(position, white);
    }

    /** @return true if the side to move's king is currently attacked. */
    public boolean isCurrentKingInCheck() {
        return moveValidator.isKingAttacked(position, whiteTurn);
    }

    /** @return the {@code [row, column]} of the given color's king, or null if not found. */
    public int[] getKingPosition(boolean white) {
        int king = Piece.forColor(Piece.KING, white);
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                if (position.getPiece(row, column) == king) {
                    return new int[]{row, column};
                }
            }
        }
        return null;
    }

    /**
     * @return the {@code [fromRow, fromColumn, toRow, toColumn]} of the last
     * committed move, or null if no move has been made yet.
     */
    public int[] getLastMove() {
        if (specialMoves.getLastFromRow() == -1) {
            return null;
        }
        return new int[]{specialMoves.getLastFromRow(), specialMoves.getLastFromColumn(),
                specialMoves.getLastToRow(), specialMoves.getLastToColumn()};
    }

    /** @return signed piece IDs of everything the given side has captured. */
    public List<Integer> getCapturedPieces(boolean white) {
        return Collections.unmodifiableList(white ? capturedByWhite : capturedByBlack);
    }

    /**
     * @return the material balance in pawn units (P1 N3 B3 R5 Q9), positive
     * when White has more material.
     */
    public int getMaterialBalance() {
        int[] values = {0, 1, 3, 3, 5, 9, 0};
        int balance = 0;

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                int piece = position.getPiece(row, column);
                if (piece == Piece.EMPTY) {
                    continue;
                }

                int value = values[Piece.typeOf(piece)];
                balance += Piece.isWhite(piece) ? value : -value;
            }
        }

        return balance;
    }

    /**
     * The side to move resigns.
     *
     * @return true if the resignation was recorded; false once the game is
     * already over.
     */
    public boolean resign() {
        if (gameStatus.isGameOver()) return false;
        gameStatus.declareResignation(whiteTurn);
        return true;
    }

    /**
     * Both players agree to a draw.
     *
     * @return true if the draw was recorded; false once the game is already
     * over.
     */
    public boolean agreeDraw() {
        if (gameStatus.isGameOver()) return false;
        gameStatus.declareAgreedDraw();
        return true;
    }

    /** Ends the game because the given side ran out of time. */
    public void endByTimeout(boolean whiteFlagged) {
        if (gameStatus.isGameOver()) return;
        gameStatus.declareTimeout(whiteFlagged);
    }

    /** @return true if there is an earlier position to review. */
    public boolean canStepBack() {
        return history.canStepBack();
    }

    /** @return true if there is a newer position to review. */
    public boolean canStepForward() {
        return history.canStepForward();
    }

    /** @return true if the current view is not the newest (live) state. */
    public boolean isViewingHistory() {
        return history.isViewingHistory();
    }

    /**
     * Steps back one position for view-only review.
     *
     * @return true if the view moved back.
     */
    public boolean stepBack() {
        MoveSnapshot snapshot = history.stepBack();
        if (snapshot == null) {
            return false;
        }
        applySnapshot(snapshot);
        return true;
    }

    /**
     * Steps forward one position during review.
     *
     * @return true if the view moved forward.
     */
    public boolean stepForward() {
        MoveSnapshot snapshot = history.stepForward();
        if (snapshot == null) {
            return false;
        }
        applySnapshot(snapshot);
        return true;
    }

    /** @return an unmodifiable view of the full snapshot history. */
    public List<MoveSnapshot> getHistory() {
        return history.snapshots();
    }

    /** @return the current history view index. */
    public int getHistoryIndex() {
        return history.getCurrentIndex();
    }

    /**
     * Replaces the whole game state with the given snapshot history (used
     * when loading a saved game). The view index is restored and the
     * repetition keys are rebuilt up to that index.
     */
    public void restoreState(List<MoveSnapshot> snapshots, int index) {
        if (snapshots.isEmpty()) {
            return;
        }

        history.replaceAll(snapshots, index);
        applySnapshot(history.current());

        repetitionRule.reset();
        Position replayed = new Position();
        for (int i = 0; i <= history.getCurrentIndex(); i++) {
            MoveSnapshot snapshot = history.at(i);
            for (int row = 0; row < 8; row++) {
                for (int column = 0; column < 8; column++) {
                    replayed.setPiece(row, column, snapshot.getPiece(row, column));
                }
            }
            repetitionRule.record(replayed, snapshot.isWhiteTurn(), snapshot.getSpecialMoves());
        }
    }

    private void applySnapshot(MoveSnapshot snapshot) {
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                position.setPiece(row, column, snapshot.getPiece(row, column));
            }
        }
        whiteTurn = snapshot.isWhiteTurn();
        gameStatus.restore(snapshot.getStatusText(), snapshot.isGameOver());
        fiftyMoveRule.setHalfmoveClock(snapshot.getHalfmoveClock());
        specialMoves.restore(snapshot.getSpecialMoves());
        capturedByWhite.clear();
        capturedByWhite.addAll(snapshot.getCapturedByWhite());
        capturedByBlack.clear();
        capturedByBlack.addAll(snapshot.getCapturedByBlack());
    }

    private MoveSnapshot captureSnapshot() {
        return new MoveSnapshot(positionPieces(), whiteTurn, gameStatus.getText(),
                gameStatus.isGameOver(), fiftyMoveRule.getHalfmoveClock(), specialMoves,
                capturedByWhite, capturedByBlack);
    }

    private int[][] positionPieces() {
        int[][] pieces = new int[8][8];
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                pieces[row][column] = position.getPiece(row, column);
            }
        }
        return pieces;
    }

    private void promotePawn(int row, int column, int piece, int promotionType) {
        if (SpecialMoves.isPromotion(piece, row)) {
            position.setPiece(row, column, SpecialMoves.promotedPiece(piece, promotionType));
        }
    }
}
