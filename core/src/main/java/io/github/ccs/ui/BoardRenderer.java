package io.github.ccs.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage; //added a few imports for my changes
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.ccs.backend.SaveData;
import io.github.ccs.backend.SaveManager;
import io.github.ccs.game_logic.Board;
import io.github.ccs.game_logic.Piece;

//Handles drawing the chessboard, pieces, animations, and game status.
public class BoardRenderer implements Disposable {
    private final SpriteBatch batch = new SpriteBatch();
    private final BitmapFont font = new BitmapFont();
    private final Texture chessboard = new Texture(Gdx.files.internal("board.png"));
    private final Texture[][] pieceTextures = new Texture[2][6];
    //just some variables for the stage, skin and save button
    private final Stage stage;
    private final Skin skin;
    private final TextButton saveButton;
    private float boardX;
    private float boardY;
    private float boardSize;
    private Board board;
    
    public BoardRenderer(Board board) {
        this.board = board;
        String[] colors = {"white", "black"};
        String[] names = {"pawn", "knight", "bishop", "rook", "queen", "king"};
        for (int color = 0; color < 2; color++) {
            for (int type = 0; type < 6; type++) {
                pieceTextures[color][type] = new Texture(
                    Gdx.files.internal(colors[color] + "-" + names[type] + ".png"));
            }
        }
        
        //Initialize Scene2D UI for the save button
        stage = new Stage(new ScreenViewport());
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        Gdx.input.setInputProcessor(stage);
        saveButton = new TextButton("Save", skin);
        stage.addActor(saveButton); 
        //listens for clicks on the save button and prints "Save Game button clicked!" to the terminal
        saveButton.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                SaveManager.saveGame(board);
                System.out.println("Save Game button clicked!");
            }
        });        
    }

    public void updateLayout() {
        boardSize = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight() - 70);
        boardSize = Math.max(1, boardSize - 20);
        boardX = (Gdx.graphics.getWidth() - boardSize) / 2f;
        boardY = (Gdx.graphics.getHeight() - boardSize - 40) / 2f;
        //Added this to position the save button at the top right corner of the screen
        saveButton.setPosition(Gdx.graphics.getWidth() - 100, Gdx.graphics.getHeight() - 50);
        saveButton.setSize(80, 35);
    }

    public void render(Board board, PieceAnimation animation) {
        updateLayout();

        batch.begin();
        batch.draw(chessboard, boardX, boardY, boardSize, boardSize);
        drawPieces(board, animation);
        font.setColor(Color.WHITE);
        font.draw(batch, board.getStatusText(), 20, Gdx.graphics.getHeight() - 20);
        batch.end();

        //Renders UI on top of board
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    private void drawPieces(Board board, PieceAnimation animation) {
        float squareSize = getSquareSize();
        int animTargetRow = animation.getToRow();
        int animTargetColumn = animation.getToColumn();

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                int piece = board.getPiece(row, column);
                if (piece == Piece.EMPTY) continue;
                if (animation.isAnimating() && row == animTargetRow && column == animTargetColumn) continue;

                drawPiece(piece, boardX + column * squareSize, boardY + row * squareSize, squareSize);
            }
        }

        if (animation.isAnimating()) {
            float startX = boardX + animation.getFromColumn() * squareSize;
            float startY = boardY + animation.getFromRow() * squareSize;
            float targetX = boardX + animation.getToColumn() * squareSize;
            float targetY = boardY + animation.getToRow() * squareSize;
            float currentX = startX + (targetX - startX) * animation.getProgress();
            float currentY = startY + (targetY - startY) * animation.getProgress();

            drawPiece(animation.getPiece(), currentX, currentY, squareSize);
        }
    }

    private void drawPiece(int piece, float x, float y, float size) {
        int colorIndex = Piece.isWhite(piece) ? 0 : 1;
        int typeIndex = Piece.typeOf(piece) - 1;
        batch.draw(pieceTextures[colorIndex][typeIndex], x, y, size, size);
    }

    public float getBoardX() {
        return boardX;
    }

    public float getBoardY() {
        return boardY;
    }

    public float getBoardSize() {
        return boardSize;
    }

    public float getSquareSize() {
        return boardSize / 8f;
    }
    //Gets the Stage object for input processing and UI rendering
    public Stage getStage() {
        return stage;
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        chessboard.dispose();
        stage.dispose(); //added this to dispose the stage
        skin.dispose(); //added this to dispose the skin
        for (Texture[] colorGroup : pieceTextures) {
            for (Texture texture : colorGroup) {
                if (texture != null) texture.dispose();
            }
        }
    }
}
