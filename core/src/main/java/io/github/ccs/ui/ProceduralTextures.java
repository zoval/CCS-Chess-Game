package io.github.ccs.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

/** Generates small pixel-based textures at runtime so UI features need no extra art files. */
final class ProceduralTextures {

    private ProceduralTextures() {
    }

    /** Solid white 1x1 texture; tint via {@code SpriteBatch.setColor} when drawing. */
    static Texture whitePixel() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(1f, 1f, 1f, 1f);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** Soft filled circle, white with radially falling alpha. */
    static Texture softDot(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float center = (size - 1) / 2f;
        float radius = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float distance = (float) Math.hypot(x - center, y - center) / radius;
                if (distance < 1f) {
                    float alpha = (float) Math.pow(1f - distance, 1.5f);
                    pixmap.setColor(1f, 1f, 1f, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** Soft-edged ring (annulus), white with radially falling alpha on both edges. */
    static Texture softRing(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float center = (size - 1) / 2f;
        float radius = size / 2f;
        float inner = 0.60f;
        float core = 0.72f;
        float outer = 0.95f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float distance = (float) Math.hypot(x - center, y - center) / radius;
                if (distance >= inner && distance <= outer) {
                    float alpha = distance < core
                        ? (distance - inner) / (core - inner)
                        : (outer - distance) / (outer - core);
                    pixmap.setColor(1f, 1f, 1f, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** Wide soft radial gradient used for additive hover glow. */
    static Texture radialGlow(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float center = (size - 1) / 2f;
        float radius = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float distance = (float) Math.hypot(x - center, y - center) / radius;
                if (distance < 1f) {
                    float alpha = (float) Math.pow(1f - distance, 2.2f);
                    pixmap.setColor(1f, 1f, 1f, alpha);
                    pixmap.drawPixel(x, y);
                }
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    /** Simple cog silhouette (8 teeth, holed hub) for the in-game settings button. */
    static Texture gearIcon(int size) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float center = (size - 1) / 2f;
        float radius = size / 2f;
        float hubInner = 0.20f;
        float hubOuter = 0.34f;
        float base = 0.62f;
        float teethTip = 0.86f;
        int teeth = 8;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - center;
                float dy = y - center;
                float distance = (float) Math.hypot(dx, dy) / radius;
                if (distance > teethTip) {
                    continue;
                }
                float angle = (float) Math.atan2(dy, dx);
                float toothPhase = (float) Math.cos(angle * teeth);
                float edge = toothPhase > 0.35f ? teethTip : base;
                boolean solid = distance <= edge
                    && !(distance >= hubInner && distance <= hubOuter);
                if (solid) {
                    pixmap.setColor(0.82f, 0.78f, 0.70f, 1f);
                    pixmap.drawPixel(x, y);
                }
            }
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }
}
