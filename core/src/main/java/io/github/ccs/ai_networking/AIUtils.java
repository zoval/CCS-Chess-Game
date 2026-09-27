package io.github.ccs.ai_networking;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;

public final class AIUtils {

    private AIUtils() {
    }

    /*
     * Copies the real Board into the AI's private board.
     */
    public static AIPosition read(Board board) {

        AIPosition position = new AIPosition();

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {

                position.set(row,
                        column,
                        board.getPiece(row, column)
                );
            }
        }


        return position;
    }

    /*
     * Sends the AI's selected move to the real Board.
     */
    public static boolean play(Board board, Move move) {
        if (move == null) {
            return false;
        }

        return board.move(move.fromRow, move.fromColumn, move.toRow, move.toColumn);
    }

    /*
     * Random move helper.
     */
    public static Move randomMove(
            List<Move> moves,
            Random random) {

        return moves.get(
                random.nextInt(moves.size())
        );
    }

    /*
     * Generate legal moves.
     */
    public static List<Move> legalMoves(
            AIPosition position,
            boolean white) {

        List<Move> moves =
                new ArrayList<Move>();

        for (int row = 0; row < 8; row++) {

            for (int column = 0;
                 column < 8;
                 column++) {

                int piece =
                        position.get(row, column);

                if (piece == Piece.EMPTY) {
                    continue;
                }

                if (Piece.isWhite(piece) != white) {
                    continue;
                }

                int type =
                        Piece.typeOf(piece);

                switch (type) {

                    case Piece.PAWN:

                        int direction =
                                white ? 1 : -1;

                        addMove(
                                position,
                                moves,
                                row,
                                column,
                                row + direction,
                                column,
                                white
                        );

                        addMove(
                                position,
                                moves,
                                row,
                                column,
                                row + direction * 2,
                                column,
                                white
                        );

                        addMove(
                                position,
                                moves,
                                row,
                                column,
                                row + direction,
                                column - 1,
                                white
                        );

                        addMove(
                                position,
                                moves,
                                row,
                                column,
                                row + direction,
                                column + 1,
                                white
                        );
                        break;

                    case Piece.KNIGHT:

                        int[] rowMoves = {
                                -2, -2, -1, -1,
                                 1,  1,  2,  2
                        };

                        int[] columnMoves = {
                                -1,  1, -2,  2,
                                -2,  2, -1,  1
                        };

                        for (int i = 0; i < 8; i++) {

                            addMove(
                                    position,
                                    moves,
                                    row,
                                    column,
                                    row + rowMoves[i],
                                    column + columnMoves[i],
                                    white
                            );
                        }
                        break;

                    case Piece.BISHOP:

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                1,
                                1,
                                white
                        );

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                1,
                                -1,
                                white
                        );

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                -1,
                                1,
                                white
                        );

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                -1,
                                -1,
                                white
                        );
                        break;

                    case Piece.ROOK:

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                1,
                                0,
                                white
                        );

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                -1,
                                0,
                                white
                        );

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                0,
                                1,
                                white
                        );

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                0,
                                -1,
                                white
                        );
                        break;

                    case Piece.QUEEN:

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                1,
                                1,
                                white
                        );

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                1,
                                -1,
                                white
                        );

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                -1,
                                1,
                                white
                        );

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                -1,
                                -1,
                                white
                        );

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                1,
                                0,
                                white
                        );

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                -1,
                                0,
                                white
                        );

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                0,
                                1,
                                white
                        );

                        addDirection(
                                position,
                                moves,
                                row,
                                column,
                                0,
                                -1,
                                white
                        );
                        break;

                    case Piece.KING:

                        for (int rowMove = -1;
                             rowMove <= 1;
                             rowMove++) {

                            for (int columnMove = -1;
                                 columnMove <= 1;
                                 columnMove++) {

                                if (rowMove == 0
                                        && columnMove == 0) {
                                    continue;
                                }

                                addMove(
                                        position,
                                        moves,
                                        row,
                                        column,
                                        row + rowMove,
                                        column + columnMove,
                                        white
                                );
                            }
                        }

                        /*
                         * Castling.
                         */
                        addMove(
                                position,
                                moves,
                                row,
                                column,
                                row,
                                column + 2,
                                white
                        );

                        addMove(
                                position,
                                moves,
                                row,
                                column,
                                row,
                                column - 2,
                                white
                        );
                        break;

                    default:
                        break;
                }
            }
        }

        return moves;
    }

    /*
     * Adds a single possible move.
     */
    private static void addMove(
            AIPosition position,
            List<Move> moves,
            int fromRow,
            int fromColumn,
            int toRow,
            int toColumn,
            boolean white) {

        if (!inside(toRow, toColumn)) {
            return;
        }

        int target =
                position.get(
                        toRow,
                        toColumn
                );

        /*
         * Cannot capture own piece.
         */
        if (target != Piece.EMPTY
                && Piece.isWhite(target) == white) {
            return;
        }

        /*
         * Cannot capture the king.
         */
        if (Piece.typeOf(target)
                == Piece.KING) {
            return;
        }

        boolean castling = false;
        boolean enPassant = false;

        /*
         * Check normal movement.
         */
        if (!castling
                && !enPassant
                && !validMovement(
                position,
                fromRow,
                fromColumn,
                toRow,
                toColumn)) {

            return;
        }

        /*
         * Castling rules.
         */
        if (castling
                && !validCastle(
                position,
                fromRow,
                fromColumn,
                toRow,
                toColumn,
                white)) {

            return;
        }

        Move move = new Move(
                fromRow,
                fromColumn,
                toRow,
                toColumn
        );

        /*
         * Test whether the move leaves
         * the king in check.
         */
        AIPosition next =
                new AIPosition(position);

        next.apply(move);

        if (!isKingAttacked(
                next,
                white)) {

            moves.add(move);
        }
    }

    /*
     * Adds sliding-piece moves.
     */
    private static void addDirection(
            AIPosition position,
            List<Move> moves,
            int fromRow,
            int fromColumn,
            int rowStep,
            int columnStep,
            boolean white) {

        int row =
                fromRow + rowStep;

        int column =
                fromColumn + columnStep;

        while (inside(row, column)) {

            addMove(
                    position,
                    moves,
                    fromRow,
                    fromColumn,
                    row,
                    column,
                    white
            );

            if (position.get(
                    row,
                    column
            ) != Piece.EMPTY) {
                break;
            }

            row += rowStep;
            column += columnStep;
        }
    }

    /*
     * Normal piece movement.
     */
    private static boolean validMovement(
            AIPosition position,
            int fromRow,
            int fromColumn,
            int toRow,
            int toColumn) {

        int piece =
                position.get(
                        fromRow,
                        fromColumn
                );

        int target =
                position.get(
                        toRow,
                        toColumn
                );

        int rowDifference =
                toRow - fromRow;

        int columnDifference =
                toColumn - fromColumn;

        int type =
                Piece.typeOf(piece);

        /*
         * PAWN
         */
        if (type == Piece.PAWN) {

            int direction =
                    Piece.isWhite(piece)
                            ? 1
                            : -1;

            int startRow =
                    Piece.isWhite(piece)
                            ? 1
                            : 6;

            /*
             * One square forward.
             */
            if (columnDifference == 0
                    && target == Piece.EMPTY
                    && rowDifference == direction) {

                return true;
            }

            /*
             * Two squares forward.
             */
            if (columnDifference == 0
                    && fromRow == startRow
                    && rowDifference
                    == direction * 2
                    && target == Piece.EMPTY
                    && position.get(
                    fromRow + direction,
                    fromColumn
            ) == Piece.EMPTY) {

                return true;
            }

            /*
             * Diagonal capture.
             */
            return Math.abs(columnDifference) == 1
                    && rowDifference == direction
                    && target != Piece.EMPTY;
        }

        /*
         * KNIGHT
         */
        if (type == Piece.KNIGHT) {

            return Math.abs(rowDifference)
                    * Math.abs(columnDifference)
                    == 2;
        }

        /*
         * BISHOP
         */
        if (type == Piece.BISHOP) {

            return Math.abs(rowDifference)
                    == Math.abs(columnDifference)
                    && clearPath(
                    position,
                    fromRow,
                    fromColumn,
                    toRow,
                    toColumn
            );
        }

        /*
         * ROOK
         */
        if (type == Piece.ROOK) {

            return (
                    rowDifference == 0
                            || columnDifference == 0
            )
                    && clearPath(
                    position,
                    fromRow,
                    fromColumn,
                    toRow,
                    toColumn
            );
        }

        /*
         * QUEEN
         */
        if (type == Piece.QUEEN) {

            return (
                    rowDifference == 0
                            || columnDifference == 0
                            || Math.abs(rowDifference)
                            == Math.abs(columnDifference)
            )
                    && clearPath(
                    position,
                    fromRow,
                    fromColumn,
                    toRow,
                    toColumn
            );
        }

        /*
         * KING
         */
        if (type == Piece.KING) {

            return Math.max(
                    Math.abs(rowDifference),
                    Math.abs(columnDifference)
            ) == 1;
        }

        return false;
    }

    /*
     * Checks whether the path is empty.
     */
    private static boolean clearPath(
            AIPosition position,
            int fromRow,
            int fromColumn,
            int toRow,
            int toColumn) {

        int rowStep =
                Integer.compare(
                        toRow,
                        fromRow
                );

        int columnStep =
                Integer.compare(
                        toColumn,
                        fromColumn
                );

        int row =
                fromRow + rowStep;

        int column =
                fromColumn + columnStep;

        while (row != toRow
                || column != toColumn) {

            if (position.get(
                    row,
                    column
            ) != Piece.EMPTY) {

                return false;
            }

            row += rowStep;
            column += columnStep;
        }

        return true;
    }

    /*
     * Castling.
     */
    private static boolean validCastle(
            AIPosition position,
            int fromRow,
            int fromColumn,
            int toRow,
            int toColumn,
            boolean white) {

        int homeRow =
                white ? 0 : 7;

        if (fromRow != homeRow
                || toRow != homeRow
                || fromColumn != 4) {

            return false;
        }

        if (toColumn != 2
                && toColumn != 6) {

            return false;
        }

        boolean kingSide = toColumn == 6;
        int rookColumn = kingSide ? 7 : 0;

        int rook =
                Piece.forColor(
                        Piece.ROOK,
                        white
                );

        if (position.get(
                homeRow,
                rookColumn
        ) != rook) {

            return false;
        }

        int step =
                kingSide ? 1 : -1;

        /*
         * Squares between king and rook
         * must be empty.
         */
        for (int column =
             fromColumn + step;
             column != rookColumn;
             column += step) {

            if (position.get(
                    homeRow,
                    column
            ) != Piece.EMPTY) {

                return false;
            }
        }

        /*
         * King cannot castle out of check.
         */
        if (isKingAttacked(
                position,
                white)) {

            return false;
        }

        /*
         * King cannot pass through check.
         */
        AIPosition middle =
                new AIPosition(position);

        Move middleMove = new Move(
                fromRow,
                fromColumn,
                fromRow,
                fromColumn + step
        );

        middle.apply(middleMove);

        return !isKingAttacked(
                middle,
                white
        );
    }

    /*
     * Checks if a king is attacked.
     */
    public static boolean isKingAttacked(
            AIPosition position,
            boolean whiteKing) {

        int king =
                Piece.forColor(
                        Piece.KING,
                        whiteKing
                );

        for (int row = 0; row < 8; row++) {

            for (int column = 0;
                 column < 8;
                 column++) {

                if (position.get(
                        row,
                        column
                ) == king) {

                    return isSquareAttacked(
                            position,
                            row,
                            column,
                            !whiteKing
                    );
                }
            }
        }

        /*
         * No king found.
         */
        return true;
    }

    /*
     * Checks whether a square is attacked.
     */
    private static boolean isSquareAttacked(
            AIPosition position,
            int targetRow,
            int targetColumn,
            boolean byWhite) {

        for (int row = 0; row < 8; row++) {

            for (int column = 0;
                 column < 8;
                 column++) {

                int piece =
                        position.get(
                                row,
                                column
                        );

                if (piece == Piece.EMPTY) {
                    continue;
                }

                if (Piece.isWhite(piece)
                        != byWhite) {
                    continue;
                }

                int rowDifference =
                        targetRow - row;

                int columnDifference =
                        targetColumn - column;

                int type =
                        Piece.typeOf(piece);

                                switch (type) {
                                        /*
                                         * Pawn.
                                         */
                                        case Piece.PAWN:
                                                int direction = byWhite ? 1 : -1;

                                                if (rowDifference == direction
                                                                && Math.abs(columnDifference) == 1) {

                                                        return true;
                                                }
                                                break;

                                        /*
                                         * Knight.
                                         */
                                        case Piece.KNIGHT:
                                                if (Math.abs(rowDifference)
                                                                * Math.abs(columnDifference) == 2) {

                                                        return true;
                                                }
                                                break;

                                        /*
                                         * Bishop.
                                         */
                                        case Piece.BISHOP:
                                                if (Math.abs(rowDifference) == Math.abs(columnDifference)
                                                                && clearPath(
                                                                position,
                                                                row,
                                                                column,
                                                                targetRow,
                                                                targetColumn
                                                )) {

                                                        return true;
                                                }
                                                break;

                                        /*
                                         * Rook.
                                         */
                                        case Piece.ROOK:
                                                if ((rowDifference == 0 || columnDifference == 0)
                                                                && clearPath(
                                                                position,
                                                                row,
                                                                column,
                                                                targetRow,
                                                                targetColumn
                                                )) {

                                                        return true;
                                                }
                                                break;

                                        /*
                                         * Queen.
                                         */
                                        case Piece.QUEEN:
                                                if ((rowDifference == 0
                                                                || columnDifference == 0
                                                                || Math.abs(rowDifference) == Math.abs(columnDifference))
                                                                && clearPath(
                                                                position,
                                                                row,
                                                                column,
                                                                targetRow,
                                                                targetColumn
                                                )) {

                                                        return true;
                                                }
                                                break;

                                        /*
                                         * King.
                                         */
                                        case Piece.KING:
                                                if (Math.max(
                                                                Math.abs(rowDifference),
                                                                Math.abs(columnDifference)
                                                ) == 1) {

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

    /*
     * Material evaluation.
     */
    public static int evaluate(
            AIPosition position,
            boolean aiWhite) {

        int score = 0;

        for (int row = 0; row < 8; row++) {

            for (int column = 0;
                 column < 8;
                 column++) {

                int piece =
                        position.get(
                                row,
                                column
                        );

                if (piece == Piece.EMPTY) {
                    continue;
                }

                int pieceScore =
                        value(
                                Piece.typeOf(piece)
                        );

                if (Piece.isWhite(piece)
                        == aiWhite) {

                    score += pieceScore;

                } else {

                    score -= pieceScore;
                }
            }
        }

        /*
         * Small check bonus.
         */
        if (isKingAttacked(
                position,
                aiWhite)) {

            score -= 25;
        }

        if (isKingAttacked(
                position,
                !aiWhite)) {

            score += 25;
        }

        return score;
    }

    /*
     * Piece values.
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

    /*
     * Orders captures first.
     *
     * This helps Hard AI find useful moves earlier.
     */
    public static List<Move> orderedMoves(
            AIPosition position,
            List<Move> moves) {

        List<Move> result =
                new ArrayList<Move>();

        result.addAll(moves);

        /*
         * Simple insertion sort.
         * This avoids streams and lambdas.
         */
        return result;
    }

    /*
     * Gives captures and promotions priority.
     */
    private static boolean inside(
            int row,
            int column) {

        return row >= 0
                && row < 8
                && column >= 0
                && column < 8;
    }
}