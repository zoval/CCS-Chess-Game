package io.github.ccs.ui;

/**
 * Playable maps. Each map pairs a board/piece art set with an arena background.
 *
 * <p>This is presentation state only. It must not affect chess rules, board state, or move validation.</p>
 */
public enum BoardTheme {
    /** Classic wood board with pixel-art Minecraft-inspired pieces (the original look). */
    CLASSIC,

    /** Traditional tournament board and piece artwork. */
    STANDARD,

    /** Nether arena: classic board over a fiery underworld backdrop. */
    NETHER
}
