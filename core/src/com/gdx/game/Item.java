package com.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public enum Item {
	CRANBERRY("cranberry", 0.01f, "farm assets\\Fruits and Vegetables\\Cranberries.png", 0, 0, 32, 32),
	BLUEBERRY("blueberry", 0.01f, "farm assets\\Fruits and Vegetables\\Blueberries.png", 0, 0, 32, 32),
	APPLE("apple", 0.1f, "farm assets\\Fruits and Vegetables\\Apple Red.png", 0, 0, 32, 32),
	BREAD("bread", 0.8f, "Ghostpixxells_pixelfood\\07_bread.png", 0, 0, 32, 32),
	MUSHROOM("mushroom", 0.05f, "farm assets\\Fruits and Vegetables\\Mushroom White.png", 0, 0, 32, 32);
	
	public final String name;
	public final float weight; // Weight in kilograms
	public final String texturePath;
	public final int textureX;
	public final int textureY;
	public final int textureWidth;
	public final int textureHeight;
	
	private Item(String itemName, float itemWeight, String texPath, int x, int y, int w, int h) {
		this.name = itemName;
		this.weight = itemWeight;
		texturePath = texPath;
		textureX = x;
		textureY = y;
		textureWidth = w;
		textureHeight = h;
	}
	
	public final TextureRegion getTextureRegion() {
		return new TextureRegion(new Texture(Gdx.files.internal(texturePath)), textureX, textureY, textureWidth, textureHeight);
	}
	
}
