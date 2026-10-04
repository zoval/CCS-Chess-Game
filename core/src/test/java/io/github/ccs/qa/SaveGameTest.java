package io.github.ccs.qa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import io.github.ccs.backend.SaveGame;
import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.GameState;
import io.github.ccs.game_logic.MoveSnapshot;
import io.github.ccs.game_logic.Piece;

public class SaveGameTest {

    @Test
    public void defaultGameRoundTrips() {
        GameState state = buildState(new Board(), 600000, 600000);

        GameState restored = SaveGame.parse(SaveGame.serialize(state));

        assertStateEquals(state, restored);
        if (restored.getSnapshots().size() != 1) {
            throw new AssertionError("Expected 1 snapshot, got " + restored.getSnapshots().size());
        }
    }

    @Test
    public void midGameRoundTripsSpecialMoveState() {
        Board board = new Board();
        playMoves(board, new int[][] {
            {1, 4, 3, 4}, // e4
            {6, 0, 5, 0}, // a6
            {3, 4, 4, 4}, // e5
            {6, 3, 4, 3}, // d5
            {4, 4, 5, 3}, // exd6 en passant
            {6, 2, 4, 2}, // c5
            {0, 4, 1, 4}, // Ke2 (revokes White castling rights)
            {7, 3, 6, 2}  // Qc7
        });

        GameState state = buildState(board, 300000, 295432);
        state.setTimeControlMinutes(5);

        GameState restored = SaveGame.parse(SaveGame.serialize(state));

        assertStateEquals(state, restored);
    }

    @Test
    public void promotionRoundTrips() {
        Board board = new Board();
        playMoves(board, new int[][] {
            {1, 0, 3, 0}, // a4
            {6, 7, 4, 7}, // h5
            {3, 0, 4, 0}, // a5
            {4, 7, 3, 7}, // h4
            {4, 0, 5, 0}, // a6
            {3, 7, 2, 7}, // h3
            {5, 0, 6, 1}, // axb7 (a7 is blocked by Black's a-pawn)
            {2, 7, 1, 6}  // hxg2
        });
        if (!board.move(6, 1, 7, 0, Piece.QUEEN)) {
            throw new AssertionError("bxa8=Q was rejected");
        }

        GameState state = buildState(board, 412000, 389500);
        GameState restored = SaveGame.parse(SaveGame.serialize(state));

        assertStateEquals(state, restored);
        MoveSnapshot last = lastOf(restored);
        if (last.getPiece(7, 0) != Piece.forColor(Piece.QUEEN, true)) {
            throw new AssertionError("Expected a white queen on a8 after promotion");
        }
        if (!last.getCapturedByWhite().contains(Piece.forColor(Piece.PAWN, false))
                || !last.getCapturedByWhite().contains(Piece.forColor(Piece.ROOK, false))) {
            throw new AssertionError("Expected White's captures to contain the b-pawn and a8 rook");
        }
        if (!last.getCapturedByBlack().contains(Piece.forColor(Piece.PAWN, true))) {
            throw new AssertionError("Expected Black's captured list to contain the g2 pawn");
        }
    }

    @Test
    public void corruptSavesParseToNull() {
        GameState state = buildState(new Board(), 600000, 600000);
        Properties valid = SaveGame.serialize(state);

        if (SaveGame.parse(new Properties()) != null) {
            throw new AssertionError("Expected an empty payload to parse to null");
        }

        Properties futureVersion = (Properties) valid.clone();
        futureVersion.setProperty("version", "99");
        if (SaveGame.parse(futureVersion) != null) {
            throw new AssertionError("Expected a future version to parse to null");
        }

        Properties badCount = (Properties) valid.clone();
        badCount.setProperty("snapshot.count", "abc");
        if (SaveGame.parse(badCount) != null) {
            throw new AssertionError("Expected a malformed snapshot count to parse to null");
        }

        Properties missingPieces = (Properties) valid.clone();
        missingPieces.remove("snapshot.0.pieces");
        if (SaveGame.parse(missingPieces) != null) {
            throw new AssertionError("Expected a missing piece grid to parse to null");
        }

        Properties badHistoryIndex = (Properties) valid.clone();
        badHistoryIndex.setProperty("historyIndex", "5");
        if (SaveGame.parse(badHistoryIndex) != null) {
            throw new AssertionError("Expected an out-of-range history index to parse to null");
        }
    }

