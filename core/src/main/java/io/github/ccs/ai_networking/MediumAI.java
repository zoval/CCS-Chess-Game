package io.github.ccs.ai_networking;

import java.util.List;

import io.github.ccs.game_logic.Board;

/**
 * Intermediate bot: fixed-depth-2 minimax over material evaluation. It tries
 * every legal move, assumes the opponent answers with the best reply, and
 * plays the move with the best guaranteed score.
 */
public final class MediumAI {

    private static final int DEPTH = 2;
    private static final int MATE = 1000000;

    /**
     * Searches all legal moves at depth 2 and plays the best one.
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

            int score = minimax(next, !white, DEPTH - 1, white);

            if (bestMove == null || score > bestScore) {
                bestMove = move;
                bestScore = score;
            }
        }

        return AIUtils.play(board, bestMove);
    }

    /**
     * Classic minimax. Scores are always from the AI's point of view; the AI
     * maximizes on its own turns and minimizes on the opponent's turns.
     * Checkmate is scored as a large constant (larger when it happens sooner),
     * stalemate as 0.
     */
    private int minimax(AIPosition position, boolean currentTurn, int depth, boolean aiWhite) {
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
            int best = Integer.MIN_VALUE;

            for (Move move : ordered) {
                AIPosition next = new AIPosition(position);
                next.apply(move);

                int score = minimax(next, !currentTurn, depth - 1, aiWhite);

                if (score > best) {
                    best = score;
                }
            }

            return best;
        }

        int best = Integer.MAX_VALUE;

        for (Move move : ordered) {
            AIPosition next = new AIPosition(position);
            next.apply(move);

            int score = minimax(next, !currentTurn, depth - 1, aiWhite);

            if (score < best) {
                best = score;
            }
        }

        return best;
    }
}
