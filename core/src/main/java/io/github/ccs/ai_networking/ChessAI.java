package io.github.ccs.ai_networking;

import io.github.ccs.game_logic.Board;

/**
 * Common contract for the bots. {@link #computeMove(Board)} is a pure
 * search: it only reads the board, so it is safe to call from a worker
 * thread while the GL thread keeps rendering. {@link #makeMove(Board)}
 * computes and applies the chosen move and must run on the thread that owns
 * the board.
 */
public interface ChessAI {

    /** Default soft time budget for a search, in milliseconds. */
    long DEFAULT_SOFT_BUDGET_MILLIS = 1500;

    /**
     * Searches for the best move for the side to move without touching the
     * board.
     *
     * @return the chosen move, or null if the side has no legal move.
     */
    Move computeMove(Board board);

    /**
     * Same as {@link #computeMove(Board)} with an explicit soft time budget
     * in milliseconds (implementations may still take longer to finish the
     * depth in progress).
     */
    default Move computeMove(Board board, long softBudgetMillis) {
        return computeMove(board);
    }

    /**
     * Computes the best move and plays it on the board.
     *
     * @return true if a move was played; false if the side has no legal moves.
     */
    default boolean makeMove(Board board) {
        Move move = computeMove(board);
        return move != null && AIUtils.play(board, move);
    }
}
