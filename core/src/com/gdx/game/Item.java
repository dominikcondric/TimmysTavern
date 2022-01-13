package com.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class Item {
	public Item(String name, String guiName, String texturePath, int x, int y, int w, int h) {
		this.name = name;
		this.guiName = guiName;
		this.texturePath = texturePath;
		this.textureX = x;
		this.textureY = y;
		this.textureWidth = w;
		this.textureHeight = h;
	}
	
	public Item(Item other) {
		this.name = other.name;
		this.guiName = other.guiName;
		this.texturePath = other.texturePath;
		this.textureX = other.textureX;
		this.textureY = other.textureY;
		this.textureWidth = other.textureWidth;
		this.textureHeight = other.textureHeight;
	}
	
	public final String name;
	public final String guiName;
	public final String texturePath;
	public final int textureX;
	public final int textureY;
	public final int textureWidth;
	public final int textureHeight;
	public static final String itemPickingSoundFile = "RPGsounds_Kenney\\OGG\\handleSmallLeather2.ogg";
	
	public final TextureRegion getTextureRegion() {
		return new TextureRegion(new Texture(Gdx.files.internal(texturePath)), textureX, textureY, textureWidth, textureHeight);
	}
}
