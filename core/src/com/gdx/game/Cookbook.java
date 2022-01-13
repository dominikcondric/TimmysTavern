package com.gdx.game;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.XmlReader;
import com.badlogic.gdx.utils.XmlReader.Element;

public class Cookbook implements Disposable {
	private XmlReader xmlReader;
	private HashMap<String, Recipe> cookbook;
	private HashMap<String, Item> ingredientItemsList;
	
	public class Ingredient {
		public Item item;
		public int amount;
		int timeToCook;
		
		Ingredient(Item item, int amount) {
			this.item = item;
			this.amount = amount;
		}
	}
	
	public class Recipe {
		Recipe(String name, int timeToCook, Texture texture) {
			this.name = name;
			this.timeToCook = timeToCook;
			ingredients = new ArrayList<>();
			this.texture = texture;
		}
		
		public final String name;
		public final Texture texture;
		public ArrayList<Ingredient> ingredients;
		public int timeToCook;
	} 
	
	public Cookbook(FileHandle cookbookFile) {
		xmlReader = new XmlReader();
		cookbook = new HashMap<>();
		ingredientItemsList = new HashMap<>();
		
		Element root = xmlReader.parse(cookbookFile);
		loadIngredientItems(root.getChild(0));
		loadRecipes(root.getChild(1));
	}
	
	private void loadIngredientItems(Element root) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			Element recipe = root.getChild(i);
			String ingredientName = recipe.getAttribute("name");
			String ingredientGuiName = recipe.getAttribute("guiName");
			String ingredientTextureFile = recipe.getAttribute("textureFile");
			ingredientItemsList.put(ingredientName, new Item(ingredientName, ingredientGuiName, ingredientTextureFile, 0, 0, 32, 32));
		}
	}
	
	private void loadRecipes(Element root) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			Element recipe = root.getChild(i);
			String recipeName = recipe.getAttribute("name");
			int timeToCook = recipe.getIntAttribute("timeToCook");
			Texture textureFile = new Texture(Gdx.files.internal(recipe.getAttribute("textureFile")));
			Recipe r = new Recipe(recipeName, timeToCook, textureFile);
			for (int j = 0; j < recipe.getChildCount(); ++j) {
				Element ingredient = recipe.getChild(j);
				r.ingredients.add(new Ingredient(ingredientItemsList.get(ingredient.getAttribute("name")), ingredient.getIntAttribute("amount")));
			}
			cookbook.put(recipeName, r);
		}
	}
	
	public final HashMap<String, Recipe> getRecipes() {
		return cookbook;
	}
	
	public Item getIngredientItem(String name) {
		return new Item(ingredientItemsList.get(name));
	}
	
	@Override
	public void dispose() {
		for (Recipe r : cookbook.values()) {
			r.texture.dispose();
		}
	}
}
