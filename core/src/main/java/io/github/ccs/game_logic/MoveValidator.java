package io.github.ccs.game_logic;

/** Validates piece movement, paths, attacks, and king safety. */
public final class MoveValidator {
    public boolean isLegalMove(Position position, int fromRow, int fromColumn,
        int toRow, int toColumn, boolean whiteTurn) {
        if (!position.contains(fromRow, fromColumn) || !position.contains(toRow, toColumn)) return false;
        int piece = position.getPiece(fromRow, fromColumn);
        int target = position.getPiece(toRow, toColumn);
        if (piece == Piece.EMPTY || Piece.isWhite(piece) != whiteTurn
            || Piece.typeOf(target) == Piece.KING
            || (target != Piece.EMPTY && Piece.isWhite(target) == Piece.isWhite(piece))) return false;
        if (!isPseudoLegalMove(position, fromRow, fromColumn, toRow, toColumn)) return false;

        position.setPiece(toRow, toColumn, piece);
        position.setPiece(fromRow, fromColumn, Piece.EMPTY);
        boolean safe = !isKingAttacked(position, Piece.isWhite(piece));
        position.setPiece(fromRow, fromColumn, piece);
        position.setPiece(toRow, toColumn, target);
        return safe;
    }

    public boolean hasLegalMove(Position position, boolean whiteTurn) {
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromColumn = 0; fromColumn < 8; fromColumn++) {
                int piece = position.getPiece(fromRow, fromColumn);
                if (piece == Piece.EMPTY || Piece.isWhite(piece) != whiteTurn) continue;
                for (int toRow = 0; toRow < 8; toRow++) {
                    for (int toColumn = 0; toColumn < 8; toColumn++) {
                        if (isLegalMove(position, fromRow, fromColumn, toRow, toColumn, whiteTurn)) return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isKingAttacked(Position position, boolean whiteKing) {
        int king = Piece.forColor(Piece.KING, whiteKing);
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                if (position.getPiece(row, column) == king) {
                    return isSquareAttacked(position, row, column, !whiteKing);
                }
            }
        }
        return true;
    }

    private boolean isPseudoLegalMove(Position position, int fromRow, int fromColumn,
        int toRow, int toColumn) {
        int piece = position.getPiece(fromRow, fromColumn);
        int target = position.getPiece(toRow, toColumn);
        int rowDelta = toRow - fromRow;
        int columnDelta = toColumn - fromColumn;
        int type = Piece.typeOf(piece);
        if (type == Piece.PAWN) {
            int direction = piece > 0 ? 1 : -1;
            int startRow = piece > 0 ? 1 : 6;
            return (columnDelta == 0 && target == Piece.EMPTY && rowDelta == direction)
                || (columnDelta == 0 && fromRow == startRow && target == Piece.EMPTY
                    && position.getPiece(fromRow + direction, fromColumn) == Piece.EMPTY
                    && rowDelta == 2 * direction)
                || (Math.abs(columnDelta) == 1 && rowDelta == direction && target != Piece.EMPTY);
        }
        if (type == Piece.KNIGHT) return Math.abs(rowDelta) * Math.abs(columnDelta) == 2;
        if (type == Piece.BISHOP) return Math.abs(rowDelta) == Math.abs(columnDelta)
            && clearPath(position, fromRow, fromColumn, toRow, toColumn);
        if (type == Piece.ROOK) return (rowDelta == 0 || columnDelta == 0)
            && clearPath(position, fromRow, fromColumn, toRow, toColumn);
        if (type == Piece.QUEEN) return (rowDelta == 0 || columnDelta == 0
            || Math.abs(rowDelta) == Math.abs(columnDelta))
            && clearPath(position, fromRow, fromColumn, toRow, toColumn);
        return Math.max(Math.abs(rowDelta), Math.abs(columnDelta)) == 1;
    }

    private boolean isSquareAttacked(Position position, int targetRow, int targetColumn, boolean byWhite) {
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                int piece = position.getPiece(row, column);
                if (piece == Piece.EMPTY || Piece.isWhite(piece) != byWhite) continue;
                int rowDelta = targetRow - row;
                int columnDelta = targetColumn - column;
                int type = Piece.typeOf(piece);
                if (type == Piece.PAWN) {
                    int direction = piece > 0 ? 1 : -1;
                    if (rowDelta == direction && Math.abs(columnDelta) == 1) return true;
                } else if (type == Piece.KNIGHT && Math.abs(rowDelta) * Math.abs(columnDelta) == 2) {
                    return true;
                } else if (type == Piece.BISHOP && Math.abs(rowDelta) == Math.abs(columnDelta)
                    && clearPath(position, row, column, targetRow, targetColumn)) {
                    return true;
                } else if (type == Piece.ROOK && (rowDelta == 0 || columnDelta == 0)
                    && clearPath(position, row, column, targetRow, targetColumn)) {
                    return true;
                } else if (type == Piece.QUEEN
                    && (rowDelta == 0 || columnDelta == 0 || Math.abs(rowDelta) == Math.abs(columnDelta))
                    && clearPath(position, row, column, targetRow, targetColumn)) {
                    return true;
                } else if (type == Piece.KING && Math.max(Math.abs(rowDelta), Math.abs(columnDelta)) == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean clearPath(Position position, int fromRow, int fromColumn, int toRow, int toColumn) {
        int rowStep = Integer.compare(toRow, fromRow);
        int columnStep = Integer.compare(toColumn, fromColumn);
        int row = fromRow + rowStep;
        int column = fromColumn + columnStep;
        while (row != toRow || column != toColumn) {
            if (position.getPiece(row, column) != Piece.EMPTY) return false;
            row += rowStep;
            column += columnStep;
        }
        return true;
    }
}
