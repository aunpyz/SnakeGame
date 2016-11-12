package com.anonyz.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.*;
import com.sun.javafx.scene.control.GlobalMenuAdapter;

/**
 * Created by Aunpyz on 11/2/2016.
 */
public class GameScreen extends ScreenAdapter {
    //viewport sizes
    private static final float WORLD_WIDTH = 640;
    private static final float WORLD_HEIGHT = 480;
    private Viewport viewport;
    private Camera camera;

    private SpriteBatch batch;
    private Texture snakeHead;
    private Texture apple;
    private Texture snakeBody;

    private static final float MOVE_TIME = 0.1f;
    private float timer = MOVE_TIME;

    private static final int SNAKE_MOVEMENT = 32;
    private static final int GRID_CELL = SNAKE_MOVEMENT;
    private int snakeX = 0, snakeY = 0;

    private enum DIR {RIGHT, LEFT, UP, DOWN}
    private enum GAME_STATE {PLAYING, GAME_OVER}

    private DIR snakeDirection = DIR.RIGHT;
    private GAME_STATE state = GAME_STATE.PLAYING;

    //apple and relatives
    private int score = 0;
    private static final int APPLE_SCORE = 20;
    private boolean appleAvailable = false;
    private int appleX, appleY;

    //array for body parts
    private Array<BodyPart> bodyParts = new Array<BodyPart>();
    private int snakeXBeforeUpdate = 0, snakeYBeforeUpdate = 0;

    private ShapeRenderer shapeRenderer;
    private boolean directionSet = false;

    //show game over state
    private BitmapFont bitmapFont;
    private GlyphLayout layout = new GlyphLayout();
    private static final String GAME_OVER_TEXT = "Game Over... Press space to restart";

    @Override
    public void show() {
        //camera and viewport initialize
        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(WORLD_WIDTH/2, WORLD_HEIGHT/2, 0);
        camera.update();
        viewport = new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        
        bitmapFont = new BitmapFont();
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        snakeHead = new Texture(Gdx.files.internal("snakehead.png"));
        apple = new Texture(Gdx.files.internal("apple.png"));
        snakeBody = new Texture(Gdx.files.internal("snakeBody.png"));
    }

    @Override
    public void render(float delta) {
        switch (state)
        {
            case PLAYING:
                queryInput();
                updateSnake(delta);
                checkAppleCollision();
                checkAndPlaceApple();
                break;
            case GAME_OVER:
                checkForRestart();
                break;
        }
        clear();
        //drawGrid();
        draw();
    }

    @Override
    public void resize(int width, int height)
    {
        super.resize(width, height);
        viewport.update(width, height);
    }

    private void checkForOutOfBounds() {
        if (snakeX >= viewport.getScreenWidth())
            snakeX = 0;
        if (snakeX < 0)
            snakeX = (int)viewport.getScreenWidth() - SNAKE_MOVEMENT;
        if (snakeY >= viewport.getScreenHeight())
            snakeY = 0;
        if (snakeY < 0)
            snakeY = (int)viewport.getScreenHeight() - SNAKE_MOVEMENT;
    }

    private void moveSnake() {
        snakeXBeforeUpdate = snakeX;
        snakeYBeforeUpdate = snakeY;
        switch (snakeDirection) {
            case RIGHT:
                snakeX += SNAKE_MOVEMENT;
                break;
            case LEFT:
                snakeX -= SNAKE_MOVEMENT;
                break;
            case UP:
                snakeY += SNAKE_MOVEMENT;
                break;
            case DOWN:
                snakeY -= SNAKE_MOVEMENT;
                break;
        }
    }

    private void queryInput() {
        boolean lPressed = Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rPressed = Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean uPressed = Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean dPressed = Gdx.input.isKeyPressed(Input.Keys.DOWN);

        if (lPressed)
            updateDirection(DIR.LEFT);
        if (rPressed)
            updateDirection(DIR.RIGHT);
        if (uPressed)
            updateDirection(DIR.UP);
        if (dPressed)
            updateDirection(DIR.DOWN);
    }

