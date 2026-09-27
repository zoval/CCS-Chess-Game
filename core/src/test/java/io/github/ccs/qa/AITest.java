package io.github.ccs.qa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import io.github.ccs.ai_networking.AIPosition;
import io.github.ccs.ai_networking.AIUtils;
import io.github.ccs.ai_networking.EasyAI;
import io.github.ccs.ai_networking.HardAI;
import io.github.ccs.ai_networking.MediumAI;
import io.github.ccs.ai_networking.Move;
import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;

public class AITest {

    @Test
    public void startingPositionHasTwentyLegalMovesPerSide() {
        AIPosition position = AIUtils.read(new Board());

        if (AIUtils.legalMoves(position, true).size() != 20) {
            throw new AssertionError("Expected 20 legal moves for White");
        }
        if (AIUtils.legalMoves(position, false).size() != 20) {
            throw new AssertionError("Expected 20 legal moves for Black");
        }
    }

    @Test
    public void quietStartingPositionEvaluatesToZero() {
        AIPosition position = AIUtils.read(new Board());

        if (AIUtils.evaluate(position, true) != 0) {
            throw new AssertionError("Expected a symmetric starting position to evaluate to 0");
        }
        if (AIUtils.evaluate(position, false) != 0) {
            throw new AssertionError("Expected a symmetric starting position to evaluate to 0 for Black");
        }
    }

    @Test
    public void captureFlagIsDetectedByMoveGeneration() {
        AIPosition position = new AIPosition();
        position.set(0, 4, Piece.forColor(Piece.KING, true));
        position.set(7, 4, Piece.forColor(Piece.KING, false));
        position.set(4, 4, Piece.forColor(Piece.PAWN, true));
        position.set(5, 3, Piece.forColor(Piece.PAWN, false));

        List<Move> moves = AIUtils.legalMoves(position, true);

        Move pawnPush = new Move(4, 4, 5, 4);
        Move diagonalCapture = new Move(4, 4, 5, 3, true);

        if (!moves.contains(pawnPush)) {
            throw new AssertionError("Expected the pawn push " + pawnPush + " to be legal");
        }
        if (!moves.contains(diagonalCapture)) {
            throw new AssertionError("Expected the diagonal capture " + diagonalCapture + " to be generated");
        }
        if (moves.get(moves.indexOf(pawnPush)).isCapture()) {
            throw new AssertionError("Expected the pawn push to be a non-capture");
        }
        if (!moves.get(moves.indexOf(diagonalCapture)).isCapture()) {
            throw new AssertionError("Expected the diagonal pawn move to be flagged as a capture");
        }
    }

    @Test
    public void orderedMovesPutsCapturesFirstAndKeepsOrder() {
        AIPosition position = new AIPosition();
        Move firstQuiet = new Move(4, 4, 5, 4);
        Move capture = new Move(4, 4, 5, 3, true);
        Move secondQuiet = new Move(1, 4, 3, 4);
        List<Move> moves = new ArrayList<Move>(Arrays.asList(firstQuiet, capture, secondQuiet));

        List<Move> ordered = AIUtils.orderedMoves(position, moves);

        if (ordered.size() != 3) {
            throw new AssertionError("Expected orderedMoves to preserve every move");
        }
        if (!ordered.get(0).equals(capture)) {
            throw new AssertionError("Expected the capture to be ordered first, got: " + ordered.get(0));
        }
        if (!ordered.get(1).equals(firstQuiet) || !ordered.get(2).equals(secondQuiet)) {
            throw new AssertionError("Expected quiet moves to keep their original relative order");
        }
    }

    @Test
    public void checkmatedSideHasNoLegalMoves() {
        Board board = new Board();
        playFoolsMate(board);

        AIPosition position = AIUtils.read(board);

        if (!AIUtils.isKingAttacked(position, true)) {
            throw new AssertionError("Expected the white king to be attacked after Fool's Mate");
        }
        if (!AIUtils.legalMoves(position, true).isEmpty()) {
            throw new AssertionError("Expected no legal moves for the checkmated side");
        }
    }

    @Test
    public void easyAiPlaysALegalMove() {
        assertBotPlaysMove(new EasyAI()::makeMove);
    }

    @Test
    public void mediumAiPlaysALegalMove() {
        assertBotPlaysMove(new MediumAI()::makeMove);
    }

    @Test
    public void hardAiPlaysALegalMove() {
        assertBotPlaysMove(new HardAI()::makeMove);
    }

    private void assertBotPlaysMove(Function<Board, Boolean> makeMove) {
        Board board = new Board();

        long start = System.currentTimeMillis();
        boolean played = makeMove.apply(board);
        long elapsed = System.currentTimeMillis() - start;

        if (!played) {
            throw new AssertionError("Expected the bot to play a move from the starting position");
        }
        if (board.isWhiteTurn()) {
            throw new AssertionError("Expected the turn to pass to Black after the bot moved");
        }
        if (countPieces(board) != 32) {
            throw new AssertionError("Expected all 32 pieces to still be on the board");
        }
        if (elapsed > 10_000) {
            throw new AssertionError("Expected the bot to move in under 10 seconds, took " + elapsed + "ms");
        }
    }

    private int countPieces(Board board) {
        int count = 0;

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                if (board.getPiece(row, column) != Piece.EMPTY) {
                    count++;
                }
            }
        }

        return count;
    }

    private void playFoolsMate(Board board) {
        if (!board.move(1, 5, 2, 5)) throw new AssertionError("f2-f3 rejected"); // 1. f3
        if (!board.move(6, 4, 4, 4)) throw new AssertionError("e7-e5 rejected"); // 1... e5
        if (!board.move(1, 6, 3, 6)) throw new AssertionError("g2-g4 rejected"); // 2. g4
        if (!board.move(7, 3, 3, 7)) throw new AssertionError("Qd8-h4 rejected"); // 2... Qh4#
    }
}
