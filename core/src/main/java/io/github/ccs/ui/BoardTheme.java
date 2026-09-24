package io.github.ccs.ui;

/**
 * Visual asset sets available for the chess board and pieces.
 *
 * <p>This is presentation state only. It must not affect chess rules, board state, or move validation.</p>
 */
public enum BoardTheme {
    /** Pixel-art Minecraft-inspired board and piece artwork. */
    MINECRAFT,

    /** Traditional chess board and piece artwork. */
    STANDARD
}
