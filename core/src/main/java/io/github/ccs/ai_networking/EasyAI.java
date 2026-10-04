package io.github.ccs.ai_networking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.ccs.game_logic.Board;

/**
 * Beginner bot: plays a uniformly random legal move, with a 60% bias toward
 * capturing moves when any capture is available.
 */
public final class EasyAI {

    private static final double CAPTURE_BIAS = 0.60;

    private final Random random = new Random();

    /**
     * Chooses a random move for the side to move and plays it on the board.
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

        List<Move> captures = new ArrayList<Move>();

        for (Move move : moves) {
            if (move.isCapture()) {
                captures.add(move);
            }
        }

        Move chosen;

        if (!captures.isEmpty() && random.nextDouble() < CAPTURE_BIAS) {
            chosen = AIUtils.randomMove(captures, random);
        } else {
            chosen = AIUtils.randomMove(moves, random);
        }

        return AIUtils.play(board, chosen);
    }
}
