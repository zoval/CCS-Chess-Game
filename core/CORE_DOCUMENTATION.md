# Core Module Technical Documentation

This document provides a comprehensive, traceable breakdown of the `core` game engine architecture, state mutation pipelines, AI decision algorithms, and LibGDX render loops.

---

## 1. System Architecture & Component Topology

The `core` module is fully cross-platform and contains all rules, UI abstractions, AI logic, and rendering code. It is completely decoupled from the desktop launcher (`lwjgl3`).

```
                              +--------------------+
                              |  MainGame (Game)   |
                              +---------+----------+
                                        |
                 +----------------------+----------------------+
                 |                                             |
     +-----------v-----------+                     +-----------v-----------+
     |   FirstScreen (Menu)  |                     |  ChessGameScreen (UI) |
     +-----------------------+                     +-----------+-----------+
                                                               |
                       +---------------------------------------+---------------------------------------+
                       |                                       |                                       |
           +-----------v-----------+               +-----------v-----------+               +-----------v-----------+
           |  BoardRenderer (View) |               |  BoardInputHandler    |               |  AI Controller        |
           +-----------+-----------+               +-----------+-----------+               +-----------+-----------+
                       |                                       |                                       |
                       +-------------------> + <---------------+---------------------------------------+
                                             |
                                  +----------v----------+
                                  |     Board (State)   |
                                  +----------+----------+
                                             |
             +-------------------------------+-------------------------------+
             |                               |                               |
 +-----------v-----------+       +-----------v-----------+       +-----------v-----------+
 |       Position        |       |     MoveValidator     |       |       GameStatus      |
 +-----------------------+       +-----------+-----------+       +-----------------------+
                                             |
                                 +-----------v-----------+
                                 |      SpecialMoves     |
                                 +-----------------------+
```

### Directory Structure
- `io.github.ccs.game_logic`: Pure chess rules, board grid representation, move validation, special rules (en passant, castling, promotion, 50-move rule).
- `io.github.ccs.ai_networking`: Computer players (Easy, Medium, Hard Minimax), board snapshots (`AIPosition`), move evaluation heuristics.
- `io.github.ccs.ui`: LibGDX `Screen` implementations, rendering passes (`BoardRenderer`), touch/keyboard handlers (`BoardInputHandler`), and animations.
- `io.github.ccs.sound`: Audio management, BGM player (`MusicPlayer`), SFX router (`SoundEffectPlayer`).
- `io.github.ccs.assets`: Centralized asset loading helpers (`AssetManagerHelper`).

---

## 2. Data Representation & Bitwise Piece Model

File: `core/src/main/java/io/github/ccs/game_logic/Piece.java`

Pieces are stored as compact 32-bit integers using bit flags:

| Constant | Value (Decimal) | Value (Binary) | Description |
|---|---|---|---|
| `EMPTY` | `0` | `000000` | Empty square |
| `PAWN` | `1` | `000001` | Pawn piece type |
| `KNIGHT` | `2` | `000010` | Knight piece type |
| `BISHOP` | `3` | `000011` | Bishop piece type |
| `ROOK` | `4` | `000100` | Rook piece type |
| `QUEEN` | `5` | `000101` | Queen piece type |
| `KING` | `6` | `000110` | King piece type |
| `WHITE` | `16` | `010000` | Color flag: White |
| `BLACK` | `32` | `100000` | Color flag: Black |

### Bitwise Utility Methods:
- `Piece.typeOf(int piece)`: `piece & 7` extracts piece type (`PAWN..KING`).
- `Piece.isWhite(int piece)`: `(piece & 16) != 0` evaluates true for White pieces.
- `Piece.isBlack(int piece)`: `(piece & 32) != 0` evaluates true for Black pieces.
- `Piece.of(int type, boolean white)`: `type | (white ? 16 : 32)` constructs piece ID.


---

## 3. Move Execution & State Mutation Pipeline

File: `core/src/main/java/io/github/ccs/game_logic/Board.java:43`

When a player or AI plays a move, `Board.move(fromRow, fromColumn, toRow, toColumn)` orchestrates state transformation:

```
[Touch / AI Move]
        |
        v
Board.move(fromRow, fromColumn, toRow, toColumn)
        |
        +---> 1. Check Game Over Guard (gameStatus.isGameOver())
        |
        +---> 2. Legal Move Validation (MoveValidator.isLegalMove())
        |
        +---> 3. Special Move Detection (SpecialMoves: Castling / En Passant)
        |
        +---> 4. Piece Relocation (Position.setPiece() on origin and target)
        |
        +---> 5. En Passant Pawn Removal (if applicable)
        |
        +---> 6. Castling Rook Displacement (if applicable)
        |
        +---> 7. Pawn Promotion Check (promotePawn() to Queen on rank 0/7)
        |
        +---> 8. Fifty-Move Clock Update (FiftymoveRule.recordMove())
        |
        +---> 9. Move History & En Passant Target Tracking (SpecialMoves.recordMove())
        |
        +---> 10. Turn Inversion (whiteTurn = !whiteTurn)
        |
        +---> 11. Status & Checkmate Evaluation (GameStatus.update())
```

