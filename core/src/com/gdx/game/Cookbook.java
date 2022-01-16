package com.gdx.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

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
	private Random randomNumberGenerator;
	
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
		public final int timeToCook;
	} 
	
	Cookbook() {
		xmlReader = new XmlReader();
		cookbook = new HashMap<>();
		ingredientItemsList = new HashMap<>();
		randomNumberGenerator = new Random();
		
		Element root = xmlReader.parse(Gdx.files.internal("Cookbook.xml"));
		loadIngredientItems(root.getChild(0));
		loadRecipes(root.getChild(1), false);
		if (Gdx.files.local("CustomAssets\\CustomCookbook.xml").exists()) {
			Element customRoot = xmlReader.parse(Gdx.files.internal("CustomAssets\\CustomCookbook.xml"));
			loadRecipes(customRoot.getChild(0), true);
		}
	}
	
	public boolean itemExists(String itemName) {
		return ingredientItemsList.containsKey(itemName);
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
	
	private void loadRecipes(Element root, boolean local) {
		for (int i = 0; i < root.getChildCount(); ++i) {
			Element recipe = root.getChild(i);
			if (!recipe.hasAttribute("name") || !recipe.hasAttribute("textureFile") || !recipe.hasAttribute("timeToCook"))
				continue;
			
			String recipeName = recipe.getAttribute("name");
			String textureFileName = recipe.getAttribute("textureFile");
			int timeToCook = recipe.getIntAttribute("timeToCook");
			Texture textureFile;
			if (!local) {
				textureFile = new Texture(Gdx.files.internal(textureFileName));
			} else {
				FileHandle textureFileHandle = Gdx.files.local("CustomAssets\\" + textureFileName);
				if (!textureFileHandle.exists())
					continue;
				
				textureFile = new Texture(textureFileHandle);
			}
				
			Recipe r = new Recipe(recipeName, timeToCook, textureFile);
			int successfulIngredientCount = 0;
			for (int j = 0; j < recipe.getChildCount(); ++j) {
				Element ingredient = recipe.getChild(j);
				if (ingredientItemsList.containsKey(ingredient.getAttribute("name"))) {
					r.ingredients.add(new Ingredient(ingredientItemsList.get(ingredient.getAttribute("name")), ingredient.getIntAttribute("amount")));
					successfulIngredientCount++;
				}
			}
			
			if (successfulIngredientCount > 0)
				cookbook.put(recipeName, r);
		}
	}
	
	public final HashMap<String, Recipe> getRecipes() {
		return cookbook;
	}
	
	public Item getIngredientItem(String name) {
		return new Item(ingredientItemsList.get(name));
	}
	
	public final Item getRandomRecipeItem() {
		int randomRecipeIndex = 1 + randomNumberGenerator.nextInt(cookbook.size());
		Recipe returnRecipe = null;
		Iterator<Recipe> recipeIterator = cookbook.values().iterator();
		for (int i = 0; i < randomRecipeIndex; ++i) {
			returnRecipe = recipeIterator.next();
		}
		
		return new Item(returnRecipe.name, "", returnRecipe.texture.getTextureData().toString(), 0, 0, 32, 32);
	}
	
	@Override
	public void dispose() {
		for (Recipe r : cookbook.values()) {
			r.texture.dispose();
		}
	}
}
