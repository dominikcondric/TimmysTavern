package com.gdx.game;

public enum Item {
	CRANBERRY("cranberry", 0.01f),
	BLUEBERRY("blueberry", 0.01f),
	APPLE("apple", 0.1f),
	BREAD("bread", 0.8f),
	MUSHROOM("mushroom", 0.05f);
	
	private Item(String itemName, float itemWeight) {
		this.name = itemName;
		this.weight = itemWeight;
	}
	
	public final String name;
	public final float weight; // Weight in kilograms
}