### Trace Details:
1. **Validation**: `MoveValidator.isLegalMove()` runs full geometric and King-safety validation.
2. **Board Array Update**: `position.setPiece(fromRow, fromColumn, Piece.EMPTY)` and `position.setPiece(toRow, toColumn, piece)` mutate the `int[8][8]` grid in `Position.java:27`.
3. **En Passant Capture**: Clears captured pawn on row `toRow + (whiteTurn ? -1 : 1)`.
4. **Castling Rook Transfer**:
   - Kingside (`toColumn == 6`): Moves Rook from column `7` to `5`.
   - Queenside (`toColumn == 2`): Moves Rook from column `0` to `3`.
5. **Promotion**: Automatically transforms pawn hitting back rank into Queen (`SpecialMoves.promotedPiece(piece, Piece.QUEEN)`).
6. **Turn Advance**: `whiteTurn = !whiteTurn` toggles active player.
7. **Status Update**: `GameStatus.update(position, moveValidator, whiteTurn, fiftyMoveRule.isDraw())` recalculates if opponent has legal moves or is in check.

---

## 4. Move Validation & King Safety Engine

File: `core/src/main/java/io/github/ccs/game_logic/MoveValidator.java`

Move validation follows a two-tier verification model:

```
isLegalMove(position, fromR, fromC, toR, toC, whiteTurn)
  │
  ├── Tier 1: Bounds & Ownership Check (lines 20-30)
  │     ├── Target square contains King? -> REJECT
  │     ├── Target piece same color? -> REJECT
  │     └── Origin piece not current turn? -> REJECT
  │
  ├── Tier 2: Pattern & Obstruction Check (lines 32-51)
  │     ├── Castling / En Passant valid? -> PASS
  │     └── isPseudoLegalMove() -> validates piece movement geometry & clear ray paths
  │
  └── Tier 3: King Safety / Check Simulation (lines 53-62)
        ├── Clone Position to sandbox
        ├── Apply move in sandbox
        └── isInCheck(tempPosition, whiteTurn)
              ├── King square attacked by Bishop/Rook/Queen rays?
              ├── King square attacked by Knight offsets?
              └── King square attacked by Pawn diagonals?
```

### Piece Movement Mechanics (`MoveValidator.java:70-136`):
- **Pawn**: Forward 1 step if empty; forward 2 steps from starting rank (1 for White, 6 for Black) if path clear; diagonal 1 step if capturing.
- **Knight**: L-shape offsets `(|dr| == 1 && |dc| == 2) || (|dr| == 2 && |dc| == 1)`. No path clearance required (jumps pieces).
- **Bishop**: Diagonal delta `|dr| == |dc|`. Verifies all intermediate squares are `Piece.EMPTY` (`isPathClear`).
- **Rook**: Orthogonal delta `dr == 0 || dc == 0`. Verifies intermediate squares are clear (`isPathClear`).
- **Queen**: Combines Bishop diagonal and Rook orthogonal validation.
- **King**: Chebyshev distance `max(|dr|, |dc|) == 1`.


---

## 5. AI Decision Pipelines

Package: `io.github.ccs.ai_networking`

### 1. Board Snapshot (`AIPosition.java` & `AIUtils.java`)
AI operates on a lightweight snapshot `AIPosition` extracted from `Board` via `AIUtils.read(board)`.

### 2. Difficulty Implementations

#### Easy AI (`EasyAI.java:13-57`)
- Obtains legal moves via `AIUtils.legalMoves(position, white)`.
- If captures exist: **60% probability** of selecting a random capture move.
- Otherwise: selects uniformly random legal move.

#### Medium AI (`MediumAI.java:11-75`)
- **1-Ply Heuristic Search**: Evaluates each legal move by simulating it and calculating the positional board score:
  `Score = MaterialScore + PositionalPieceSquareScore`
- Piece Material Values:
  - `PAWN`: 100
  - `KNIGHT`: 320
  - `BISHOP`: 330
  - `ROOK`: 500
  - `QUEEN`: 900
  - `KING`: 20000
- Positional Tables (`AIUtils.java:175-235`): Piece-square matrices rewarding center control, pawn advancement, and piece development.

