package io.github.ccs.ai_networking;

import java.util.List;

import io.github.ccs.game_logic.Board;

/**
 * Advanced bot: fixed-depth-2 alpha-beta minimax over material evaluation.
 * Same search as {@link MediumAI} but prunes branches that cannot influence
 * the final choice, and searches captures first for more cutoffs.
 */
public final class HardAI {

    private static final int DEPTH = 2;
    private static final int MATE = 1000000;

    /**
     * Searches all legal moves at depth 2 with alpha-beta pruning and plays
     * the best one.
     *
     * @return true if a move was played; false if the side has no legal moves.
     */
    public boolean makeMove(Board board) {
        boolean white = board.isWhiteTurn();

        AIPosition position = AIUtils.read(board);
        List<Move> moves = AIUtils.legalMoves(position, white);

        if (moves.isEmpty()) {
            return false;
        }

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        List<Move> ordered = AIUtils.orderedMoves(position, moves);

        for (Move move : ordered) {
            AIPosition next = new AIPosition(position);
            next.apply(move);

            int score = search(next, !white, DEPTH - 1,
                    Integer.MIN_VALUE + 1, Integer.MAX_VALUE - 1, white);

            if (bestMove == null || score > bestScore) {
                bestMove = move;
                bestScore = score;
            }
        }

        return AIUtils.play(board, bestMove);
    }

    /**
     * Alpha-beta minimax. Scores are always from the AI's point of view; the
     * AI maximizes on its own turns and minimizes on the opponent's turns.
     * Branches outside the {@code [alpha, beta]} window are pruned.
     * Checkmate is scored as a large constant (larger when it happens sooner),
     * stalemate as 0.
     */
    private int search(AIPosition position, boolean currentTurn, int depth,
            int alpha, int beta, boolean aiWhite) {

        if (depth == 0) {
            return AIUtils.evaluate(position, aiWhite);
        }

        List<Move> moves = AIUtils.legalMoves(position, currentTurn);

        if (moves.isEmpty()) {
            if (AIUtils.isKingAttacked(position, currentTurn)) {
                return currentTurn == aiWhite ? -MATE - depth : MATE + depth;
            }

            return 0;
        }

        List<Move> ordered = AIUtils.orderedMoves(position, moves);

        if (currentTurn == aiWhite) {
            int best = Integer.MIN_VALUE + 1;

            for (Move move : ordered) {
                AIPosition next = new AIPosition(position);
                next.apply(move);

                int score = search(next, !currentTurn, depth - 1, alpha, beta, aiWhite);

                if (score > best) {
                    best = score;
                }

                if (best > alpha) {
                    alpha = best;
                }

                if (beta <= alpha) {
                    break;
                }
            }

            return best;
        }

        int best = Integer.MAX_VALUE - 1;

        for (Move move : ordered) {
            AIPosition next = new AIPosition(position);
            next.apply(move);

            int score = search(next, !currentTurn, depth - 1, alpha, beta, aiWhite);

            if (score < best) {
                best = score;
            }

            if (best < beta) {
                beta = best;
            }

            if (beta <= alpha) {
                break;
            }
        }

        return best;
    }
}
