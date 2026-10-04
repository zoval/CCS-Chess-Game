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

	public boolean isWhiteKingMoved() {
		return whiteKingMoved;
	}

	public boolean isBlackKingMoved() {
		return blackKingMoved;
	}

	public boolean isWhiteKingSideRookMoved() {
		return whiteKingSideRookMoved;
	}

	public boolean isWhiteQueenSideRookMoved() {
		return whiteQueenSideRookMoved;
	}

	public boolean isBlackKingSideRookMoved() {
		return blackKingSideRookMoved;
	}

	public boolean isBlackQueenSideRookMoved() {
		return blackQueenSideRookMoved;
	}

	public int getLastFromRow() {
		return lastFromRow;
	}

	public int getLastFromColumn() {
		return lastFromColumn;
	}

	public int getLastToRow() {
		return lastToRow;
	}

	public int getLastToColumn() {
		return lastToColumn;
	}

	public int getLastPiece() {
		return lastPiece;
	}

	/** Copies every field from another state (history navigation, save/load). */
	public void restore(SpecialMoves other) {
		whiteKingMoved = other.whiteKingMoved;
		blackKingMoved = other.blackKingMoved;
		whiteKingSideRookMoved = other.whiteKingSideRookMoved;
		whiteQueenSideRookMoved = other.whiteQueenSideRookMoved;
		blackKingSideRookMoved = other.blackKingSideRookMoved;
		blackQueenSideRookMoved = other.blackQueenSideRookMoved;
		lastFromRow = other.lastFromRow;
		lastFromColumn = other.lastFromColumn;
		lastToRow = other.lastToRow;
		lastToColumn = other.lastToColumn;
		lastPiece = other.lastPiece;
	}

	/** @return a compact single-line encoding of every field (save/load). */
	public String encode() {
		return (whiteKingMoved ? 1 : 0) + "," + (blackKingMoved ? 1 : 0) + ","
			+ (whiteKingSideRookMoved ? 1 : 0) + "," + (whiteQueenSideRookMoved ? 1 : 0) + ","
			+ (blackKingSideRookMoved ? 1 : 0) + "," + (blackQueenSideRookMoved ? 1 : 0) + ","
			+ lastFromRow + "," + lastFromColumn + "," + lastToRow + "," + lastToColumn + ","
			+ lastPiece;
	}

	/**
	 * Rebuilds a state from {@link #encode()} output.
	 *
	 * @throws IllegalArgumentException if the string is not a valid encoding.
	 */
	public static SpecialMoves decode(String encoded) {
		SpecialMoves moves = new SpecialMoves();
		if (encoded == null || encoded.isEmpty()) {
			return moves;
		}

		String[] fields = encoded.split(",");
		if (fields.length != 11) {
			throw new IllegalArgumentException("Expected 11 fields, got " + fields.length);
		}

		moves.whiteKingMoved = Integer.parseInt(fields[0]) != 0;
		moves.blackKingMoved = Integer.parseInt(fields[1]) != 0;
		moves.whiteKingSideRookMoved = Integer.parseInt(fields[2]) != 0;
		moves.whiteQueenSideRookMoved = Integer.parseInt(fields[3]) != 0;
		moves.blackKingSideRookMoved = Integer.parseInt(fields[4]) != 0;
		moves.blackQueenSideRookMoved = Integer.parseInt(fields[5]) != 0;
		moves.lastFromRow = Integer.parseInt(fields[6]);
		moves.lastFromColumn = Integer.parseInt(fields[7]);
		moves.lastToRow = Integer.parseInt(fields[8]);
		moves.lastToColumn = Integer.parseInt(fields[9]);
		moves.lastPiece = Integer.parseInt(fields[10]);
		return moves;
	}

	/** @return an independent deep copy of this state. */
	public SpecialMoves copy() {
		SpecialMoves copy = new SpecialMoves();
		copy.restore(this);
		return copy;
	}

	//promotion is handled in Board class, but needed to be here to check if a pawn is being promoted
	public static boolean isPromotion(int piece, int row) {
		return Piece.typeOf(piece) == Piece.PAWN && (row == 0 || row == 7);
	}

	//checks if promoted piece is valid, as isPromotion does states that any piece at the ends of the board
	//is a promotion, but we need to check if the piece is valid for promotion
	public static int promotedPiece(int pawn, int promotedType) {
		if (Piece.typeOf(pawn) != Piece.PAWN) {
			throw new IllegalArgumentException("Only pawns can be promoted");
		}
		//only allow promotion to queen, rook, bishop, or knight
		//else pick a piece again
		if (promotedType != Piece.QUEEN && promotedType != Piece.ROOK
			&& promotedType != Piece.BISHOP && promotedType != Piece.KNIGHT) {
			throw new IllegalArgumentException("A pawn must promote to a queen, rook, bishop, or knight");
		}
		return Piece.forColor(promotedType, Piece.isWhite(pawn));
	}

	//checks the conditions for castling
	public boolean isCastlingMove(Position position, int fromRow, int fromColumn,
		int toRow, int toColumn, boolean white, MoveValidator validator) {
		if (position.getPiece(fromRow, fromColumn) != Piece.forColor(Piece.KING, white)
			|| fromRow != (white ? 0 : 7) || fromColumn != 4 || toRow != fromRow       //so tldr: if the piece is not a king, or the king is not in its 
			|| (toColumn != 2 && toColumn != 6) || position.getPiece(toRow, toColumn) != Piece.EMPTY //original position, or the king is not moving to the same row, return false
			|| (white ? whiteKingMoved : blackKingMoved)){
				//added braces for intuivity
				return false;
			} 

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
		if (validator.isKingAttacked(position, white)) return false; //because if the king is in check, it cannot castle

		position.setPiece(fromRow, fromColumn, Piece.EMPTY);
		position.setPiece(fromRow, fromColumn + step, Piece.forColor(Piece.KING, white));
		boolean safe = !validator.isKingAttacked(position, white); //check if the king is attacked after moving one square towards the rook, if safe, then castle allowed
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
		//toColums is not read at all, but program breaks if I remove it, so no touchy touch
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

	//reset state for a new game
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
