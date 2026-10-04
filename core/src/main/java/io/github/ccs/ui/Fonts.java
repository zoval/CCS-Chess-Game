package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * Loads the Press Start 2P pixel font at fixed sizes with Nearest filtering
 * (crisp scaling). Falls back to the built-in bitmap font when the TTF is
 * missing so the game still runs from a bare asset tree. Call {@link #init()}
 * once after LibGDX is up; fonts stay valid until {@link #dispose()}.
 */
public final class Fonts {

    public static final int[] SIZES = {8, 10, 12, 16, 24};
    private static final String FONT_PATH = "fonts/PressStart2P-Regular.ttf";

    private static BitmapFont[] fonts;

    private Fonts() {
    }

    public static synchronized void init() {
        if (fonts != null) return;
        fonts = new BitmapFont[SIZES.length];

        if (Gdx.files.internal(FONT_PATH).exists()) {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONT_PATH));
            FreeTypeFontGenerator.FreeTypeFontParameter parameter =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
            parameter.minFilter = Texture.TextureFilter.Nearest;
            parameter.magFilter = Texture.TextureFilter.Nearest;
            for (int i = 0; i < SIZES.length; i++) {
                parameter.size = SIZES[i];
                fonts[i] = generator.generateFont(parameter);
            }
            generator.dispose();
        } else {
            for (int i = 0; i < SIZES.length; i++) {
                BitmapFont fallback = new BitmapFont();
                fallback.getData().setScale(SIZES[i] / 16f);
                fonts[i] = fallback;
            }
        }
    }

    /** @return the font at the given pixel size, snapped to the nearest configured size. */
    public static BitmapFont get(int size) {
        init();
        int best = 0;
        for (int i = 1; i < SIZES.length; i++) {
            if (Math.abs(SIZES[i] - size) < Math.abs(SIZES[best] - size)) best = i;
        }
        return fonts[best];
    }

    public static BitmapFont tiny() {
        return get(8);
    }

    public static BitmapFont small() {
        return get(10);
    }

    public static BitmapFont medium() {
        return get(12);
    }

    public static BitmapFont large() {
        return get(16);
    }

    public static BitmapFont title() {
        return get(24);
    }

    public static synchronized void dispose() {
        if (fonts == null) return;
        for (BitmapFont font : fonts) {
            font.dispose();
        }
        fonts = null;
    }
}
