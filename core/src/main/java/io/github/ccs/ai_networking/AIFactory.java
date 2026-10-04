package io.github.ccs.ai_networking;

/** Creates the bot matching the selected difficulty. */
public final class AIFactory {

    private AIFactory() {
    }

    /**
     * @return a new bot instance for the given difficulty.
     */
    public static ChessAI create(Difficulty difficulty) {
        switch (difficulty) {
            case EASY:
                return new EasyAI();

            case HARD:
                return new HardAI();

            case MEDIUM:
            default:
                return new MediumAI();
        }
    }
}
