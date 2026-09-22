package io.github.ccs.backend;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

import io.github.ccs.game_logic.Board;

public class SaveData {
    public int[][] pieceLocation;
    public boolean isWhiteTurn;

    public SaveData() {
    }

    public static SaveData capture(Board board) {
        SaveData data = new SaveData();
        data.pieceLocation = new int[8][8];
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                data.pieceLocation[row][column] = board.getPiece(row, column);
                System.out.print(data.pieceLocation[row][column] + " ");
            }
            System.out.println();
        }
        data.isWhiteTurn = board.isWhiteTurn();
        System.out.println(data.isWhiteTurn ? "White's turn" : "Black's turn");
        return data;
    }

    //Applies this saved data onto the given board, restoring its state.
    public void applyTo(Board board) {
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                board.position.setPiece(row, column, pieceLocation[row][column]);
            }
        }
        board.setWhiteTurn(isWhiteTurn);
    }
}