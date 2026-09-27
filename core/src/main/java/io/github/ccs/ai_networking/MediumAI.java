package io.github.ccs.ai_networking;

import java.util.List;

import io.github.ccs.game_logic.Board;

public final class MediumAI {

    private static final int DEPTH = 2;
    private static final int MATE = 1000000;

    public boolean makeMove(Board board) {

        boolean white =
                board.isWhiteTurn();

        AIPosition position =
                AIUtils.read(board);

        List<Move> moves =
                AIUtils.legalMoves(
                        position,
                        white
                );

        if (moves.isEmpty()) {
            return false;
        }

        Move bestMove = null;
        int bestScore = Integer.MIN_VALUE;

        List<Move> ordered =
                AIUtils.orderedMoves(
                        position,
                        moves
                );

        for (Move move : ordered) {

            AIPosition next =
                    new AIPosition(position);

            next.apply(move);

            int score =
                    minimax(
                            next,
                            !white,
                            DEPTH - 1,
                            white
                    );

            if (bestMove == null
                    || score > bestScore) {

                bestMove = move;
                bestScore = score;
            }
        }

        return AIUtils.play(
                board,
                bestMove
        );
    }

    private int minimax(
            AIPosition position,
            boolean currentTurn,
            int depth,
            boolean aiWhite) {

        if (depth == 0) {

            return AIUtils.evaluate(
                    position,
                    aiWhite
            );
        }

        List<Move> moves =
                AIUtils.legalMoves(
                        position,
                        currentTurn
                );

        if (moves.isEmpty()) {

            if (AIUtils.isKingAttacked(
                    position,
                    currentTurn)) {

                return currentTurn == aiWhite
                        ? -MATE - depth
                        : MATE + depth;
            }

            return 0;
        }

        List<Move> ordered =
                AIUtils.orderedMoves(
                        position,
                        moves
                );

        if (currentTurn == aiWhite) {

            int best =
                    Integer.MIN_VALUE;

            for (Move move : ordered) {

                AIPosition next =
                        new AIPosition(position);

                next.apply(move);

                int score =
                        minimax(
                                next,
                                !currentTurn,
                                depth - 1,
                                aiWhite
                        );

                if (score > best) {
                    best = score;
                }
            }

            return best;
        }

        int best =
                Integer.MAX_VALUE;

        for (Move move : ordered) {

            AIPosition next =
                    new AIPosition(position);

            next.apply(move);

            int score =
                    minimax(
                            next,
                            !currentTurn,
                            depth - 1,
                            aiWhite
                    );

            if (score < best) {
                best = score;
            }
        }

        return best;
    }
}