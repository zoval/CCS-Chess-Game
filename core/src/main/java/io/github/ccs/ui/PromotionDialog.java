package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

import io.github.ccs.game_logic.Piece;
import io.github.ccs.sound.SoundManager;

/**
 * Modal promotion picker: a wooden box with the four promotion pieces as
 * icons. The box art flips based on which half of the board the promoting
 * pawn lands on, icons glow green on hover, and clicking the backdrop
 * cancels so the pawn keeps its square.
 */
public class PromotionDialog extends Group implements Disposable {

    /** Receives the chosen {@link Piece} type (QUEEN, ROOK, KNIGHT, or BISHOP). */
    public interface PromotionListener {
        void onPromotionChosen(int pieceType);
    }

    private static final float BOX_ASPECT = 684f / 1612f;

    private final Texture boxLeftTexture;
    private final Texture boxRightTexture;
    private final Texture backdropTexture;
    private final TextureRegionDrawable boxLeftDrawable;
    private final TextureRegionDrawable boxRightDrawable;
    private final Image boxImage;
    private final PieceChoice[] choices;
    private PromotionListener listener;
    private Runnable cancelAction;
    private float boxX, boxY, boxW, boxH;

    public PromotionDialog() {
        boxLeftTexture = new Texture(Gdx.files.internal("promotion/box_left.png"));
        boxRightTexture = new Texture(Gdx.files.internal("promotion/box_right.png"));
        backdropTexture = ProceduralTextures.whitePixel();
        boxLeftDrawable = new TextureRegionDrawable(new TextureRegion(boxLeftTexture, 48, 8, 1612, 684));
        boxRightDrawable = new TextureRegionDrawable(new TextureRegion(boxRightTexture, 0, 8, 1612, 684));

        setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        Image backdrop = new Image(new TextureRegionDrawable(new TextureRegion(backdropTexture)));
        backdrop.setColor(0f, 0f, 0f, 0.45f);
        backdrop.setFillParent(true);
        backdrop.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                cancel();
            }
        });
        addActor(backdrop);

        boxImage = new Image(boxLeftDrawable);
        addActor(boxImage);

        choices = new PieceChoice[]{
            new PieceChoice(Piece.QUEEN, "promotion/queen.png", 674, 21, 651, 875,
                "promotion/queen_selected.png", 674, 21, 651, 875),
            new PieceChoice(Piece.ROOK, "promotion/rook.png", 72, 35, 1159, 840,
                "promotion/rook_selected.png", 0, 35, 1231, 840),
            new PieceChoice(Piece.KNIGHT, "promotion/knight.png", 380, 21, 945, 931,
                "promotion/knight_selected.png", 272, 21, 1053, 931),
            new PieceChoice(Piece.BISHOP, "promotion/bishop.png", 578, 21, 747, 875,
                "promotion/bishop_selected.png", 578, 21, 747, 875),
        };
        for (final PieceChoice choice : choices) {
            addActor(choice.image);
            choice.image.addListener(new ClickListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer == -1) {
                        applyVisual(choice, true);
                    }
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (pointer == -1) {
                        applyVisual(choice, false);
                    }
                }

                @Override
                public void clicked(InputEvent event, float x, float y) {
                    SoundManager.getInstance().playUIClick();
                    PromotionListener chosen = listener;
                    close();
                    if (chosen != null) {
                        chosen.onPromotionChosen(choice.pieceType);
                    }
                }
            });
        }

        setVisible(false);
    }

    /**
     * Shows the dialog for a pawn promoting on the given destination column.
     * Clicking an icon reports the choice; clicking anywhere else runs
     * {@code cancelAction} instead.
     */
    public void open(int promotionColumn, PromotionListener listener, Runnable cancelAction) {
        this.listener = listener;
        this.cancelAction = cancelAction;
        boxImage.setDrawable(promotionColumn < 4 ? boxRightDrawable : boxLeftDrawable);
        for (PieceChoice choice : choices) {
            applyVisual(choice, false);
        }
        setVisible(true);
    }

    public void close() {
        setVisible(false);
        listener = null;
        cancelAction = null;
    }

    public boolean isOpen() {
        return isVisible();
    }

    /** Centers the box over the board and lays the icons out inside it. */
    public void layout(float boardX, float boardY, float boardSize) {
        boxW = boardSize * 0.98f;
        boxH = boxW * BOX_ASPECT;
        boxX = boardX + (boardSize - boxW) / 2f;
        boxY = boardY + (boardSize - boxH) / 2f;
        boxImage.setBounds(boxX, boxY, boxW, boxH);
        for (PieceChoice choice : choices) {
            choice.measure(boxX, boxY, boxW, boxH);
            applyVisual(choice, false);
        }
    }

    private void applyVisual(PieceChoice choice, boolean selected) {
        TextureRegion region = selected ? choice.selectedRegion : choice.normalRegion;
        choice.image.setDrawable(new TextureRegionDrawable(region));
        float width = selected ? choice.selectedWidth : choice.normalWidth;
        float height = selected ? choice.selectedHeight : choice.normalHeight;
        choice.image.setSize(width, height);
        choice.image.setPosition(choice.centerX - width / 2f, choice.bottomY);
    }

    private void cancel() {
        Runnable action = cancelAction;
        close();
        if (action != null) {
            action.run();
        }
    }

    @Override
    public void dispose() {
        boxLeftTexture.dispose();
        boxRightTexture.dispose();
        backdropTexture.dispose();
        for (PieceChoice choice : choices) {
            choice.dispose();
        }
    }

    /** One promotion icon with its two art variants and computed layout slot. */
    private static final class PieceChoice implements Disposable {
        final int pieceType;
        final Texture normalTexture;
        final Texture selectedTexture;
        final TextureRegion normalRegion;
        final TextureRegion selectedRegion;
        final Image image;
        float centerX;
        float bottomY;
        float normalWidth;
        float normalHeight;
        float selectedWidth;
        float selectedHeight;

        PieceChoice(int pieceType, String normalPath, int nx, int ny, int nw, int nh,
                    String selectedPath, int sx, int sy, int sw, int sh) {
            this.pieceType = pieceType;
            normalTexture = new Texture(Gdx.files.internal(normalPath));
            selectedTexture = new Texture(Gdx.files.internal(selectedPath));
            normalRegion = new TextureRegion(normalTexture, nx, ny, nw, nh);
            selectedRegion = new TextureRegion(selectedTexture, sx, sy, sw, sh);
            image = new Image(new TextureRegionDrawable(normalRegion));
        }

        void measure(float boxX, float boxY, float boxW, float boxH) {
            float cellW = boxW / 4f;
            float maxH = boxH * 0.86f;
            float maxW = cellW * 0.98f;
            centerX = boxX + cellW * (index() + 0.5f);
            bottomY = boxY + boxH * 0.06f;
            float[] fitted = fit(maxW, maxH,
                normalRegion.getRegionWidth(), normalRegion.getRegionHeight());
            normalWidth = fitted[0];
            normalHeight = fitted[1];
            fitted = fit(maxW, maxH,
                selectedRegion.getRegionWidth(), selectedRegion.getRegionHeight());
            selectedWidth = fitted[0];
            selectedHeight = fitted[1];
        }

        private int index() {
            switch (pieceType) {
                case Piece.QUEEN: return 0;
                case Piece.ROOK: return 1;
                case Piece.KNIGHT: return 2;
                default: return 3;
            }
        }

        private static float[] fit(float maxW, float maxH, int regionW, int regionH) {
            float height = maxH;
            float width = height * (regionW / (float) regionH);
            if (width > maxW) {
                width = maxW;
                height = width * (regionH / (float) regionW);
            }
            return new float[]{width, height};
        }

        @Override
        public void dispose() {
            normalTexture.dispose();
            selectedTexture.dispose();
        }
    }
}