    private GameState buildState(Board board, long whiteMillis, long blackMillis) {
        GameState state = new GameState();
        state.setMode("P_V_AI");
        state.setDifficulty("HARD");
        state.setMapName("CLASSIC");
        state.setTimeControlMinutes(10);
        state.setWhiteMillis(whiteMillis);
        state.setBlackMillis(blackMillis);
        state.setSnapshots(new ArrayList<MoveSnapshot>(board.getHistory()));
        state.setHistoryIndex(board.getHistoryIndex());
        return state;
    }

    private void playMoves(Board board, int[][] moves) {
        for (int[] move : moves) {
            if (!board.move(move[0], move[1], move[2], move[3])) {
                throw new AssertionError("Move " + Arrays.toString(move) + " was rejected");
            }
        }
    }

    private MoveSnapshot lastOf(GameState state) {
        return state.getSnapshots().get(state.getSnapshots().size() - 1);
    }

    private void assertStateEquals(GameState expected, GameState actual) {
        if (actual == null) {
            throw new AssertionError("Expected the parsed state to be non-null");
        }
        if (!expected.getMode().equals(actual.getMode())) {
            throw new AssertionError("Mode changed: " + actual.getMode());
        }
        if (!expected.getDifficulty().equals(actual.getDifficulty())) {
            throw new AssertionError("Difficulty changed: " + actual.getDifficulty());
        }
        if (!expected.getMapName().equals(actual.getMapName())) {
            throw new AssertionError("Map changed: " + actual.getMapName());
        }
        if (expected.getTimeControlMinutes() != actual.getTimeControlMinutes()) {
            throw new AssertionError("Time control changed: " + actual.getTimeControlMinutes());
        }
        if (expected.getWhiteMillis() != actual.getWhiteMillis()) {
            throw new AssertionError("White clock changed: " + actual.getWhiteMillis());
        }
        if (expected.getBlackMillis() != actual.getBlackMillis()) {
            throw new AssertionError("Black clock changed: " + actual.getBlackMillis());
        }
        if (expected.getHistoryIndex() != actual.getHistoryIndex()) {
            throw new AssertionError("History index changed: " + actual.getHistoryIndex());
        }
        assertSnapshotsEqual(expected.getSnapshots(), actual.getSnapshots());
    }

    private void assertSnapshotsEqual(List<MoveSnapshot> expected, List<MoveSnapshot> actual) {
        if (expected.size() != actual.size()) {
            throw new AssertionError("Snapshot count changed: expected " + expected.size()
                + ", got " + actual.size());
        }
        for (int i = 0; i < expected.size(); i++) {
            MoveSnapshot a = expected.get(i);
            MoveSnapshot b = actual.get(i);
            if (!Arrays.equals(a.getPieces(), b.getPieces())) {
                throw new AssertionError("Pieces differ in snapshot " + i);
            }
            if (a.isWhiteTurn() != b.isWhiteTurn()) {
                throw new AssertionError("Turn differs in snapshot " + i);
            }
            if (!a.getStatusText().equals(b.getStatusText())) {
                throw new AssertionError("Status text differs in snapshot " + i);
            }
            if (a.isGameOver() != b.isGameOver()) {
                throw new AssertionError("Game-over flag differs in snapshot " + i);
            }
            if (a.getHalfmoveClock() != b.getHalfmoveClock()) {
                throw new AssertionError("Halfmove clock differs in snapshot " + i);
            }
            if (!a.getSpecialMoves().encode().equals(b.getSpecialMoves().encode())) {
                throw new AssertionError("Special-move state differs in snapshot " + i);
            }
            if (!a.getCapturedByWhite().equals(b.getCapturedByWhite())) {
                throw new AssertionError("White captures differ in snapshot " + i);
            }
            if (!a.getCapturedByBlack().equals(b.getCapturedByBlack())) {
                throw new AssertionError("Black captures differ in snapshot " + i);
            }
        }
    }
}
