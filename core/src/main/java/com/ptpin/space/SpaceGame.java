package com.ptpin.space;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class SpaceGame extends Game {
    public SpriteBatch batch;
    public BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.getData().setScale(1.5f);
        this.setScreen(new MenuScreen(this)); 
    }

    @Override
    public void render() {
        super.render(); 
    }

    @Override
    public void dispose() {
        if(batch != null) batch.dispose();
        if(font != null) font.dispose();
    }
}
