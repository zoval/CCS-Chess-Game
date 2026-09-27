package io.github.ccs.ai_networking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.ccs.game_logic.Board;

public final class EasyAI {

    private final Random random = new Random();

    public boolean makeMove(Board board) {

        boolean white = board.isWhiteTurn();

        AIPosition position = AIUtils.read(board);

        List<Move> moves =
                AIUtils.legalMoves(position, white);

        if (moves.isEmpty()) {
            return false;
        }

        List<Move> captures =
                new ArrayList<Move>();

        for (Move move : moves) {

            if (move.isCapture()) {
                captures.add(move);
            }
        }

        Move chosen;

        if (!captures.isEmpty()
                && random.nextDouble() < 0.60) {

            chosen =
                    AIUtils.randomMove(
                            captures,
                            random
                    );

        } else {

            chosen =
                    AIUtils.randomMove(
                            moves,
                            random
                    );
        }

        return AIUtils.play(board, chosen);
    }
}