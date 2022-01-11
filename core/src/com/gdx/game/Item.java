package com.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public enum Item {
	CRANBERRY("cranberry", "Brusnica", "farm assets\\Fruits and Vegetables\\Cranberries.png", 0, 0, 32, 32),
	BLUEBERRY("blueberry", "Borovnica", "farm assets\\Fruits and Vegetables\\Blueberries.png", 0, 0, 32, 32),
	APPLE("apple", "Jabuka", "farm assets\\Fruits and Vegetables\\Apple Red.png", 0, 0, 32, 32),
	BREAD("bread", "Kruh", "Ghostpixxells_pixelfood\\07_bread.png", 0, 0, 32, 32),
	MUSHROOM("mushroom", "Gljive", "farm assets\\Fruits and Vegetables\\Mushroom White.png", 0, 0, 32, 32),
	SUGAR("sugar", "Secer", "Pixel_Mart\\sugar.png", 0, 0, 32, 32),
	EGG("egg", "Jaja", "Pixel_Mart\\egg_brown", 0, 0, 32, 32),
	FLOUR("flour", "Brasno", "Pixel_Mart\\flour.png", 0, 0, 32, 32),
	CARROT("carrot", "Mrkva", "farm assets\\Fruits and Vegetables\\Carrot.png", 0, 0, 32, 32),
	OIL("oil", "Ulje", "Pixel_Mart\\olive_oil.png", 0, 0, 32, 32),
	WATER("water", "Voda", "Pixel_Mart\\water.png", 0, 0, 32, 32),
	BAKING_POWDER("bakingPowder", "Prasak za pecivo", "Pixel_Mart\\baking_powder.png", 0, 0, 32, 32);
	
	public final String name;
	public final String guiName;
	public final String texturePath;
	public final int textureX;
	public final int textureY;
	public final int textureWidth;
	public final int textureHeight;
	public static final String itemPickingSoundFile = "RPGsounds_Kenney\\OGG\\handleSmallLeather2.ogg";
	
	private Item(String itemName, String guiName, String texPath, int x, int y, int w, int h) {
		this.name = itemName;
		this.guiName = guiName;
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
