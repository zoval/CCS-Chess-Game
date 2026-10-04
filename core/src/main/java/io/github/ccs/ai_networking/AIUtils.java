package io.github.ccs.ai_networking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;

/**
 * Static helpers that let the AI bots read the real {@link Board}, generate
 * legal moves on a private {@link AIPosition}, evaluate positions, and play
 * the chosen move back onto the real board.
 *
 * <p>Castling and en passant are not generated here yet; the king's castling
 * candidates are still enumerated but rejected by {@code validMovement}.
 */
public final class AIUtils {

    private AIUtils() {
    }

    /**
     * Copies the real board into a private AI position.
     */
    public static AIPosition read(Board board) {
        AIPosition position = new AIPosition();

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                position.set(row, column, board.getPiece(row, column));
            }
        }

        return position;
    }

    /**
     * Sends the AI's selected move to the real board.
     *
     * @return true if the real board accepted the move.
     */
    public static boolean play(Board board, Move move) {
        if (move == null) {
            return false;
        }

        return board.move(move.fromRow, move.fromColumn, move.toRow, move.toColumn);
    }

    /**
     * Picks a uniformly random move from a non-empty list.
     */
    public static Move randomMove(List<Move> moves, Random random) {
        return moves.get(random.nextInt(moves.size()));
    }

    /**
     * Generates every legal move for the given side. A move is legal only if
     * the moving side's king is not attacked afterwards.
     *
     * @param white the side to move.
     * @return all legal moves; empty if the side has none.
     */
    public static List<Move> legalMoves(AIPosition position, boolean white) {
        List<Move> moves = new ArrayList<Move>();

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                int piece = position.get(row, column);

                if (piece == Piece.EMPTY || Piece.isWhite(piece) != white) {
                    continue;
                }

                switch (Piece.typeOf(piece)) {
                    case Piece.PAWN:
                        int direction = white ? 1 : -1;

                        addMove(position, moves, row, column, row + direction, column, white);
                        addMove(position, moves, row, column, row + direction * 2, column, white);
                        addMove(position, moves, row, column, row + direction, column - 1, white);
                        addMove(position, moves, row, column, row + direction, column + 1, white);
                        break;

                    case Piece.KNIGHT:
                        int[] rowMoves = {-2, -2, -1, -1, 1, 1, 2, 2};
                        int[] columnMoves = {-1, 1, -2, 2, -2, 2, -1, 1};

                        for (int i = 0; i < 8; i++) {
                            addMove(position, moves, row, column,
                                    row + rowMoves[i], column + columnMoves[i], white);
                        }
                        break;

                    case Piece.BISHOP:
                        addDirection(position, moves, row, column, 1, 1, white);
                        addDirection(position, moves, row, column, 1, -1, white);
                        addDirection(position, moves, row, column, -1, 1, white);
                        addDirection(position, moves, row, column, -1, -1, white);
                        break;

                    case Piece.ROOK:
                        addDirection(position, moves, row, column, 1, 0, white);
                        addDirection(position, moves, row, column, -1, 0, white);
                        addDirection(position, moves, row, column, 0, 1, white);
                        addDirection(position, moves, row, column, 0, -1, white);
                        break;

                    case Piece.QUEEN:
                        addDirection(position, moves, row, column, 1, 1, white);
                        addDirection(position, moves, row, column, 1, -1, white);
                        addDirection(position, moves, row, column, -1, 1, white);
                        addDirection(position, moves, row, column, -1, -1, white);
                        addDirection(position, moves, row, column, 1, 0, white);
                        addDirection(position, moves, row, column, -1, 0, white);
                        addDirection(position, moves, row, column, 0, 1, white);
                        addDirection(position, moves, row, column, 0, -1, white);
                        break;

                    case Piece.KING:
                        for (int rowMove = -1; rowMove <= 1; rowMove++) {
                            for (int columnMove = -1; columnMove <= 1; columnMove++) {
                                if (rowMove == 0 && columnMove == 0) {
                                    continue;
                                }

                                addMove(position, moves, row, column,
                                        row + rowMove, column + columnMove, white);
                            }
                        }

                        // Castling candidates; rejected by validMovement for now.
                        addMove(position, moves, row, column, row, column + 2, white);
                        addMove(position, moves, row, column, row, column - 2, white);
                        break;

                    default:
                        break;
                }
            }
        }

        return moves;
    }

    /**
     * Validates a single candidate move and appends it if it is legal. The
     * move must stay on the board, may not capture a friendly piece or the
     * enemy king, must satisfy normal movement rules, and must not leave the
     * moving side's king attacked.
     */
    private static void addMove(AIPosition position, List<Move> moves,
            int fromRow, int fromColumn, int toRow, int toColumn, boolean white) {

        if (!inside(toRow, toColumn)) {
            return;
        }

        int target = position.get(toRow, toColumn);

        // Cannot capture own piece.
        if (target != Piece.EMPTY && Piece.isWhite(target) == white) {
            return;
        }

        // Cannot capture the king.
        if (Piece.typeOf(target) == Piece.KING) {
            return;
        }

        if (!validMovement(position, fromRow, fromColumn, toRow, toColumn)) {
            return;
        }

        Move move = new Move(fromRow, fromColumn, toRow, toColumn, target != Piece.EMPTY);

        // Reject moves that leave the king in check.
        AIPosition next = new AIPosition(position);
        next.apply(move);

        if (!isKingAttacked(next, white)) {
            moves.add(move);
        }
    }

    /**
     * Adds the moves of a sliding piece (bishop, rook, queen) along one
     * direction, stopping after the first occupied square.
     */
    private static void addDirection(AIPosition position, List<Move> moves,
            int fromRow, int fromColumn, int rowStep, int columnStep, boolean white) {

        int row = fromRow + rowStep;
        int column = fromColumn + columnStep;

        while (inside(row, column)) {
            addMove(position, moves, fromRow, fromColumn, row, column, white);

            if (position.get(row, column) != Piece.EMPTY) {
                break;
            }

            row += rowStep;
            column += columnStep;
        }
    }

    /**
     * Checks the normal movement rules of a piece (ignoring legality of the
     * resulting position).
     */
    private static boolean validMovement(AIPosition position,
            int fromRow, int fromColumn, int toRow, int toColumn) {

        int piece = position.get(fromRow, fromColumn);
        int target = position.get(toRow, toColumn);

        int rowDifference = toRow - fromRow;
        int columnDifference = toColumn - fromColumn;

        switch (Piece.typeOf(piece)) {
            case Piece.PAWN:
                int direction = Piece.isWhite(piece) ? 1 : -1;
                int startRow = Piece.isWhite(piece) ? 1 : 6;

                // One square forward.
                if (columnDifference == 0 && target == Piece.EMPTY
                        && rowDifference == direction) {
                    return true;
                }

                // Two squares forward from the starting rank.
                if (columnDifference == 0 && fromRow == startRow
                        && rowDifference == direction * 2
                        && target == Piece.EMPTY
                        && position.get(fromRow + direction, fromColumn) == Piece.EMPTY) {
                    return true;
                }

                // Diagonal capture.
                return Math.abs(columnDifference) == 1
                        && rowDifference == direction
                        && target != Piece.EMPTY;

            case Piece.KNIGHT:
                return Math.abs(rowDifference) * Math.abs(columnDifference) == 2;

            case Piece.BISHOP:
                return Math.abs(rowDifference) == Math.abs(columnDifference)
                        && clearPath(position, fromRow, fromColumn, toRow, toColumn);

            case Piece.ROOK:
                return (rowDifference == 0 || columnDifference == 0)
                        && clearPath(position, fromRow, fromColumn, toRow, toColumn);

            case Piece.QUEEN:
                return (rowDifference == 0 || columnDifference == 0
                        || Math.abs(rowDifference) == Math.abs(columnDifference))
                        && clearPath(position, fromRow, fromColumn, toRow, toColumn);

            case Piece.KING:
                return Math.max(Math.abs(rowDifference), Math.abs(columnDifference)) == 1;

            default:
                return false;
        }
    }

    /**
     * Checks whether every square strictly between two squares is empty.
     */
    private static boolean clearPath(AIPosition position,
            int fromRow, int fromColumn, int toRow, int toColumn) {

        int rowStep = Integer.compare(toRow, fromRow);
        int columnStep = Integer.compare(toColumn, fromColumn);

        int row = fromRow + rowStep;
        int column = fromColumn + columnStep;

        while (row != toRow || column != toColumn) {
            if (position.get(row, column) != Piece.EMPTY) {
                return false;
            }

            row += rowStep;
            column += columnStep;
        }

        return true;
    }

    /**
     * Checks whether a king is attacked by any enemy piece.
     *
     * @return true if the given side's king is attacked.
     */
    public static boolean isKingAttacked(AIPosition position, boolean whiteKing) {
        int king = Piece.forColor(Piece.KING, whiteKing);

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                if (position.get(row, column) == king) {
                    return isSquareAttacked(position, row, column, !whiteKing);
                }
            }
        }

        // No king found; treat as attacked so such positions are rejected.
        return true;
    }

    /**
     * Checks whether a single square is attacked by the given side.
     */
    private static boolean isSquareAttacked(AIPosition position,
            int targetRow, int targetColumn, boolean byWhite) {

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                int piece = position.get(row, column);

                if (piece == Piece.EMPTY || Piece.isWhite(piece) != byWhite) {
                    continue;
                }

                int rowDifference = targetRow - row;
                int columnDifference = targetColumn - column;

                switch (Piece.typeOf(piece)) {
                    case Piece.PAWN:
                        int direction = byWhite ? 1 : -1;

                        if (rowDifference == direction && Math.abs(columnDifference) == 1) {
                            return true;
                        }
                        break;

                    case Piece.KNIGHT:
                        if (Math.abs(rowDifference) * Math.abs(columnDifference) == 2) {
                            return true;
                        }
                        break;

                    case Piece.BISHOP:
                        if (Math.abs(rowDifference) == Math.abs(columnDifference)
                                && clearPath(position, row, column, targetRow, targetColumn)) {
                            return true;
                        }
                        break;

                    case Piece.ROOK:
                        if ((rowDifference == 0 || columnDifference == 0)
                                && clearPath(position, row, column, targetRow, targetColumn)) {
                            return true;
                        }
                        break;

                    case Piece.QUEEN:
                        if ((rowDifference == 0 || columnDifference == 0
                                || Math.abs(rowDifference) == Math.abs(columnDifference))
                                && clearPath(position, row, column, targetRow, targetColumn)) {
                            return true;
                        }
                        break;

                    case Piece.KING:
                        if (Math.max(Math.abs(rowDifference), Math.abs(columnDifference)) == 1) {
                            return true;
                        }
                        break;

                    default:
                        break;
                }
            }
        }

        return false;
    }

    /**
     * Evaluates a position from the AI's perspective: material balance (own
     * pieces minus enemy pieces) plus a small bonus when the enemy king is
     * attacked and a small penalty when the AI's own king is attacked.
     *
     * @param aiWhite the color the AI plays.
     * @return positive score favors the AI.
     */
    public static int evaluate(AIPosition position, boolean aiWhite) {
        int score = 0;

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                int piece = position.get(row, column);

                if (piece == Piece.EMPTY) {
                    continue;
                }

                int pieceScore = value(Piece.typeOf(piece));

                if (Piece.isWhite(piece) == aiWhite) {
                    score += pieceScore;
                } else {
                    score -= pieceScore;
                }
            }
        }

        // Small check bonus.
        if (isKingAttacked(position, aiWhite)) {
            score -= 25;
        }

        if (isKingAttacked(position, !aiWhite)) {
            score += 25;
        }

        return score;
    }

    /**
     * Standard centipawn-style piece values used by {@link #evaluate}.
     */
    public static int value(int type) {
        switch (type) {
            case Piece.PAWN:
                return 100;

            case Piece.KNIGHT:
                return 320;

            case Piece.BISHOP:
                return 330;

            case Piece.ROOK:
                return 500;

            case Piece.QUEEN:
                return 900;

            case Piece.KING:
                return 20000;

            default:
                return 0;
        }
    }

    /**
     * Orders moves so captures come first. Searching captures first lets the
     * alpha-beta search in {@link HardAI} prune more branches. The relative
     * order within each group is preserved (stable partition).
     */
    public static List<Move> orderedMoves(AIPosition position, List<Move> moves) {
        List<Move> result = new ArrayList<Move>(moves.size());

        for (Move move : moves) {
            if (move.isCapture()) {
                result.add(move);
            }
        }

        for (Move move : moves) {
            if (!move.isCapture()) {
                result.add(move);
            }
        }

        return result;
    }

    /**
     * @return true if the coordinates are on the board.
     */
    private static boolean inside(int row, int column) {
        return row >= 0 && row < 8 && column >= 0 && column < 8;
    }
}
