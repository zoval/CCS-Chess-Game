package io.github.ccs.backend;
import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Position;
import io.github.ccs.game_logic.Piece;

//this is the method that 
public class SaveData {
    Board Board = new Board();
    Position Position = new Position();
    

    public static void SaveGame() {
        //This should scan the board and save the pieces' current location
        int[][] pieceLocation = new int[8][8];
        {
            pieceLocation[1][1] = Piece.ROOK;
            System.out.print(pieceLocation[1][1]);
            System.out.print("Hello");
        }
    }
    

    /*
    This retains the players turn. 
    White's turn = True, Black's turn = false
    */
    public boolean isWhite = Board.isWhiteTurn();

}