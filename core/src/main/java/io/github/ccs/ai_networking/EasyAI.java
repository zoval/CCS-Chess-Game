package io.github.ccs.ai_networking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.ccs.game_logic.Board;

/**
 * Beginner bot: plays a uniformly random legal move, with a 60% bias toward
 * capturing moves when any capture is available.
 */
public final class EasyAI implements ChessAI {

    private static final double CAPTURE_BIAS = 0.60;

    private final Random random = new Random();

    /**
     * Picks a random move for the side to move without touching the board.
     *
     * @return the chosen move, or null if the side has no legal moves.
     */
    @Override
    public Move computeMove(Board board) {
        boolean white = board.isWhiteTurn();

        AIPosition position = AIUtils.read(board);
        List<Move> moves = AIUtils.legalMoves(position, white);

        if (moves.isEmpty()) {
            return null;
        }

        List<Move> captures = new ArrayList<Move>();

        for (Move move : moves) {
            if (move.isCapture()) {
                captures.add(move);
            }
        }

        if (!captures.isEmpty() && random.nextDouble() < CAPTURE_BIAS) {
            return AIUtils.randomMove(captures, random);
        }

        return AIUtils.randomMove(moves, random);
    }
}
