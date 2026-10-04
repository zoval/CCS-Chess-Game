package io.github.ccs.qa;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import io.github.ccs.game_logic.Board;

/** Tests for the move-query API the UI visual aids rely on. */
public class BoardQueryTest {

    @Test
    public void isLegalMoveMatchesOpeningRules() {
        Board board = new Board();
        Assert.assertTrue(board.isLegalMove(1, 0, 2, 0));
        Assert.assertTrue(board.isLegalMove(1, 0, 3, 0));
        Assert.assertFalse("pawns cannot push three squares", board.isLegalMove(1, 0, 4, 0));
        Assert.assertFalse("blocked rook cannot move", board.isLegalMove(0, 0, 0, 4));
        Assert.assertFalse("moving opponent pieces is not legal", board.isLegalMove(6, 0, 5, 0));
    }

    @Test
    public void getLegalMovesListsPawnAndKnightDestinations() {
        Board board = new Board();
        assertMovesContain(board.getLegalMoves(1, 0), new int[][]{{2, 0}, {3, 0}});
        assertMovesContain(board.getLegalMoves(0, 1), new int[][]{{2, 0}, {2, 2}});
        Assert.assertTrue("blocked pieces have no moves",
            board.getLegalMoves(0, 0).isEmpty());
        Assert.assertTrue("empty squares have no moves",
            board.getLegalMoves(3, 3).isEmpty());
    }

    @Test
    public void kingsStartSafeAndFoolsMatePutsWhiteInCheck() {
        Board board = new Board();
        Assert.assertFalse(board.isKingInCheck(true));
        Assert.assertFalse(board.isKingInCheck(false));
        Assert.assertFalse(board.isCurrentKingInCheck());
        Assert.assertArrayEquals(new int[]{0, 4}, board.getKingPosition(true));
        Assert.assertArrayEquals(new int[]{7, 4}, board.getKingPosition(false));

        playFoolsMate(board);

        Assert.assertTrue(board.isKingInCheck(true));
        Assert.assertFalse(board.isKingInCheck(false));
        Assert.assertTrue(board.isCurrentKingInCheck());
        Assert.assertArrayEquals(new int[]{0, 4}, board.getKingPosition(true));
    }

    @Test
    public void legalMovesRespectSideToMove() {
        Board board = new Board();
        Assert.assertTrue("black pieces yield no queried moves on white's turn",
            board.getLegalMoves(6, 3).isEmpty());

        Assert.assertTrue(board.move(1, 4, 3, 4));
        assertMovesContain(board.getLegalMoves(6, 3), new int[][]{{5, 3}, {4, 3}});
        Assert.assertTrue("white pieces yield no queried moves on black's turn",
            board.getLegalMoves(1, 0).isEmpty());
    }

    private void playFoolsMate(Board board) {
        Assert.assertTrue(board.move(1, 5, 2, 5));
        Assert.assertTrue(board.move(6, 4, 4, 4));
        Assert.assertTrue(board.move(1, 6, 3, 6));
        Assert.assertTrue(board.move(7, 3, 3, 7));
    }

    private void assertMovesContain(List<int[]> moves, int[][] expected) {
        for (int[] destination : expected) {
            boolean found = false;
            for (int[] move : moves) {
                if (move[0] == destination[0] && move[1] == destination[1]) {
                    found = true;
                    break;
                }
            }
            Assert.assertTrue("expected move to " + destination[0] + "," + destination[1],
                found);
        }
        Assert.assertEquals(expected.length, moves.size());
    }
}
