package io.github.ccs.qa;

import java.util.List;

import org.junit.Test;

import io.github.ccs.ai_networking.AIFactory;
import io.github.ccs.ai_networking.AIPosition;
import io.github.ccs.ai_networking.AIUtils;
import io.github.ccs.ai_networking.Difficulty;
import io.github.ccs.ai_networking.EasyAI;
import io.github.ccs.ai_networking.HardAI;
import io.github.ccs.ai_networking.MediumAI;
import io.github.ccs.ai_networking.Move;
import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;

public class AISearchTest {

    @Test
    public void aipositionAutoQueensOnTheLastRank() {
        AIPosition position = new AIPosition();
        position.set(6, 0, Piece.PAWN);
        position.apply(new Move(6, 0, 7, 0));

        if (position.get(7, 0) != Piece.QUEEN) {
            throw new AssertionError("Expected the white pawn to auto-queen on row 7");
        }

        position = new AIPosition();
        position.set(1, 0, -Piece.PAWN);
        position.apply(new Move(1, 0, 0, 0));

        if (position.get(0, 0) != -Piece.QUEEN) {
            throw new AssertionError("Expected the black pawn to auto-queen on row 0");
        }

        position = new AIPosition();
        position.set(6, 0, Piece.ROOK);
        position.apply(new Move(6, 0, 7, 0));

        if (position.get(7, 0) != Piece.ROOK) {
            throw new AssertionError("Only pawns may auto-queen");
        }
    }

    @Test
    public void searchOrderedMovesPutsPromotionsFirstThenMvvLvaCapturesThenQuiet() {
        AIPosition position = new AIPosition();
        position.set(0, 0, Piece.forColor(Piece.KING, true));
        position.set(6, 4, Piece.PAWN);
        position.set(6, 6, Piece.PAWN);
        position.set(2, 3, Piece.PAWN);
        position.set(4, 0, Piece.forColor(Piece.ROOK, true));
        position.set(5, 5, Piece.forColor(Piece.KNIGHT, true));
        position.set(7, 7, Piece.forColor(Piece.KING, false));
        position.set(7, 5, Piece.forColor(Piece.ROOK, false));
        position.set(4, 7, Piece.forColor(Piece.KNIGHT, false));
        position.set(3, 4, Piece.forColor(Piece.BISHOP, false));

        List<Move> moves = AIUtils.legalMoves(position, true);
        List<Move> ordered = AIUtils.searchOrderedMoves(position, moves);

        Move promoPush = new Move(6, 4, 7, 4);
        Move promoCapture = new Move(6, 6, 7, 5, true);
        // Knight takes bishop: 330 - 320/10 = 298 beats rook takes knight: 320 - 500/10 = 270.
        Move knightTakesBishop = new Move(5, 5, 3, 4, true);
        Move rookTakesKnight = new Move(4, 0, 4, 7, true);
        Move quietPush = new Move(2, 3, 3, 3);

        if (ordered.size() != moves.size()) {
            throw new AssertionError("searchOrderedMoves must keep every move");
        }

        int promoPushIndex = ordered.indexOf(promoPush);
        int promoCaptureIndex = ordered.indexOf(promoCapture);
        int knightBishopIndex = ordered.indexOf(knightTakesBishop);
        int rookKnightIndex = ordered.indexOf(rookTakesKnight);
        int quietIndex = ordered.indexOf(quietPush);

        if (promoPushIndex < 0 || promoCaptureIndex < 0 || knightBishopIndex < 0
                || rookKnightIndex < 0 || quietIndex < 0) {
            throw new AssertionError("Expected all reference moves to stay legal, got: " + ordered);
        }

        if (!(promoPushIndex < promoCaptureIndex)) {
            throw new AssertionError("Promotions must come first (push before capture-promo)");
        }
        if (!(promoCaptureIndex < knightBishopIndex)) {
            throw new AssertionError("Captures must come after promotions");
        }
        if (!(knightBishopIndex < rookKnightIndex)) {
            throw new AssertionError("MVV-LVA must rank knight-takes-bishop (298) above rook-takes-knight (270)");
        }
        if (!(rookKnightIndex < quietIndex)) {
            throw new AssertionError("Quiet moves must come last");
        }
    }

    @Test
    public void hardAiFindsMateInOne() {
        Board board = new Board();
        playFoolsMatePrelude(board);

        long start = System.currentTimeMillis();
        Move move = new HardAI().computeMove(board);
        long elapsed = System.currentTimeMillis() - start;

        if (move == null || move.fromRow != 7 || move.fromColumn != 3
                || move.toRow != 3 || move.toColumn != 7) {
            throw new AssertionError("Expected HardAI to find Qd8-h4 mate in one, got: " + move);
        }
        if (board.isWhiteTurn()) {
            throw new AssertionError("computeMove must not mutate the board");
        }
        if (elapsed > 10_000) {
            throw new AssertionError("Expected the mate-in-one search to finish quickly, took " + elapsed + "ms");
        }
    }

    @Test
    public void hardAiPlaysTheMateAndEndsTheGame() {
        Board board = new Board();
        playFoolsMatePrelude(board);

        if (!new HardAI().makeMove(board)) {
            throw new AssertionError("Expected HardAI to play the mating move");
        }
        if (!board.isGameOver()) {
            throw new AssertionError("Expected the game to be over after Qh4#");
        }
        if (!"Checkmate - Black wins".equals(board.getStatusText())) {
            throw new AssertionError("Expected 'Checkmate - Black wins', got: " + board.getStatusText());
        }
    }

    @Test
    public void aifactoryCreatesTheBotForTheDifficulty() {
        if (!(AIFactory.create(Difficulty.EASY) instanceof EasyAI)) {
            throw new AssertionError("EASY must create an EasyAI");
        }
        if (!(AIFactory.create(Difficulty.MEDIUM) instanceof MediumAI)) {
            throw new AssertionError("MEDIUM must create a MediumAI");
        }
        if (!(AIFactory.create(Difficulty.HARD) instanceof HardAI)) {
            throw new AssertionError("HARD must create a HardAI");
        }
    }

    @Test
    public void mediumAiPlaysMateInOne() {
        Board board = new Board();
        playFoolsMatePrelude(board);

        Move move = new MediumAI().computeMove(board);

        if (move == null || move.fromRow != 7 || move.fromColumn != 3
                || move.toRow != 3 || move.toColumn != 7) {
            throw new AssertionError("Expected MediumAI to find Qd8-h4 mate in one, got: " + move);
        }
    }

    private void playFoolsMatePrelude(Board board) {
        if (!board.move(1, 5, 2, 5)) throw new AssertionError("f2-f3 rejected"); // 1. f3
        if (!board.move(6, 4, 4, 4)) throw new AssertionError("e7-e5 rejected"); // 1... e5
        if (!board.move(1, 6, 3, 6)) throw new AssertionError("g2-g4 rejected"); // 2. g4
    }
}
