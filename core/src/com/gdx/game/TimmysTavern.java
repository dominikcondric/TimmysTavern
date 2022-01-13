package com.gdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import screens.MainMenuScreen;

public class TimmysTavern extends Game {
	public SpriteBatch batch;
	public static BitmapFont font;
	public static Cookbook cookbook;
	
	@Override
	public void create () {
		batch = new SpriteBatch();
		font = new BitmapFont(false);
		cookbook = new Cookbook(Gdx.files.internal("Cookbook.xml"));
		this.setScreen(new MainMenuScreen(this));
	}

	@Override
	public void render () {
		super.render();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		font.dispose();
		cookbook.dispose();
	}
}
