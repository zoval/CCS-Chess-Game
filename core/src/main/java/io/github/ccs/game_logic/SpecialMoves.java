package io.github.ccs.game_logic;

/** rules shared by chess moves that do not use normal piece movement. */
//this is added by romero.
public final class SpecialMoves {
	private SpecialMoves() {
	}

	public static boolean isPromotion(int piece, int row) {
		return Piece.typeOf(piece) == Piece.PAWN && (row == 0 || row == 7);
	}

	public static int promotedPiece(int pawn, int promotedType) {
		if (Piece.typeOf(pawn) != Piece.PAWN) {
			throw new IllegalArgumentException("Only pawns can be promoted");
		}
		if (promotedType != Piece.QUEEN && promotedType != Piece.ROOK
			&& promotedType != Piece.BISHOP && promotedType != Piece.KNIGHT) {
			throw new IllegalArgumentException("A pawn must promote to a queen, rook, bishop, or knight");
		}
		return Piece.forColor(promotedType, Piece.isWhite(pawn));
	}
}
