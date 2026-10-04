package io.github.ccs.game_logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Threefold-repetition rule. Every position reached in the game (including
 * the initial one) is folded into a Zobrist-style key covering piece
 * placement, side to move, remaining castling rights, and the en-passant
 * file when a two-square pawn push just happened.
 */
public final class RepetitionRule {

    private static final long FIXED_SEED = 0x5EEDC0DE5EEDC0DEL;

    private static final long[][] PIECE_SQUARE = new long[12][64];
    private static final long SIDE_TO_MOVE;
    private static final long[] CASTLING = new long[4];
    private static final long[] EN_PASSANT_FILE = new long[8];

    static {
        Random random = new Random(FIXED_SEED);
        for (int piece = 0; piece < 12; piece++) {
            for (int square = 0; square < 64; square++) {
                PIECE_SQUARE[piece][square] = random.nextLong();
            }
        }
        SIDE_TO_MOVE = random.nextLong();
        for (int right = 0; right < 4; right++) {
            CASTLING[right] = random.nextLong();
        }
        for (int file = 0; file < 8; file++) {
            EN_PASSANT_FILE[file] = random.nextLong();
        }
    }

    private final List<Long> keys = new ArrayList<Long>();

    /** Folds the given position into a key and records it. */
    public void record(Position position, boolean whiteTurn, SpecialMoves special) {
        keys.add(computeKey(position, whiteTurn, special));
    }

    /** @return true if the current position has occurred three times. */
    public boolean isThreefoldRepetition() {
        if (keys.isEmpty()) {
            return false;
        }

        long last = keys.get(keys.size() - 1);

        int count = 0;
        for (long key : keys) {
            if (key == last) {
                count++;
            }
        }

        return count >= 3;
    }

    /**
     * Drops recorded keys beyond the given size (used when stepping back in
     * history: pass the history index + 1).
     */
    public void truncate(int size) {
        while (keys.size() > size) {
            keys.remove(keys.size() - 1);
        }
    }

    /** Clears all recorded keys. */
    public void reset() {
        keys.clear();
    }

    /** @return the number of recorded position keys. */
    public int size() {
        return keys.size();
    }

    private long computeKey(Position position, boolean whiteTurn, SpecialMoves special) {
        long key = 0;

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                int piece = position.getPiece(row, column);
                if (piece == Piece.EMPTY) {
                    continue;
                }

                int index = (Piece.typeOf(piece) - 1) + (Piece.isWhite(piece) ? 0 : 6);
                key ^= PIECE_SQUARE[index][row * 8 + column];
            }
        }

        if (!whiteTurn) {
            key ^= SIDE_TO_MOVE;
        }

        if (hasCastlingRight(special, true, true)) {
            key ^= CASTLING[0];
        }
        if (hasCastlingRight(special, true, false)) {
            key ^= CASTLING[1];
        }
        if (hasCastlingRight(special, false, true)) {
            key ^= CASTLING[2];
        }
        if (hasCastlingRight(special, false, false)) {
            key ^= CASTLING[3];
        }

        // The en-passant file only distinguishes positions right after a
        // two-square pawn push, when the capture is actually possible.
        if (Piece.typeOf(special.getLastPiece()) == Piece.PAWN
                && Math.abs(special.getLastToRow() - special.getLastFromRow()) == 2) {
            key ^= EN_PASSANT_FILE[special.getLastToColumn()];
        }

        return key;
    }

    private boolean hasCastlingRight(SpecialMoves special, boolean white, boolean kingSide) {
        boolean kingMoved = white ? special.isWhiteKingMoved() : special.isBlackKingMoved();
        boolean rookMoved = white
                ? (kingSide ? special.isWhiteKingSideRookMoved() : special.isWhiteQueenSideRookMoved())
                : (kingSide ? special.isBlackKingSideRookMoved() : special.isBlackQueenSideRookMoved());
        return !kingMoved && !rookMoved;
    }
}
