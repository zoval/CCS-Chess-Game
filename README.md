# CCS Chess

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and a main class extending `Game` that sets the first screen.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Team Folders

Most day-to-day game work should happen inside `core/src/main/java/io/github/ccs`.

- `ui`: Screens, HUDs, menus, visual presentation, and input-facing UI code.
- `game_logic`: Chess rules, board state, turn flow, move validation, and gameplay systems.
- `backend`: Persistence, settings, save/load workflows, and local service-style support code.
- `ai_networking`: Computer-player behavior — Easy (random with capture bias), Medium (depth-2 minimax), and Hard (alpha-beta minimax) bots built on shared move generation and evaluation helpers. Multiplayer/network sessions are planned for this package but not implemented yet.
- `sound`: Music, sound effects, audio triggers, mixer settings, and playback integration.
- `core/src/test/java/io/github/ccs/qa`: Automated tests, fixtures, regression checks, and QA verification helpers.

The root game class is `MainGame`, which wires shared LibGDX startup code together. Platform-specific launch code stays in `lwjgl3`.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
# CCS-Chess-Game
