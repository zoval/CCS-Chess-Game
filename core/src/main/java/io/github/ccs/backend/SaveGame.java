package io.github.ccs.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import io.github.ccs.game_logic.GameState;
import io.github.ccs.game_logic.MoveSnapshot;
import io.github.ccs.game_logic.SpecialMoves;

/**
 * Single-slot save/load backed by a {@link Properties} file. Serialization
 * is headless (pure Java); {@link #write(GameState)} and {@link #read()} do
 * the file IO through libGDX. {@link #parse(Properties)} returns null for
 * missing, corrupt, or future-version saves instead of throwing.
 */
public final class SaveGame {

    /** Single save slot filename, relative to the game's local storage. */
    public static final String FILE_NAME = "blocktics_save.properties";

    /** Save format version; saves with a different version fail to load. */
    public static final String VERSION = "1";

    private static final String KEY_VERSION = "version";
    private static final String KEY_MODE = "mode";
    private static final String KEY_DIFFICULTY = "difficulty";
    private static final String KEY_MAP = "map";
    private static final String KEY_TIME_CONTROL = "timeControlMinutes";
    private static final String KEY_WHITE_MILLIS = "whiteMillis";
    private static final String KEY_BLACK_MILLIS = "blackMillis";
    private static final String KEY_HISTORY_INDEX = "historyIndex";
    private static final String KEY_SNAPSHOT_COUNT = "snapshot.count";

    private SaveGame() {
    }

    /** @return true if a save file exists on disk. */
    public static boolean exists() {
        return Gdx.files.local(FILE_NAME).exists();
    }

    /** Writes the state to the single save slot, replacing any previous save. */
    public static void write(GameState state) throws IOException {
        FileHandle file = Gdx.files.local(FILE_NAME);
        serialize(state).store(file.write(false), null);
    }

    /**
     * @return the saved game, or null when there is no save file or it
     * cannot be read.
     */
    public static GameState read() {
        FileHandle file = Gdx.files.local(FILE_NAME);
        if (!file.exists()) {
            return null;
        }
        Properties props = new Properties();
        try {
            props.load(file.read());
        } catch (IOException e) {
            return null;
        }
        return parse(props);
    }

    /** @return the save properties for the given state. */
    public static Properties serialize(GameState state) {
        Properties props = new Properties();
        props.setProperty(KEY_VERSION, VERSION);
        props.setProperty(KEY_MODE, state.getMode());
        props.setProperty(KEY_DIFFICULTY, state.getDifficulty());
        props.setProperty(KEY_MAP, state.getMapName());
        props.setProperty(KEY_TIME_CONTROL, Integer.toString(state.getTimeControlMinutes()));
        props.setProperty(KEY_WHITE_MILLIS, Long.toString(state.getWhiteMillis()));
        props.setProperty(KEY_BLACK_MILLIS, Long.toString(state.getBlackMillis()));
        props.setProperty(KEY_HISTORY_INDEX, Integer.toString(state.getHistoryIndex()));

        List<MoveSnapshot> snapshots = state.getSnapshots();
        props.setProperty(KEY_SNAPSHOT_COUNT, Integer.toString(snapshots.size()));
        for (int i = 0; i < snapshots.size(); i++) {
            appendSnapshot(props, "snapshot." + i + ".", snapshots.get(i));
        }
        return props;
    }

    /**
     * @return the state described by the properties, or null when a required
     * key is missing or a value is malformed.
     */
    public static GameState parse(Properties props) {
        if (props == null || props.isEmpty()) {
            return null;
        }
        try {
            if (!VERSION.equals(props.getProperty(KEY_VERSION))) {
                return null;
            }

            GameState state = new GameState();
            state.setMode(require(props, KEY_MODE));
            state.setDifficulty(require(props, KEY_DIFFICULTY));
            state.setMapName(require(props, KEY_MAP));
            state.setTimeControlMinutes(Integer.parseInt(require(props, KEY_TIME_CONTROL)));
            state.setWhiteMillis(Long.parseLong(require(props, KEY_WHITE_MILLIS)));
            state.setBlackMillis(Long.parseLong(require(props, KEY_BLACK_MILLIS)));

            int count = Integer.parseInt(require(props, KEY_SNAPSHOT_COUNT));
            int historyIndex = Integer.parseInt(require(props, KEY_HISTORY_INDEX));
            if (count < 1 || historyIndex < 0 || historyIndex >= count) {
                return null;
            }

            List<MoveSnapshot> snapshots = new ArrayList<MoveSnapshot>(count);
            for (int i = 0; i < count; i++) {
                snapshots.add(parseSnapshot(props, "snapshot." + i + "."));
            }
            state.setSnapshots(snapshots);
            state.setHistoryIndex(historyIndex);
            return state;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void appendSnapshot(Properties props, String prefix, MoveSnapshot snapshot) {
        StringBuilder pieces = new StringBuilder();
        for (int piece : snapshot.getPieces()) {
            if (pieces.length() > 0) {
                pieces.append(',');
            }
            pieces.append(piece);
        }
        props.setProperty(prefix + "pieces", pieces.toString());
        props.setProperty(prefix + "whiteTurn", Boolean.toString(snapshot.isWhiteTurn()));
        props.setProperty(prefix + "statusText", snapshot.getStatusText());
        props.setProperty(prefix + "gameOver", Boolean.toString(snapshot.isGameOver()));
        props.setProperty(prefix + "halfmoveClock", Integer.toString(snapshot.getHalfmoveClock()));
        props.setProperty(prefix + "special", snapshot.getSpecialMoves().encode());
        props.setProperty(prefix + "capturedByWhite", joinInts(snapshot.getCapturedByWhite()));
        props.setProperty(prefix + "capturedByBlack", joinInts(snapshot.getCapturedByBlack()));
    }

    private static MoveSnapshot parseSnapshot(Properties props, String prefix) {
        int[][] pieces = new int[8][8];
        String[] flat = require(props, prefix + "pieces").split(",");
        if (flat.length != 64) {
            throw new IllegalArgumentException("Expected 64 squares, got " + flat.length);
        }
        for (int i = 0; i < 64; i++) {
            pieces[i / 8][i % 8] = Integer.parseInt(flat[i]);
        }

        List<Integer> capturedByWhite = parseIntList(require(props, prefix + "capturedByWhite"));
        List<Integer> capturedByBlack = parseIntList(require(props, prefix + "capturedByBlack"));
        return new MoveSnapshot(pieces,
                Boolean.parseBoolean(require(props, prefix + "whiteTurn")),
                require(props, prefix + "statusText"),
                Boolean.parseBoolean(require(props, prefix + "gameOver")),
                Integer.parseInt(require(props, prefix + "halfmoveClock")),
                SpecialMoves.decode(require(props, prefix + "special")),
                capturedByWhite, capturedByBlack);
    }

    private static String require(Properties props, String key) {
        String value = props.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing key: " + key);
        }
        return value;
    }

    private static String joinInts(List<Integer> values) {
        StringBuilder joined = new StringBuilder();
        for (Integer value : values) {
            if (joined.length() > 0) {
                joined.append(',');
            }
            joined.append(value);
        }
        return joined.toString();
    }

    private static List<Integer> parseIntList(String value) {
        List<Integer> values = new ArrayList<Integer>();
        if (value.isEmpty()) {
            return values;
        }
        for (String part : value.split(",")) {
            values.add(Integer.parseInt(part));
        }
        return values;
    }
}