#### Hard AI (`HardAI.java:12-108`)
- **2-Ply Minimax with Alpha-Beta Pruning**:
  ```
  search(position, turn, depth, alpha, beta, maximizingColor):
    if depth == 0 or game over:
      return evaluate(position, maximizingColor)
    
    order moves using MVV-LVA (Most Valuable Victim - Least Valuable Attacker)
    for each move in orderedMoves:
      apply move to sandbox AIPosition
      score = -search(sandbox, !turn, depth - 1, -beta, -alpha, maximizingColor)
      if score >= beta:
        return beta (Beta Cutoff)
      if score > alpha:
        alpha = score
    return alpha
  ```
- **Move Ordering**: `AIUtils.orderedMoves()` sorts moves by capture value difference before searching, maximizing early alpha-beta branch pruning.

---

## 6. LibGDX Screen & Render Loop

Files:
- `core/src/main/java/io/github/ccs/ui/ChessGameScreen.java`
- `core/src/main/java/io/github/ccs/ui/BoardRenderer.java`
- `core/src/main/java/io/github/ccs/ui/BoardInputHandler.java`

### Frame Render Cycle (`ChessGameScreen.render(float delta)`):
```
1. ScreenUtils.clear(0, 0, 0, 1)  [Clear Frame Buffer]
2. AI Turn Check                  [Trigger background AI thread / makeMove if AI's turn]
3. PieceAnimation.update(delta)   [Advance movement interpolation timers]
4. Batch.begin()
     ├── BoardRenderer.render()
     │     ├── Draw Board Background Texture (based on BoardTheme)
     │     ├── Draw Tile Overlays (Selected square gold, Legal target dots)
     │     ├── Draw Static Pieces (board.getPiece(r, c) -> TextureRegion)
     │     └── Draw Animated Piece (interpolated X, Y from PieceAnimation)
     └── Batch.end()
5. UI Stage.act() & Stage.draw()  [Draw Game Over overlay, Turn text, Buttons]
```

### Coordinate & Input Mapping (`BoardInputHandler.java:61-100`):
Input coordinates from LibGDX (`screenX`, `screenY` with `(0,0)` at top-left) are transformed into board indices:
1. Invert Y-axis: `pixelY = Gdx.graphics.getHeight() - screenY - boardY`.
2. Normalize to board bounds: `gridX = (screenX - boardX) / boardSize`.
3. Compute square indices:
   - `column = floor((gridX - gridOffset) / squareW)`
   - `row = floor((gridY - gridOffset) / squareH)`
4. Clicks validate within `[0..7, 0..7]`.


---

## 7. Audio & Asset Management

- **Asset Loading** (`io.github.ccs.assets.AssetManagerHelper`): Preloads textures for pieces (Classic, Minecraft, Neo themes), boards, and UI skins using LibGDX `AssetManager`.
- **Sound Routing** (`io.github.ccs.sound.SoundManager`): Singleton interface delegating to:
  - `SoundEffectPlayer`: Plays move, capture, check, and game-over sound effects.
  - `MusicPlayer`: Handles looping background music tracks and volume preferences.

---

## 8. Code Traceability Matrix

| Flow / Feature | Primary Entry Point | Collaborating Classes | Line Reference |
|---|---|---|---|
| Application Startup | `MainGame.java:23` | `FirstScreen.java` | `MainGame.java:23-26` |
| Move Validation | `MoveValidator.java:17` | `SpecialMoves.java`, `Position.java` | `MoveValidator.java:17-62` |
| Move Execution | `Board.java:43` | `Position.java`, `SpecialMoves.java`, `FiftymoveRule.java`, `GameStatus.java` | `Board.java:43-79` |
| King In-Check Scan | `MoveValidator.java:140` | `Position.java`, `Piece.java` | `MoveValidator.java:140-200` |
| En Passant Logic | `SpecialMoves.java:54` | `Position.java`, `Piece.java` | `SpecialMoves.java:54-68` |
| Castling Logic | `SpecialMoves.java:31` | `Position.java`, `MoveValidator.java` | `SpecialMoves.java:31-52` |
| Pawn Promotion | `Board.java:89` | `SpecialMoves.java` | `Board.java:89-93` |
| Easy AI | `EasyAI.java:13` | `AIUtils.java`, `AIPosition.java` | `EasyAI.java:13-57` |
| Medium AI | `MediumAI.java:11` | `AIUtils.java`, `AIPosition.java` | `MediumAI.java:11-75` |
| Hard AI (Minimax) | `HardAI.java:12` | `AIUtils.java`, `AIPosition.java` | `HardAI.java:12-108` |
| Board Touch Input | `BoardInputHandler.java:61` | `Board.java`, `ChessGameScreen.java` | `BoardInputHandler.java:61-120` |
| Board Frame Render | `BoardRenderer.java:55` | `AssetManagerHelper.java`, `PieceAnimation.java` | `BoardRenderer.java:55-250` |

