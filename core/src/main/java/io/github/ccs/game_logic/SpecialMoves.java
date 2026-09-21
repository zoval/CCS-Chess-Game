package io.github.ccs.game_logic;

/** Stores state and rules for castling, en passant, and promotion. */
public final class SpecialMoves {
	private boolean whiteKingMoved;
	private boolean blackKingMoved;
	private boolean whiteKingSideRookMoved;
	private boolean whiteQueenSideRookMoved;
	private boolean blackKingSideRookMoved;
	private boolean blackQueenSideRookMoved;
	private int lastFromRow = -1;
	private int lastFromColumn = -1;
	private int lastToRow = -1;
	private int lastToColumn = -1;
	private int lastPiece = Piece.EMPTY;

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

	public boolean isCastlingMove(Position position, int fromRow, int fromColumn,
		int toRow, int toColumn, boolean white, MoveValidator validator) {
		if (position.getPiece(fromRow, fromColumn) != Piece.forColor(Piece.KING, white)
			|| fromRow != (white ? 0 : 7) || fromColumn != 4 || toRow != fromRow
			|| (toColumn != 2 && toColumn != 6) || position.getPiece(toRow, toColumn) != Piece.EMPTY
			|| (white ? whiteKingMoved : blackKingMoved)) return false;

		boolean kingSide = toColumn == 6;
		int rookColumn = kingSide ? 7 : 0;
		boolean rookMoved = white
			? (kingSide ? whiteKingSideRookMoved : whiteQueenSideRookMoved)
			: (kingSide ? blackKingSideRookMoved : blackQueenSideRookMoved);
		if (rookMoved || position.getPiece(fromRow, rookColumn) != Piece.forColor(Piece.ROOK, white)) return false;

		int step = kingSide ? 1 : -1;
		for (int column = fromColumn + step; column != rookColumn; column += step) {
			if (position.getPiece(fromRow, column) != Piece.EMPTY) return false;
		}
		if (validator.isKingAttacked(position, white)) return false;

		position.setPiece(fromRow, fromColumn, Piece.EMPTY);
		position.setPiece(fromRow, fromColumn + step, Piece.forColor(Piece.KING, white));
		boolean safe = !validator.isKingAttacked(position, white);
		position.setPiece(fromRow, fromColumn + step, Piece.EMPTY);
		position.setPiece(fromRow, fromColumn, Piece.forColor(Piece.KING, white));
		return safe;
	}

	public boolean isEnPassantMove(Position position, int fromRow, int fromColumn,
		int toRow, int toColumn, boolean white) {
		int pawn = Piece.forColor(Piece.PAWN, white);
		int direction = white ? 1 : -1;
		return position.getPiece(fromRow, fromColumn) == pawn
			&& toRow - fromRow == direction
			&& Math.abs(toColumn - fromColumn) == 1
			&& position.getPiece(toRow, toColumn) == Piece.EMPTY
			&& lastPiece == Piece.forColor(Piece.PAWN, !white)
			&& lastFromRow == (white ? 6 : 1)
			&& lastToRow == (white ? 4 : 3)
			&& lastToRow == fromRow
			&& lastToColumn == toColumn;
	}

	public void recordMove(int fromRow, int fromColumn, int toRow, int toColumn, int piece) {
		lastFromRow = fromRow;
		lastFromColumn = fromColumn;
		lastToRow = toRow;
		lastToColumn = toColumn;
		lastPiece = piece;
		boolean white = Piece.isWhite(piece);
		if (Piece.typeOf(piece) == Piece.KING) {
			if (white) whiteKingMoved = true;
			else blackKingMoved = true;
		} else if (Piece.typeOf(piece) == Piece.ROOK) {
			if (white && fromRow == 0 && fromColumn == 0) whiteQueenSideRookMoved = true;
			if (white && fromRow == 0 && fromColumn == 7) whiteKingSideRookMoved = true;
			if (!white && fromRow == 7 && fromColumn == 0) blackQueenSideRookMoved = true;
			if (!white && fromRow == 7 && fromColumn == 7) blackKingSideRookMoved = true;
		}
	}

	public void reset() {
		whiteKingMoved = false;
		blackKingMoved = false;
		whiteKingSideRookMoved = false;
		whiteQueenSideRookMoved = false;
		blackKingSideRookMoved = false;
		blackQueenSideRookMoved = false;
		lastFromRow = -1;
		lastFromColumn = -1;
		lastToRow = -1;
		lastToColumn = -1;
		lastPiece = Piece.EMPTY;
	}
}
