package io.github.ccs.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

/**
 * Menu button that illuminates with an additive glow and a subtle scale bounce while hovered.
 * A {@code null} action renders the button disabled, matching the old createButton behavior.
 */
public class IlluminatedButton extends ImageButton implements Disposable {
    private static final float HOVER_SCALE = 1.03f;
    private static final float SCALE_SPEED = 12f;
    private static final float GLOW_SIZE = 1.9f;
    private static final float GLOW_ALPHA = 0.55f;
    private static final Color GLOW_TINT = new Color(1f, 0.88f, 0.55f, 1f);

    private final Texture glowTexture;
    private final Runnable action;
    private float hoverProgress;
    private float hoverTarget;

    public IlluminatedButton(TextureRegion region, Runnable action) {
        super(new TextureRegionDrawable(region));
        this.action = action;
        glowTexture = ProceduralTextures.radialGlow(128);

        if (action == null) {
            setTouchable(Touchable.disabled);
            return;
        }

        addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                IlluminatedButton.this.action.run();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    hoverTarget = 1f;
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    hoverTarget = 0f;
                }
            }
        });
    }

    @Override
    protected void sizeChanged() {
        super.sizeChanged();
        setOrigin(getWidth() / 2f, getHeight() / 2f);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float step = Math.min(1f, delta * SCALE_SPEED);
        hoverProgress += (hoverTarget - hoverProgress) * step;
        if (Math.abs(hoverProgress - hoverTarget) < 0.01f) {
            hoverProgress = hoverTarget;
        }
        float scale = 1f + (HOVER_SCALE - 1f) * hoverProgress;
        setScale(scale);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (hoverProgress > 0.01f) {
            float glowW = getWidth() * GLOW_SIZE;
            float glowH = getHeight() * GLOW_SIZE;
            float glowX = getX() + getWidth() / 2f - glowW / 2f;
            float glowY = getY() + getHeight() / 2f - glowH / 2f;
            Color previousColor = batch.getColor().cpy();
            batch.setColor(GLOW_TINT.r, GLOW_TINT.g, GLOW_TINT.b, GLOW_ALPHA * hoverProgress * parentAlpha);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE);
            batch.draw(glowTexture, glowX, glowY, glowW, glowH);
            batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            batch.setColor(previousColor);
        }
        super.draw(batch, parentAlpha);
    }

    @Override
    public void dispose() {
        glowTexture.dispose();
    }
}
