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
	OIL("oil", "Ulje", "Pixel_Mart\\cooking_oil.png", 0, 0, 32, 32),
	WATER("water", "Voda", "Pixel_Mart\\water.png", 0, 0, 32, 32),
	BAKING_POWDER("bakingPowder", "Prasak za pecivo", "Pixel_Mart\\baking_powder.png", 0, 0, 32, 32),
	PORK("meat", "Svinjetina", "Pixel_Mart\\meat1.png", 0, 0, 32, 32),
	LAMB("lamb", "Janjetina", "Pixel_Mart\\meat1_p.png", 0, 0, 32, 32),
	BEEF("govedina", "Govedina", "Pixel_Mart\\meat2.png", 0, 0, 32, 32),
	CHICKEN("chicken", "Piletina", "Pixel_Mart\\meat3.png", 0, 0, 32, 32),
	TUNA("fish", "Tuna", "Pixel_Mart\\fish.png", 0, 0, 32, 32),
	TROUT("trout", "Pastrva", "Pixel_Mart\\salmon.png", 0, 0, 32, 32),
	KETCHUP("ketchup", "Kecap", "Pixel_Mart\\ketchup.png", 0, 0, 32, 32),
	MAYONNAISE("mayonnaise", "Majoneza", "Pixel_Mart\\mustard.png", 0, 0, 32, 32),
	SALT("salt", "Sol", "Pixel_Mart\\salt.png", 0, 0, 32, 32),
	CHEESE("cheese", "Sir", "Pixel_Mart\\white_cheese.png", 0, 0, 32, 32),
	SALAD("salad", "Salata", "farm assets\\Fruits and Vegetables\\Lettuce.png", 0, 0, 32, 32),
	TOMATO("tomato", "Pasirana rajcica", "farm assets\\Fruits and Vegetables\\Tomato.png", 0, 0, 32, 32),
	POTATO("potato", "Krumpir", "farm assets\\Fruits and Vegetables\\Potato.png", 0, 0, 32, 32),
	CORN("corn", "Kukuruz", "farm assets\\Fruits and Vegetables\\Corn.png", 0, 0, 32, 32),
	EGGPLANT("eggplant", "Patlidzan", "farm assets\\Fruits and Vegetables\\Aubergine.png", 0, 0, 32, 32),
	PEPPER("pepper", "Paprika", "farm assets\\Fruits and Vegetables\\Pepper Red.png", 0, 0, 32, 32),
	BROCCOLI("broccoli", "Brokula", "farm assets\\Fruits and Vegetables\\Broccoli.png", 0, 0, 32, 32),
	PASTA("pasta", "Pasta", "Pixel_Mart\\white_cheese.png\\pencil_box.png", 0, 0, 32, 32),
	WINE("wine", "Vino", "Pixel_Mart\\white_cheese.png\\wine_red.png", 0, 0, 32, 32),
	ONION("onion", "Luk", "farm assets\\Fruits and Vegetables\\Onion White.png", 0, 0, 32, 32),
	GARLIC("garlic", "Cesnjak", "farm assets\\Fruits and Vegetables\\Garlic.png", 0, 0, 32, 32),
	HAM("ham", "Sunka", "Pixel_Mart\\white_cheese.png\\sausage_p.png", 0, 0, 32, 32),
	CHOCOLATE("chocolate", "Cokolada", "Ghostpixxells_pixelfood\\26_chocolate.png", 0, 0, 32, 32);
	
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
