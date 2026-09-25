# Core Module Documentation

If you are reading this, you are working on the game engine part of this project. If you break this, the game stops working. If you don't understand it, read carefully.

## Overview
The `core` directory contains the cross-platform game logic and UI using LibGDX. It does *not* know about the desktop (`lwjgl3`) or Android specifically, mostly.

## Packages Breakdown

### 1. `game_logic` (The Brains)
This is where the actual chess rules live. If a move is illegal, it's checked here.
*   **Board.java**: Represents the 8x8 grid. Holds pieces.
*   **Piece.java**: Represents a single chess piece (king, queen, etc.).
*   **MoveValidator.java**: The gatekeeper. Determines if a move is allowed according to chess rules.
*   **SpecialMoves.java**: Handles castling, en passant, promotion.
*   **GameStatus.java**: Keeps track of check, checkmate, stalemate.

### 2. `ui` (The Face)
How the user interacts with the game.
*   **ChessGameScreen.java**: The main game screen where the board is drawn.
*   **BoardRenderer.java**: Draws the board and pieces on the screen.
*   **BoardInputHandler.java**: Translates mouse clicks/taps into game moves.
*   **PieceAnimation.java**: Makes pieces move smoothly instead of teleporting.

### 3. `ai_networking` (The "Magic")
*   Handles AI logic (moves a computer player makes) and network communication (if playing against an opponent over internet).

### 4. `backend`
*   Likely handles saving/loading game states or high scores, if that's implemented. Don't touch unless you know what you are doing.

### 5. `sound`
*   Audio handling. If the game is silent, check here.

### 6. `assets`
*   Data and resources loading.

---
**Advice:** If you add a new piece type, update `Piece.java` and `MoveValidator.java`. If you change the UI look, look at `BoardRenderer.java`. If you are confused, *read the code*. It's not magic.
