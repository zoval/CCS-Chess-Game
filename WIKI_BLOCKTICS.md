# Blocktics — Project Wiki

A custom Minecraft-inspired Chess game built with **Java 8+** and the **LibGDX** game development framework.

---

## Table of Contents

1. [Overview](#overview)
2. [Getting Started](#getting-started)
3. [Project Structure](#project-structure)
4. [Architecture](#architecture)
5. [Game Mechanics](#game-mechanics)
6. [Chess Engine & Rules System](#chess-engine--rules-system)
7. [AI & Bot System](#ai--bot-system)
8. [Audio & Sound System](#audio--sound-system)
9. [UI & Rendering System](#ui--rendering-system)
10. [Themes & Board Customization](#themes--board-customization)
11. [Save & Persistence System](#save--persistence-system)
12. [Asset Pipeline](#asset-pipeline)
13. [Design Patterns](#design-patterns)
14. [Testing & Quality Assurance](#testing--quality-assurance)

---

## Overview

**Blocktics** is a standalone 2D chess game combining traditional chess rules with Minecraft-themed visual assets and audio effects. It supports local Player-vs-Player (PvP) matches, Player-vs-AI (PvAI) across multiple difficulty tiers, timed clocks, full move history undo/redo navigation, save/load persistence, and dynamic audio playlists.

| | |
|---|---|
| **Engine / Framework** | LibGDX 1.14.2 (LWJGL3 backend) |
| **Language** | Java (Source / Target Java 8+) |
| **Menu Resolution** | 1024×572 (Fixed) |
| **Game Resolution** | 750×750 (Square Board) |
| **Entry Point (Desktop)** | `io.github.ccs.lwjgl3.Lwjgl3Launcher` |
| **Game Root Class** | `io.github.ccs.MainGame` |
| **Save File** | `blocktics_save.properties` |
| **Build Tool** | Gradle 9.7.1 |

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK):** Version 8, 17, 21, or 25
- **Gradle:** Wrapper included (`gradlew` / `gradlew.bat`)

### Running the Game

Run desktop launcher via Gradle:

```bash
# Windows
.\gradlew.bat lwjgl3:run

# Linux / macOS
./gradlew lwjgl3:run
```

### Running the Test Suite


## Project Structure

```
CCS-Chess-Game/
├── build.gradle                             # Root build configuration
├── settings.gradle                          # Multi-project module declarations (:core, :lwjgl3)
├── gradlew / gradlew.bat                    # Gradle wrapper scripts
│
├── core/                                    # Platform-agnostic game logic and UI
│   ├── build.gradle                         # Core dependencies (LibGDX, gdx-freetype, JUnit)
│   ├── assets/                              # Runtime asset directory
│   │   ├── board/                           # Board textures (new bg.png, new chessboard.png)
│   │   ├── Chesspieces/                     # Piece textures (Waytt [White], Blak [Black])
│   │   ├── Classic/                         # Classic board background and decorations
│   │   ├── fonts/                           # Bitmap & TrueType fonts (PressStart2P-Regular.ttf)
│   │   ├── game_end/                        # Win/lose overlay banners, buttons, icons
│   │   ├── maps/                            # Map selection assets (Classic & Nether themes)
│   │   ├── menu/                            # Menu backgrounds, title artwork, quit dialogs
│   │   ├── opponent/                        # Opponent select buttons and difficulty art
│   │   ├── sounds/                          # SFX & Music (Bubble captures, Ghast screams, OST)
│   │   └── timer/                           # Clock UI graphics and toggle buttons
│   │
│   └── src/
│       ├── main/java/io/github/ccs/
│       │   ├── MainGame.java                # Game singleton: lifecycle, screen routing, config
│       │   │
│       │   ├── ai_networking/               # Chess bots, minimax search, position models
│       │   │   ├── ChessAI.java             # Common interface for AI bots
│       │   │   ├── Difficulty.java          # Enum (EASY, MEDIUM, HARD)
│       │   │   ├── EasyAI.java              # Random legal mover with capture bias (60%)
│       │   │   ├── MediumAI.java            # Depth-2 minimax bot with basic heuristics
│       │   │   ├── HardAI.java              # Alpha-beta search (depth 4 + quiescence + PST)
│       │   │   ├── AIFactory.java           # Factory returning bot instance for difficulty
│       │   │   ├── AIPosition.java          # Lightweight, immutable board snapshot for AI
│       │   │   ├── AIUtils.java             # Board conversion, legal move generator, evaluation
│       │   │   ├── Move.java                # Value object representing a chess move
│       │   │   └── PieceSquareTables.java   # Positional bonus matrices for piece evaluation
│       │   │
│       │   ├── assets/                      # Asset loading helper utilities
│       │   │   └── AssetManagerHelper.java  # Texture caching and resource access
│       │   │
│       │   │
│       │   ├── game_logic/                  # Pure Chess Rules Engine
│       │   │   ├── Board.java               # Central orchestrator: 8x8 grid, turns, captures
│       │   │   ├── Piece.java               # Piece constants (PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING)
│       │   │   ├── Position.java            # 8x8 integer board grid representation
│       │   │   ├── MoveValidator.java       # Pseudo-legal and legal move validation
│       │   │   ├── SpecialMoves.java        # En Passant, Castling rights, Pawn Promotion
│       │   │   ├── FiftymoveRule.java       # 50-move without capture/pawn advance draw rule
│       │   │   ├── RepetitionRule.java      # Threefold repetition detection via FEN history
│       │   │   ├── InsufficientMaterial.java# Automatic draw on insufficient mating material
│       │   │   ├── GameClock.java           # Countdown timer per player with timeout triggers
│       │   │   ├── GameState.java           # Full match state container (board, clock, history)
│       │   │   ├── GameStatus.java          # Win/loss/draw game outcome evaluation
│       │   │   ├── MoveHistory.java         # Undo/redo stack and move snapshot timeline
│       │   │   └── MoveSnapshot.java        # Immutable delta snapshot of a completed move
│       │   │
│       │   ├── sound/                       # Sound Effects & Background Music
│       │   │   ├── SoundCategory.java       # Enum of sound and music asset file mappings
│       │   │   ├── SoundLoader.java         # Asset loader, playlist builder, directory scanner
│       │   │   ├── MusicPlayer.java         # Background music player with playlist streaming
│       │   │   ├── SoundEffectPlayer.java   # Low-latency SFX playback engine
│       │   │   ├── AudioSettings.java       # Volume control, mute toggles, audio preferences
│       │   │   └── SoundManager.java        # Facade singleton unifying SFX and Music controls
│       │   │
│       │   └── ui/                          # UI Screens, Stage2D Actors, Board Rendering
│       │       ├── FirstScreen.java         # Main Menu screen (Play, Settings, Quit)
│       │       ├── OpponentSelectScreen.java# Match setup (PvP vs PvAI, Difficulty selection)
│       │       ├── MapSelectScreen.java     # Theme picker (Classic vs Nether) & Time control
│       │       ├── ChessGameScreen.java     # Primary gameplay screen, HUD, AI driver loop
│       │       ├── BoardRenderer.java       # 8x8 board rendering, square highlights, piece drawing
│       │       ├── BoardInputHandler.java   # Mouse touch/drag and keyboard input processing
│       │       ├── ChessHud.java            # Action buttons (Undo, Redo, Save, Reset, Resign)
│       │       ├── PlayerPanel.java         # Player timer, captured piece display, turn indicators
│       │       ├── GameOverOverlay.java     # Victory / Defeat / Stalemate banner overlay
│       │       ├── SettingsDialog.java      # Music & SFX volume sliders modal dialog
│       │       ├── PromotionDialog.java     # Pawn promotion piece picker modal dialog
│       │       ├── PieceAnimation.java      # Smooth piece interpolation during move animations
│       │       ├── IlluminatedButton.java   # Custom hover-illuminated Stage2D button
│       │       ├── ProceduralTextures.java  # Runtime pixmap generators for highlights & panels
│       │       ├── Fonts.java               # PressStart2P FreeType font generator and cache
│       │       └── BoardTheme.java          # Theme definitions (Classic, Nether)
│       │
│       └── test/java/io/github/ccs/qa/      # Automated unit test suite
│           ├── AISearchTest.java            # Bot search depth & evaluation verification
│           ├── AITest.java                  # AI move selection correctness
│           ├── AudioSettingsTest.java       # Volume & mute logic tests
│           ├── BoardGameOverTest.java       # Checkmate & stalemate detection tests
│           ├── BoardHistoryTest.java        # Move history undo/redo stack tests
│           ├── BoardPromotionTest.java      # Pawn promotion mechanics tests
│           ├── BoardQueryTest.java          # Board legal move & square query tests
│           ├── BoardRepetitionHistoryTest.java # Repetition tracking tests
│           ├── BoardResignDrawTest.java     # Resignation and mutual draw agreement tests
│           ├── CapturedPiecesTest.java      # Captured piece collection tests
│           ├── FiftyMoveRuleTest.java       # 50-move rule counter tests
│           ├── GameClockTest.java           # Timer countdown and flag drop tests
│           ├── InsufficientMaterialTest.java# Insufficient material draw conditions
│           ├── RepetitionRuleTest.java      # Threefold repetition trigger tests
│           └── SaveGameTest.java            # Headless serialization & deserialization tests

## Architecture

### Screen State Machine

Screen navigation is governed by `MainGame` (`com.badlogic.gdx.Game`), switching active `Screen` instances and disposing outgoing screens to prevent memory and texture leaks:

```
                  ┌──────────────────────┐
                  │     FirstScreen      │ (Main Menu)
                  │   Play / Settings    │
                  └──────────┬───────────┘
                             │ (Play Clicked)
                             ▼
                  ┌──────────────────────┐
                  │ OpponentSelectScreen │ (PVP vs PV_AI)
                  │  Difficulty Selection│
                  └──────────┬───────────┘
                             │ (Continue)
                             ▼
                  ┌──────────────────────┐
                  │   MapSelectScreen    │ (Classic / Nether)
                  │ Time Control (5/10/…)│
                  └──────────┬───────────┘
                             │ (Start Game)
                             ▼
                  ┌──────────────────────┐
                  │   ChessGameScreen    │ (Active Match)
                  │ Board + HUD + AI Loop│
                  └──────────┬───────────┘
                             │
            ┌────────────────┴────────────────┐
            ▼                                 ▼
   Settings Dialog (Modal)           GameOverOverlay (Modal)
   Volume & SFX Sliders              Win / Lose / Rematch / Menu
```

All game screens extend `ScreenAdapter` or implement `Screen` and handle:
- `show()` — initializes Stage2D actors, event listeners, and viewport
- `render(delta)` — updates game state, ticks clocks, and renders textures
- `resize(width, height)` — updates viewport dimensions
- `hide()` — pauses screen activities
- `dispose()` — releases Stage, Batch, Textures, and Skin resources

### Class Hierarchy & Core Modules

```
MainGame (io.github.ccs.MainGame)
  ├── GameMode (P_V_P, P_V_AI)
  ├── Difficulty (EASY, MEDIUM, HARD)
  ├── TimeControl (Minutes: 1, 3, 5, 10, unlimited)
  ├── BoardTheme (CLASSIC, NETHER)
  └── pendingLoad: GameState (for resuming saved matches)

Board (io.github.ccs.game_logic.Board)
  ├── Position (8x8 board array)
  ├── MoveValidator & SpecialMoves (en passant, castling, promotion)
  ├── GameStatus (IN_PROGRESS, CHECKMATE, STALEMATE, DRAW_*)
  ├── MoveHistory (Undo/Redo stack of MoveSnapshot)
  ├── GameClock (dual countdown timers)
  ├── FiftymoveRule (50-move counter)
  ├── RepetitionRule (threefold hash tracking)
  └── InsufficientMaterial (automatic draw detection)

ChessAI (io.github.ccs.ai_networking.ChessAI)
  ├── EasyAI (capture-biased random legal mover)
  ├── MediumAI (minimax depth 2 + piece-square tables)
  └── HardAI (alpha-beta depth 4 + quiescence + move ordering)

SoundManager (io.github.ccs.sound.SoundManager)
  ├── AudioSettings (master, music, sfx volume and mute flags)
  ├── SoundLoader (sound assets & dynamic directory scanner)
  ├── MusicPlayer (background music stream & playlist manager)
  └── SoundEffectPlayer (low-latency SFX playback)

## Game Mechanics

### Core Gameplay Loop

```
Main Menu → Match Configuration (PvP / AI, Map, Clock)
                             ↓
              Match Start (Live Game Screen)
                             ↓
           Player Move (Input → Validation → Sound)
                             ↓
         Turn Switch (Clock Switch → Special Moves Check)
                             ↓
     [If PvAI] AI Worker Thread Search → Dispatch AI Move
                             ↓
      Terminal Condition Check (Checkmate / Stalemate / Clock Flag)
                             ↓
      GameOver Overlay (Play Again / Opponent Select / Main Menu)
```

### Time Control System

- Configurable match times: **1 min (Bullet)**, **3 min (Blitz)**, **5 min (Rapid)**, **10 min (Standard)**, or **Unlimited (No Timer)**.
- `GameClock` tracks elapsed time per player down to millisecond precision.
- Running out of time immediately triggers a clock flag drop, awarding victory to the opposing side (unless the opponent has insufficient mating material, resulting in a draw).

### Move History & Timeline Navigation

- Every move is recorded as an immutable `MoveSnapshot`.
- Players can step backward and forward through past positions via HUD navigation arrows.
- While viewing history:
  - The board enters read-only mode (`isViewingHistory() == true`).
  - Active timers pause.
  - Making a new move from the live head position clears the redo stack.

---

## Chess Engine & Rules System

### Piece Representation

Pieces are encoded as integers composed of a piece type and color bitmask:

| Constant | Value | Description |
|---|---|---|
| `Piece.EMPTY` | `0` | Empty square |
| `Piece.PAWN` | `1` | Pawn |
| `Piece.KNIGHT` | `2` | Knight |
| `Piece.BISHOP` | `3` | Bishop |
| `Piece.ROOK` | `4` | Rook |
| `Piece.QUEEN` | `5` | Queen |
| `Piece.KING` | `6` | King |
| `Piece.WHITE` | `0x08` | White piece bitmask |
| `Piece.BLACK` | `0x10` | Black piece bitmask |

### Move Validation

- `MoveValidator` computes pseudo-legal moves for each piece type.
- King safety is verified by simulating the move on a cloned `Position` and ensuring the friendly king is not under attack from enemy pieces.
- Pins, ray obstacles, knight jumps, and pawn push/capture rules are fully enforced.

### Special Moves

| Move | Implementation Details |
|---|---|
| **Castling** | Supports Kingside (O-O) and Queenside (O-O-O). Verifies king and rook have not moved, intervening squares are empty, and the king does not start in, pass through, or land in check. |
| **En Passant** | Allowed immediately after an enemy pawn moves 2 squares forward. Tracked via `SpecialMoves.getEnPassantColumn()`, resets on the subsequent turn. |
| **Pawn Promotion** | Triggered when a pawn reaches rank 8 (White) or rank 1 (Black). Displays `PromotionDialog` modal to select Queen, Rook, Bishop, or Knight. AI bots automatically promote to Queen. |

### Draw Conditions

- **Stalemate:** Current player has no legal moves and is not in check.
- **Threefold Repetition:** The exact same board position occurs 3 times (`RepetitionRule`).
- **50-Move Rule:** 50 consecutive full moves without a capture or pawn push (`FiftymoveRule`).
- **Insufficient Material:** Neither side has mating potential (`InsufficientMaterial`):
  - King vs King
  - King and Bishop vs King
  - King and Knight vs King
  - King and Bishop vs King and Bishop (same square color)
- **Mutual Draw Agreement:** Offered and accepted via the HUD draw button.

---

```

---


## AI & Bot System

### Difficulty Levels

| Difficulty | Class | Algorithm | Search Depth | Features |
|---|---|---|---|---|
| **Easy** | `EasyAI` | Random Legal Mover | 0 | 60% bias towards captures, otherwise uniform random move |
| **Medium** | `MediumAI` | Minimax | 2 | Positional Piece-Square Tables + Material Evaluation |
| **Hard** | `HardAI` | Alpha-Beta Pruning | 4 | Quiescence search on captures, Move ordering (MVV-LVA), Piece-Square Tables |

### Positional Evaluation (Piece-Square Tables)

`PieceSquareTables.java` defines 8×8 positional weight matrices for each piece type:
- **Pawns:** Rewarded for center control and advancement toward promotion ranks.
- **Knights:** Rewarded for outpost centralization; penalized for rim placement.
- **Bishops:** Rewarded for open diagonals and controlling long lines.
- **Rooks:** Rewarded for 7th-rank infiltration and open files.
- **Queens:** Rewarded for central mobility; penalized for premature opening development.
- **Kings:** Rewarded for castled corner safety in opening/middlegame; encouraged to centralize during endgame.

### Threading & Non-Blocking AI Execution

To keep rendering and UI interaction smooth at 60 FPS:
1. `ChessGameScreen.updateAiDriver(float delta)` detects when it is Black's turn.
2. A natural human-like delay (`AI_MOVE_DELAY = 0.35s`) counts down.
3. The AI search runs asynchronously on a separate background worker thread.
4. The resulting `Move` is dispatched onto LibGDX's OpenGL render thread via `Gdx.app.postRunnable()` to execute board changes, trigger piece animations, and play sound effects.

---

## Audio & Sound System

### Architecture

```
SoundManager (Singleton Facade)
  ├── AudioSettings (Volume levels, Mute toggles)
  ├── SoundLoader (Dynamic folder scanning + fallback playlists)
  ├── MusicPlayer (Playlist streaming & auto-advancement)
  └── SoundEffectPlayer (Low-latency SFX triggers)
```

### Dynamic Playlist Streaming

`SoundLoader.buildPlaylist(prefix, fallback)` dynamically scans the `sounds/` internal asset directory at runtime:
- **Menu Music Playlist:** Shuffles all audio files starting with `Menu` (e.g. `Menu page music.mp3`, `MenupageLabyrinthine.mp3`).
- **In-Game Music Playlist:** Shuffles all audio files starting with `In-game` (e.g. `In-game music.mp3`, `In-gameBelow and Above.mp3`, `In-gameBroken Clocks.mp3`, etc.).
- `MusicPlayer` loads and streams one track at a time to minimize memory consumption, automatically advancing and shuffling when the current track finishes.

### Sound Effects Mapping

| Event | Asset File | Description |
|---|---|---|
| **Piece Move** | `sounds/moving(wood).ogg` | Wood click sound on normal piece drop |
| **Piece Capture** | `sounds/capture(bubble).ogg` | Bubble pop sound on capturing an opponent piece |
| **Checkmate** | `sounds/checkmate(xp leveling up).mp3` | Minecraft XP level-up chime |
| **Stalemate / Draw** | `sounds/drawORstalemate1(anvil fall).mp3` | Minecraft anvil fall sound |
| **Game Loss** | `sounds/losingSoundfx(ghast).mp3` | Ghast scream on defeat |
| **Game Start** | `sounds/startgame(orb).mp3` | Experience orb chime when match starts |
| **UI Click** | `sounds/UISelect.mp3` | Button selection feedback sound |

---

│
└── lwjgl3/                                  # Desktop launcher target
    ├── build.gradle                         # LWJGL3 backend packaging & natives
    └── src/main/java/io/github/ccs/lwjgl3/
        ├── Lwjgl3Launcher.java              # Desktop application bootstrap & window configuration
        └── StartupHelper.java               # macOS JVM workaround helper
```

---


## UI & Rendering System

### Coordinate Spaces & Viewports

- **Menu Screens:** Fixed 1024×572 resolution matching the UI background texture (`menu/BG.jpg`).
- **Game Screen:** 750×750 square board with `FitViewport` ensuring consistent aspect ratios across varying display resolutions.

### Component System

| Component | Class | Responsibility |
|---|---|---|
| **Board Renderer** | `BoardRenderer` | Draws chessboard tiles, coordinate labels, valid move dots, checkmate glow, and piece sprites. |
| **Input Processor** | `BoardInputHandler` | Translates touch/drag screen coordinates to board square `(row, col)` indices. |
| **HUD Controller** | `ChessHud` | Action buttons: Undo, Redo, Reset, Resign, Draw, Save, and Settings. |
| **Player Panel** | `PlayerPanel` | Renders player names, active turn indicators, remaining time clocks, and captured piece trays. |
| **Game Over Overlay** | `GameOverOverlay` | Modal overlay displaying match outcome banners (Win/Lose/Draw) and action buttons (Play Again, Main Menu). |
| **Illuminated Button** | `IlluminatedButton` | Custom Stage2D actor with smooth brightness interpolation on hover. |
| **Piece Animator** | `PieceAnimation` | Linear interpolation (`alpha`) for smooth visual transitions during piece moves. |
| **Procedural Textures** | `ProceduralTextures` | Generates highlight overlays, rounded rect panels, and circle dots at runtime without external image files. |

---

## Themes & Board Customization

The game supports interchangeable visual themes selectable in `MapSelectScreen`:

| Theme | Board Asset | Background Asset | Description |
|---|---|---|---|
| **Classic** | `board/new chessboard.png` | `Classic/classic bg in frame.jpeg` | Traditional warm wooden chessboard and framed parlor backdrop. |
| **Nether** | `board/new chessboard.png` | `maps/nether_landscape.jpeg` | Minecraft Nether theme with dark crimson textures and eerie atmospheric tones. |

---

│       │   ├── backend/                     # Persistence and save/load logic
│       │   │   └── SaveGame.java            # Headless .properties serializer and LibGDX I/O

```bash
# Run unit and QA regression tests
.\gradlew.bat test
```

### Building Distribution JAR


## Save & Persistence System

### File Format: `blocktics_save.properties`

Match state is persisted using Java's standard `Properties` format for readability, crash resilience, and cross-platform compatibility.

### Property Keys

```properties
version=1
mode=P_V_AI
difficulty=MEDIUM
map=CLASSIC
timeControlMinutes=10
whiteMillis=542100
blackMillis=589400
historyIndex=4
snapshot.count=5
snapshot.0.fromRow=1
snapshot.0.fromCol=4
snapshot.0.toRow=3
snapshot.0.toCol=4
snapshot.0.movedPiece=9
snapshot.0.capturedPiece=0
...
```

### Safety Features

- **Headless Serializer:** `SaveGame.parse()` and `SaveGame.format()` are pure Java methods free of LibGDX dependencies, allowing full automated unit testing outside an active OpenGL context.
- **Version Guarding:** Loads with an incompatible `version` string fail gracefully instead of corrupting runtime state.
- **Corrupt File Resilience:** Returns `null` on missing or damaged files, allowing the UI to notify the user cleanly.

---

## Asset Pipeline

### Directory Structure (`core/assets/`)

```
core/assets/
├── board/         # Chessboard square tiles and borders
├── Chesspieces/   # Piece sprites categorized by side (Waytt / Blak)
├── fonts/         # PressStart2P TrueType pixel font and license
├── game_end/      # Win/lose dialogue artwork and buttons
├── maps/          # Map preview thumbnails and landscape art
├── menu/          # Title screens, button states, background art
├── opponent/      # AI vs PvP mode selector icons
├── sounds/        # OGG sound effects and MP3 soundtrack tracks
└── timer/         # Digital clock frame textures and controls
```

### Dynamic Font Generation

`Fonts.java` utilizes LibGDX `FreeTypeFontGenerator` to generate pixel-crisp `BitmapFont` instances dynamically at runtime from `PressStart2P-Regular.ttf` across multiple size breakpoints (`10pt`, `12pt`, `14pt`, `16pt`, `20pt`), preventing blurry font scaling.

---

## Design Patterns

| Pattern | Location | Usage |
|---|---|---|
| **State Machine** | `MainGame`, `ScreenAdapter` | Screen transitions between Main Menu, Setup, and Active Match |
| **Factory Pattern** | `AIFactory` | Instantiates `EasyAI`, `MediumAI`, or `HardAI` based on selected `Difficulty` |
| **Facade Pattern** | `SoundManager` | Unifies `SoundLoader`, `MusicPlayer`, `SoundEffectPlayer`, and `AudioSettings` |
| **Observer Pattern** | `GameClock`, LibGDX Scene2D | Event-driven UI listeners and clock tick callbacks |
| **Strategy Pattern** | `ChessAI` | Interchangeable move selection algorithms for computer opponents |
| **Memento / Snapshot** | `MoveSnapshot`, `MoveHistory` | Immutable capture of board state deltas for undo/redo and persistence |
| **Procedural Texture Generation** | `ProceduralTextures` | Runtime dynamic creation of highlight pixmaps and color overlays |

---

## Testing & Quality Assurance

The codebase includes an automated JUnit 4 test suite located under `core/src/test/java/io/github/ccs/qa/`:

| Test Class | Focus Area |
|---|---|
| `AITest.java` | AI bot instantiation and move generation validity |
| `AISearchTest.java` | Minimax / Alpha-Beta depth correctness and search consistency |
| `BoardGameOverTest.java` | Checkmate, stalemate, and king safety verification |
| `BoardHistoryTest.java` | Move recording, undo, redo, and timeline truncation |
| `BoardPromotionTest.java` | Pawn promotion to Queen, Rook, Bishop, and Knight |
| `BoardRepetitionHistoryTest.java` | Threefold repetition detection across repetitive move cycles |
| `BoardResignDrawTest.java` | Player resignation and mutual draw agreement logic |
| `CapturedPiecesTest.java` | Captured piece tracking for White and Black trays |
| `FiftyMoveRuleTest.java` | 50-move rule counter increments and reset conditions |
| `GameClockTest.java` | Clock countdown accuracy, pause states, and flag drop triggers |
| `InsufficientMaterialTest.java` | Verification of King-only, King+Bishop, and King+Knight draw states |
| `RepetitionRuleTest.java` | Hash recording and repetition limit thresholds |
| `SaveGameTest.java` | Serialization, parsing, round-trip fidelity, and corruption safety |
| `AudioSettingsTest.java` | Volume level bounds clamping and mute toggle logic |

---

*This wiki provides technical documentation for the Blocktics Chess codebase. Last updated: 2026-10-05.*

```bash
.\gradlew.bat lwjgl3:jar
```

---
