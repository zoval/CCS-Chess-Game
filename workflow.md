# Workflow

This file explains where each team should work and what to check before submitting changes.

## Prerequisites

- Install a JDK that works with this project.
- Use an IDE that understands Gradle, such as IntelliJ IDEA or VS Code with Java extensions.
- Open the whole project folder, not only `core` or `lwjgl3`.
- Use the included Gradle wrapper:
  - Windows: `gradlew.bat build`
  - macOS/Linux: `./gradlew build`
- Run the game from the desktop launcher:
  - Windows: `gradlew.bat lwjgl3:run`
  - macOS/Linux: `./gradlew lwjgl3:run`
- Run a build before submitting changes.
- Keep shared game code in `core/src/main/java/io/github/ccs`.
- Keep automated tests in `core/src/test/java/io/github/ccs/qa`.
- Keep desktop-only startup settings in `lwjgl3`.
- Put shipped game assets in `assets`.

## Project Layout

- `core`: The main game module. Most teams work here.
- `lwjgl3`: Desktop launcher code for PC/Mac/Linux. This starts the game but should not contain normal gameplay code.
- `assets`: Images, sounds, music, fonts, and other files loaded by the game at runtime.
- `gradle`, `gradlew`, `gradlew.bat`: Gradle wrapper files. Do not edit these unless the team agrees to upgrade Gradle.
- `build` folders: Generated output. Do not edit these files manually.

## General Rules

- Ask: "Does this code need to run on every platform?" If yes, it belongs in `core`.
- Ask: "Is this only about the desktop window or launcher?" If yes, it belongs in `lwjgl3`.
- Do not put game rules inside UI screens.
- Do not put drawing code inside game logic classes.
- Do not put networking calls directly inside chess rule classes.
- Keep classes small enough that their purpose is obvious.
- Use clear names such as `BoardState`, `MoveValidator`, `MainMenuScreen`, or `SoundManager`.
- Prefer adding a new class in the correct team folder over making one file do everything.
- Do not edit generated files inside `build`.

## Team Responsibilities

### UI Team

Folder: `core/src/main/java/io/github/ccs/ui`

Owns everything the player sees or directly interacts with.

Examples:

- Screens, menus, HUDs, buttons, overlays, and board display.
- Reading clicks, taps, keyboard input, and mouse movement.
- Showing whose turn it is.
- Highlighting selected pieces or legal moves.
- Displaying game over, pause, settings, and main menu screens.

Hidden nuances:

- UI should ask game logic what is allowed; it should not decide chess rules itself.
- UI can display board state, but `game_logic` should own the real board state.
- Avoid hardcoding important gameplay decisions in a screen class.
- LibGDX `Screen` classes have lifecycle methods like `show`, `render`, `resize`, and `dispose`; use `dispose` for cleanup when assets or resources are owned by that screen.

### Game Logic Team

Folder: `core/src/main/java/io/github/ccs/game_logic`

Owns the actual chess game rules and gameplay state.

Examples:

- Board representation.
- Piece movement rules.
- Turn order.
- Legal move validation.
- Check, checkmate, stalemate, draw rules, and win/loss state.
- Move history, undo rules, and captured pieces.

Hidden nuances:

- This code should work even if there is no UI.
- This code should not import LibGDX rendering classes unless truly necessary.
- This code should not know whether a player clicked a mouse or received a network message.
- AI and networking should send requested moves into game logic, then game logic decides if the move is legal.
- Good game logic is easy for QA to test because it does not depend on graphics.

### Backend Team

Folder: `core/src/main/java/io/github/ccs/backend`

Owns support systems that store, load, or manage data.

Examples:

- Save/load files.
- Player profiles.
- Settings storage.
- Match history.
- Local configuration.
- Data models that are not specifically UI or chess rules.

Hidden nuances:

- Backend code should not draw anything.
- Backend code should avoid depending on one specific screen.
- Keep file paths and save formats documented in code or README notes.
- If saving game state, coordinate with game logic so the saved data matches the real rules.
- If a change affects files on disk, QA should test loading older or missing data when possible.

### AI / Networking Team

Folder: `core/src/main/java/io/github/ccs/ai_networking`

Owns computer-player decisions and communication between players or services.

Examples:

- Chess AI move selection.
- Difficulty settings for AI.
- Multiplayer session state.
- Sending and receiving moves.
- Matchmaking hooks.
- Network request/response code.
- Reconnection or sync behavior.

Hidden nuances:

- AI should choose a move, but game logic should validate that move.
- Networking should transport moves, not redefine chess rules.
- Network code can fail; plan for disconnects, timeouts, duplicate messages, and out-of-order messages.
- Keep protocol/data formats simple and documented.
- Do not let remote input directly change UI; route it through game state first.

### QA Team

Folder: `core/src/test/java/io/github/ccs/qa`

Owns tests, verification, and bug reproduction.

Examples:

- Unit tests for chess rules.
- Regression tests for fixed bugs.
- Test fixtures and sample board states.
- Manual test checklists.
- Release verification notes.

Hidden nuances:

- Test game logic before testing UI whenever possible.
- A good bug report includes expected result, actual result, and exact reproduction steps.
- Add tests for edge cases, not only normal moves.
- Useful chess edge cases include checkmate, stalemate, castling, en passant, promotion, pinned pieces, and invalid moves.
- If a bug is fixed, add a regression test so it does not come back.

### Sound Design Team

Folder: `core/src/main/java/io/github/ccs/sound`

Owns music, sound effects, and audio behavior.

Examples:

- Move sounds.
- Capture sounds.
- Check/checkmate sounds.
- Menu music.
- Volume controls.
- Muting and audio settings.
- Sound playback helpers.

Hidden nuances:

- Audio files that ship with the game should go in `assets`.
- Code that decides when to play a sound can live in `sound`, but gameplay rules still belong in `game_logic`.
- Avoid loading the same sound file repeatedly during gameplay.
- Clean up audio resources when they are no longer needed.
- Coordinate with UI for button/menu sounds and with game logic for move/check/checkmate events.

## Common Change Examples

- New menu screen: UI team, `ui`.
- New legal move rule: Game logic team, `game_logic`.
- Save game feature: Backend team, `backend`, with help from game logic.
- Computer opponent: AI / Networking team, `ai_networking`, with validation from game logic.
- Multiplayer move sync: AI / Networking team, `ai_networking`, with QA edge-case tests.
- Move sound effect: Sound design team, `sound`, with the audio file in `assets`.
- Bug test for checkmate: QA team, `core/src/test/java/io/github/ccs/qa`.
- Window title or app icon: `lwjgl3`.

## Before Submitting

- Run `gradlew.bat build` or `./gradlew build`.
- Run the game if your change affects player experience.
- Check that your files are in the correct team folder.
- Check that no generated `build` files were edited manually.
- Add or update tests when changing game logic, backend data, AI behavior, or networking.
- Tell the team what changed, how you tested it, and what still needs review.
