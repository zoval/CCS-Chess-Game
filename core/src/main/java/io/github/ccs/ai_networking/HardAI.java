package io.github.ccs.ai_networking;

import java.util.ArrayList;
import java.util.List;

import io.github.ccs.game_logic.Board;

/**
 * Advanced bot (~1200 Elo): iterative-deepening alpha-beta search (depth 1-4)
 * over material plus piece-square tables, with a capture-only quiescence
 * search, MVV-LVA move ordering, and ply-adjusted mate scores. Stops starting
 * new depth iterations once the soft budget is spent and aborts mid-search at
 * the hard deadline, always keeping the best move of the last completed depth.
 * No transposition table, null-move pruning, or killer moves.
 */
public final class HardAI implements ChessAI {

    private static final int MAX_DEPTH = 4;
    private static final int MATE = 1000000;
    private static final long HARD_BUDGET_MILLIS = 8000;
    private static final int QUIESCENCE_MAX_DEPTH = 6;
    private static final int NODE_CHECK_MASK = 1023;

    private long hardDeadline;
    private boolean aborted;
    private int nodes;

    @Override
    public Move computeMove(Board board) {
        return computeMove(board, DEFAULT_SOFT_BUDGET_MILLIS);
    }

    @Override
    public Move computeMove(Board board, long softBudgetMillis) {
        boolean white = board.isWhiteTurn();

        AIPosition position = AIUtils.read(board);
        List<Move> moves = AIUtils.legalMoves(position, white);

        if (moves.isEmpty()) {
            return null;
        }

        if (moves.size() == 1) {
            return moves.get(0);
        }

        List<Move> ordered = AIUtils.searchOrderedMoves(position, moves);
        Move bestMove = ordered.get(0);

        long start = System.currentTimeMillis();
        hardDeadline = start + HARD_BUDGET_MILLIS;
        aborted = false;
        nodes = 0;

        for (int depth = 1; depth <= MAX_DEPTH; depth++) {
            Move depthBest = null;
            int alpha = Integer.MIN_VALUE + 1;
            int beta = Integer.MAX_VALUE - 1;

            for (Move move : ordered) {
                AIPosition next = new AIPosition(position);
                next.apply(move);

                int score = search(next, !white, depth - 1, 1, alpha, beta, white);

                if (aborted) {
                    break;
                }

                if (depthBest == null || score > alpha) {
                    depthBest = move;
                    alpha = score;
                }
            }

            if (aborted) {
                break;
            }

            if (depthBest != null) {
                bestMove = depthBest;

                // Search the best move first in the next, deeper iteration.
                ordered.remove(depthBest);
                ordered.add(0, depthBest);
            }

            // A forced mate was found; deeper search cannot improve it.
            if (Math.abs(alpha) >= MATE - 64) {
                break;
            }

            if (System.currentTimeMillis() - start >= softBudgetMillis) {
                break;
            }
        }

        return bestMove;
    }

    /**
     * Alpha-beta minimax. Scores are always from the AI's point of view; the
     * AI maximizes on its own turns and minimizes on the opponent's turns.
     * Branches outside the {@code [alpha, beta]} window are pruned. Checkmate
     * is scored relative to the ply so faster mates are preferred; stalemate
     * scores 0.
     */
    private int search(AIPosition position, boolean currentTurn, int depth, int ply,
            int alpha, int beta, boolean aiWhite) {

        if (depth == 0) {
            return quiescence(position, currentTurn, ply, alpha, beta, aiWhite,
                    QUIESCENCE_MAX_DEPTH);
        }

        if ((++nodes & NODE_CHECK_MASK) == 0 && System.currentTimeMillis() >= hardDeadline) {
            aborted = true;
        }

        if (aborted) {
            return 0;
        }

        List<Move> moves = AIUtils.legalMoves(position, currentTurn);

        if (moves.isEmpty()) {
            if (AIUtils.isKingAttacked(position, currentTurn)) {
                int mateScore = MATE - ply;
                return currentTurn == aiWhite ? -mateScore : mateScore;
            }

            return 0;
        }

        List<Move> ordered = AIUtils.searchOrderedMoves(position, moves);

        if (currentTurn == aiWhite) {
            int best = Integer.MIN_VALUE + 1;

            for (Move move : ordered) {
                AIPosition next = new AIPosition(position);
                next.apply(move);

                int score = search(next, !currentTurn, depth - 1, ply + 1, alpha, beta, aiWhite);

                if (aborted) {
                    return 0;
                }

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

            int score = search(next, !currentTurn, depth - 1, ply + 1, alpha, beta, aiWhite);

            if (aborted) {
                return 0;
            }

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

    /**
     * Quiescence search: resolves the horizon by playing out tactical moves
     * (captures and promotions, MVV-LVA ordered) until quiet, with a stand-pat
     * cutoff so a side may keep its material instead of entering a losing
     * capture sequence.
     */
    private int quiescence(AIPosition position, boolean currentTurn, int ply,
            int alpha, int beta, boolean aiWhite, int qdepth) {

        int standPat = PieceSquareTables.evaluate(position, aiWhite);

        if (qdepth == 0) {
            return standPat;
        }

        if ((++nodes & NODE_CHECK_MASK) == 0 && System.currentTimeMillis() >= hardDeadline) {
            aborted = true;
        }

        if (aborted) {
            return 0;
        }

        List<Move> tactical = new ArrayList<Move>();

        for (Move move : AIUtils.legalMoves(position, currentTurn)) {
            if (move.isCapture() || AIUtils.isPromotion(position, move)) {
                tactical.add(move);
            }
        }

        if (tactical.isEmpty()) {
            return standPat;
        }

        List<Move> ordered = AIUtils.searchOrderedMoves(position, tactical);

        if (currentTurn == aiWhite) {
            int best = standPat;

            if (best > alpha) {
                alpha = best;
            }

            if (beta <= alpha) {
                return best;
            }

            for (Move move : ordered) {
                AIPosition next = new AIPosition(position);
                next.apply(move);

                int score = quiescence(next, !currentTurn, ply + 1, alpha, beta, aiWhite,
                        qdepth - 1);

                if (aborted) {
                    return 0;
                }

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

        int best = standPat;

        if (best < beta) {
            beta = best;
        }

        if (beta <= alpha) {
            return best;
        }

        for (Move move : ordered) {
            AIPosition next = new AIPosition(position);
            next.apply(move);

            int score = quiescence(next, !currentTurn, ply + 1, alpha, beta, aiWhite,
                    qdepth - 1);

            if (aborted) {
                return 0;
            }

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
