package io.github.ccs.ai_networking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import io.github.ccs.game_logic.Board;

/**
 * Intermediate bot (~600 Elo): fixed-depth-2 minimax over material evaluation,
 * weakened on purpose. It overlooks tactics 30% of the time by playing a
 * random legal move, otherwise it ranks its root moves with a little noise and
 * picks among its top three with weights 0.6/0.3/0.1. Blunders are suppressed
 * once a forced mate is found.
 */
public final class MediumAI implements ChessAI {

    private static final int DEPTH = 2;
    private static final int MATE = 1000000;
    private static final double BLUNDER_CHANCE = 0.30;
    private static final int NOISE_CENTIPAWNS = 20;
    private static final double[] TOP_WEIGHTS = {0.60, 0.30, 0.10};

    private final Random random = new Random();

    /**
     * Searches all legal moves at depth 2 and picks one through the blunder
     * model. Pure search: the board is only read.
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

        List<ScoredMove> scored = new ArrayList<ScoredMove>();

        for (Move move : AIUtils.orderedMoves(position, moves)) {
            AIPosition next = new AIPosition(position);
            next.apply(move);

            scored.add(new ScoredMove(move, minimax(next, !white, DEPTH - 1, white)));
        }

        ScoredMove best = Collections.max(scored, SCORE_ORDER);

        // Found a mate: stop pretending to be weak.
        if (best.score >= 900) {
            return best.move;
        }

        // Blunder: overlook the tactic entirely.
        if (random.nextDouble() < BLUNDER_CHANCE) {
            return AIUtils.randomMove(moves, random);
        }

        for (ScoredMove candidate : scored) {
            candidate.score += random.nextInt(NOISE_CENTIPAWNS * 2 + 1) - NOISE_CENTIPAWNS;
        }

        Collections.sort(scored, SCORE_ORDER);

        int options = Math.min(TOP_WEIGHTS.length, scored.size());
        double roll = random.nextDouble();
        int index = options - 1;

        for (int i = 0; i < options; i++) {
            if (roll < TOP_WEIGHTS[i]) {
                index = i;
                break;
            }
        }

        return scored.get(index).move;
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

    private static final Comparator<ScoredMove> SCORE_ORDER = new Comparator<ScoredMove>() {
        @Override
        public int compare(ScoredMove a, ScoredMove b) {
            return b.score - a.score;
        }
    };

    private static final class ScoredMove {
        final Move move;
        int score;

        ScoredMove(Move move, int score) {
            this.move = move;
            this.score = score;
        }
    }
}