    private void checkAndPlaceApple() {
        if (!appleAvailable) {
            do {
                appleX = MathUtils.random((int)viewport.getWorldWidth() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleY = MathUtils.random((int)viewport.getWorldHeight() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleAvailable = true;
            } while (appleX == snakeX && appleY == snakeY);
        }
    }

    private void clear() {
        Gdx.gl.glClearColor(135/255f, 206/255f, 235/255f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void draw() {
        batch.setProjectionMatrix(camera.projection);
        batch.setTransformMatrix(camera.view);

        batch.begin();
        if (appleAvailable) {
            batch.draw(apple, appleX, appleY);
        }
        batch.draw(snakeHead, snakeX, snakeY);
        for (BodyPart bodyPart : bodyParts) {
            bodyPart.draw(batch);
        }
        if(state == GAME_STATE.GAME_OVER)
        {
            layout.setText(bitmapFont, GAME_OVER_TEXT);
            bitmapFont.setColor(0, 0, 0, 1);
            bitmapFont.draw(batch, GAME_OVER_TEXT, (viewport.getWorldWidth() - layout.width)/2 , (viewport.getWorldHeight() - layout.height)/2);
        }
        drawScore();
        batch.end();
    }

    private void drawScore()
    {
        if(state == GAME_STATE.PLAYING)
        {
            String scoreAsString = Integer.toString(score);
            layout.setText(bitmapFont, scoreAsString);
            bitmapFont.setColor(0, 0, 0, 1);
            bitmapFont.draw(batch, scoreAsString, viewport.getWorldWidth()/2 - layout.width/2, (4 * viewport.getWorldHeight())/5 - layout.height/2);
        }
    }

    private void checkAppleCollision() {
        if (appleAvailable && appleX == snakeX && appleY == snakeY) {
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updateBodyPosition(snakeX, snakeY);
            bodyParts.insert(0, bodyPart);
            //adding score to score by APPLE_SCORE
            score += APPLE_SCORE;
            appleAvailable = false;
        }
    }

    private class BodyPart {
        private int x, y;
        private Texture texture;

        public BodyPart(Texture texture) {
            this.texture = texture;
        }

        public void updateBodyPosition(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public void draw(Batch batch) {
            if (!(x == snakeX && y == snakeY))
                batch.draw(texture, x, y);
        }
    }

    private void updateBodyPartsPosition() {
        if (bodyParts.size > 0) {
            BodyPart bodyPart = bodyParts.removeIndex(0);
            bodyPart.updateBodyPosition(snakeXBeforeUpdate, snakeYBeforeUpdate);
            bodyParts.add(bodyPart);
        }
    }

    private void drawGrid() {
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 0.5f, 0.5f, 100);
        for (int i = 0; i < viewport.getWorldWidth(); i += GRID_CELL) {
            for (int j = 0; j < viewport.getWorldHeight(); j += GRID_CELL) {
                shapeRenderer.rect(i, j, GRID_CELL, GRID_CELL);
            }
        }
        shapeRenderer.end();
    }

    private void updateIfNotOppositeDirection(DIR newSnakeDirection, DIR oppositeDirection) {
        if (snakeDirection != oppositeDirection || bodyParts.size == 0)
            snakeDirection = newSnakeDirection;
    }

    private void updateDirection(DIR newSnakeDirection) {
        if (!directionSet && snakeDirection != newSnakeDirection) {
            directionSet = true;
            switch (newSnakeDirection) {
                case LEFT:
                    updateIfNotOppositeDirection(newSnakeDirection, DIR.RIGHT);
                    break;
                case RIGHT:
                    updateIfNotOppositeDirection(newSnakeDirection, DIR.LEFT);
                    break;
                case UP:
                    updateIfNotOppositeDirection(newSnakeDirection, DIR.DOWN);
                    break;
                case DOWN:
                    updateIfNotOppositeDirection(newSnakeDirection, DIR.UP);
                    break;
            }
        }
    }

    private void checkSnakeBodyCollision()
    {
        for (BodyPart bodypart : bodyParts)
        {
            if(bodypart.x == snakeX && bodypart.y == snakeY)
                state = GAME_STATE.GAME_OVER;
        }
    }

    private void updateSnake(float delta)
    {
        timer -= delta;
        if (timer <= 0)
        {
            timer = MOVE_TIME;
            moveSnake();
            checkForOutOfBounds();
            updateBodyPartsPosition();
            checkSnakeBodyCollision();
            directionSet = false;
        }
    }

    private void checkForRestart()
    {
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE))
            doRestart();
    }

    private void doRestart()
    {
        state = GAME_STATE.PLAYING;
        bodyParts.clear();
        snakeDirection = DIR.RIGHT;
        directionSet = false;
        snakeX = 0;
        snakeY = 0;
        snakeYBeforeUpdate = snakeY;
        snakeXBeforeUpdate = snakeX;
        appleAvailable = false;
        score = 0;
    }
}
