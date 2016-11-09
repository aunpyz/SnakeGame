package com.anonyz.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import java.awt.*;

/**
 * Created by Aunpyz on 11/2/2016.
 */
public class GameScreen extends ScreenAdapter {
    private SpriteBatch batch;
    private Texture snakeHead;
    private Texture apple;
    private Texture snakeBody;

    private static final float MOVE_TIME = 1f;
    private float timer = MOVE_TIME;

    private static final int SNAKE_MOVEMENT = 32;
    private static final int GRID_CELL = SNAKE_MOVEMENT;
    private int snakeX = 0, snakeY = 0;

    private enum dir {RIGHT, LEFT, UP, DOWN}

    private dir snakeDirection = dir.RIGHT;

    //apple
    private boolean appleAvailable = false;
    private int appleX, appleY;

    //array for body parts
    private Array<BodyPart> bodyParts = new Array<BodyPart>();
    private int snakeXBeforeUpdate = 0, snakeYBeforeUpdate = 0;

    private ShapeRenderer shapeRenderer;

    @Override
    public void show() {
        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        snakeHead = new Texture(Gdx.files.internal("snakehead.png"));
        apple = new Texture(Gdx.files.internal("apple.png"));
        snakeBody = new Texture(Gdx.files.internal("snakeBody.png"));
    }

    @Override
    public void render(float delta) {
        queryInput();
        timer -= delta;
        if (timer <= 0) {
            timer = MOVE_TIME;
            moveSnake();
            checkForOutOfBounds();
            updateBodyPartsPosition();
        }
        checkAppleCollision();
        checkAndPlaceApple();
        clear();
        drawGrid();
        draw();
    }

    private void checkForOutOfBounds() {
        if (snakeX >= Gdx.graphics.getWidth())
            snakeX = 0;
        if (snakeX < 0)
            snakeX = Gdx.graphics.getWidth() - SNAKE_MOVEMENT;
        if (snakeY >= Gdx.graphics.getHeight())
            snakeY = 0;
        if (snakeY < 0)
            snakeY = Gdx.graphics.getHeight() - SNAKE_MOVEMENT;
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
            snakeDirection = dir.LEFT;
        if (rPressed)
            snakeDirection = dir.RIGHT;
        if (uPressed)
            snakeDirection = dir.UP;
        if (dPressed)
            snakeDirection = dir.DOWN;
    }

    private void checkAndPlaceApple() {
        if (!appleAvailable) {
            do {
                appleX = MathUtils.random(Gdx.graphics.getWidth() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleY = MathUtils.random(Gdx.graphics.getHeight() / SNAKE_MOVEMENT - 1) * SNAKE_MOVEMENT;
                appleAvailable = true;
            } while (appleX == snakeX && appleY == snakeY);
        }
    }

    private void clear() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void draw()
    {
        batch.begin();
        if(appleAvailable)
        {
            batch.draw(apple, appleX, appleY);
        }
        batch.draw(snakeHead, snakeX, snakeY);
        for(BodyPart bodyPart : bodyParts)
        {
            bodyPart.draw(batch);
        }
        batch.end();
    }

    private void checkAppleCollision()
    {
        if(appleAvailable && appleX == snakeX && appleY == snakeY)
        {
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updateBodyPosition(snakeX, snakeY);
            bodyParts.insert(0, bodyPart);
            appleAvailable = false;
        }
    }

    private class BodyPart
    {
        private int x, y;
        private Texture texture;

        public BodyPart(Texture texture)
        {
            this.texture = texture;
        }

        public void updateBodyPosition(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        public void draw(Batch batch)
        {
            if(!(x == snakeX && y == snakeY))
                batch.draw(texture, x, y);
        }
    }

    private void updateBodyPartsPosition()
    {
        if(bodyParts.size > 0)
        {
            BodyPart bodyPart = bodyParts.removeIndex(0);
            bodyPart.updateBodyPosition(snakeXBeforeUpdate, snakeYBeforeUpdate);
            bodyParts.add(bodyPart);
        }
    }

    private void drawGrid() {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < Gdx.graphics.getWidth(); i += GRID_CELL)
        {
            for (int j = 0; j < Gdx.graphics.getHeight(); j += GRID_CELL)
            {
                shapeRenderer.rect(i, j, GRID_CELL, GRID_CELL);
            }
        }
        shapeRenderer.end();
    }
}
