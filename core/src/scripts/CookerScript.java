package scripts;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gdx.game.Item;
import com.gdx.game.TimmysTavern;

import components.EntityBits;
import components.GuiComponent;

public class CookerScript extends Script implements Disposable {
	private ArrayList<Recipe> meals;
	private Table recipesList;
	private Table recipeIngredientList;
	private ScrollPane recipeListScrollPane;
	private ScrollPane recipeIngredientListScrollPane;
	private HashMap<String, Ingredient> itemsOnCooker;
	private Recipe activeRecipe;
	private int completedItems = 0;
	private boolean cookable = false;
	private TextButtonStyle textButtonStyle;
	private LabelStyle labelStyle;
	private TextButton cookButton;
	private TextButton goBackButton;
	private TextButtonStyle cookButtonStyle;
	
	class Ingredient {
		Item item;
		int amount;
		
		Ingredient(Item item, int amount) {
			this.item = item;
			this.amount = amount;
		}
	}
	
	class Recipe {
		String name;
		ArrayList<Ingredient> Ingredients;
		TextButton button;
	}
	
	public CookerScript(Entity selfEntity) {
		super(selfEntity);
		meals = new ArrayList<Recipe>();
		itemsOnCooker = new HashMap<String, Ingredient>();
		labelStyle = new LabelStyle(TimmysTavern.font, Color.WHITE);
		
		Recipe recipe = new Recipe();
		recipe.name = "Pita od jabuka";
		meals.add(recipe);
		recipe.Ingredients = new ArrayList<>();
		recipe.Ingredients.add(new Ingredient(Item.APPLE, 10));
		recipe.Ingredients.add(new Ingredient(Item.FLOUR, 20));
		recipe.Ingredients.add(new Ingredient(Item.SUGAR, 3));
		recipe.Ingredients.add(new Ingredient(Item.OIL, 5));
		recipe.Ingredients.add(new Ingredient(Item.WATER, 10));
		
		recipe = new Recipe();
		recipe.name = "Kolac od borovnica";
		meals.add(recipe);
		recipe.Ingredients = new ArrayList<>();
		recipe.Ingredients.add(new Ingredient(Item.BLUEBERRY, 40));
		recipe.Ingredients.add(new Ingredient(Item.FLOUR, 20));
		recipe.Ingredients.add(new Ingredient(Item.WATER, 5));
		recipe.Ingredients.add(new Ingredient(Item.OIL, 10));
		recipe.Ingredients.add(new Ingredient(Item.BAKING_POWDER, 2));
//		
//		recipe = new Recipe();
//		recipe.name = "Hamburger";
//		meals.put(recipe.name, recipe);
//		recipe.Ingredients = new HashMap<>();
//		recipe.Ingredients.put("bread", new Ingredient("bread", 40));
//		recipe.Ingredients.put("meat", new Ingredient("meat", 20));
//		recipe.Ingredients.put("salad", new Ingredient("salad", 5));
//		recipe.Ingredients.put("ketchup", new Ingredient("ketchup", 10));
//		recipe.Ingredients.put("mayonnaise", new Ingredient("mayonnaise", 2));
		
		
		final float inventorySlotSize = Gdx.graphics.getWidth() / 30.f;
		final float startX = inventorySlotSize * 2;
		final float startY = Gdx.graphics.getHeight() - inventorySlotSize * 6;
		Texture borderTexture = new Texture(Gdx.files.internal("InventoryItemBorder.png"));
		
		recipesList = new Table();
		recipeIngredientList = new Table();
		recipesList.setBackground(new TextureRegionDrawable(new TextureRegion(borderTexture)));
		recipeIngredientList.setBackground(new TextureRegionDrawable(new TextureRegion(borderTexture)));
		textButtonStyle = new TextButtonStyle();
		textButtonStyle.font = TimmysTavern.font;
		textButtonStyle.fontColor = Color.WHITE;
		textButtonStyle.overFontColor = Color.BLACK;
		
		cookButtonStyle = new TextButtonStyle(textButtonStyle);
		Pixmap grayBackground = new Pixmap(1, 1, Format.RGB888);
		grayBackground.setColor(Color.GRAY);
		grayBackground.fill();
		cookButtonStyle.up = new TextureRegionDrawable(new Texture(grayBackground));
		cookButtonStyle.fontColor = Color.BLACK;
		cookButtonStyle.overFontColor = Color.GOLD;
		grayBackground.dispose();
		
		goBackButton = new TextButton("Back", cookButtonStyle);
		goBackButton.align(Align.center);
		goBackButton.getLabel().setFontScale(2.f);
		goBackButton.addListener(new ClickListener(Buttons.LEFT) {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				recipeIngredientList.clear();
				recipeListScrollPane.setVisible(true);
				recipeIngredientListScrollPane.setVisible(false);
				activeRecipe = null;
				
			}
			
		});
		
		cookButton = new TextButton("Kuhaj!", cookButtonStyle);
		cookButton.align(Align.center);
		cookButton.getLabel().setFontScale(2.f);
		
		for (Recipe r : meals) {
			recipesList.row();
			TextButton tb = new TextButton(r.name, textButtonStyle);
			r.button = tb;
			tb.getLabel().setFontScale(2.f);
			tb.align(Align.center);
			recipesList.add(tb).expandX();
		}
		
		recipeListScrollPane = new ScrollPane(recipesList);
		recipeListScrollPane.setVisible(false);
		recipeListScrollPane.setPosition(startX, startY);
		recipeListScrollPane.setSize(inventorySlotSize * 5, inventorySlotSize * 5);
		recipeListScrollPane.setScrollBarPositions(false, true);
		
		recipeIngredientListScrollPane = new ScrollPane(recipeIngredientList);
		recipeIngredientListScrollPane.setVisible(false);
		recipeIngredientListScrollPane.setPosition(startX, startY);
		recipeIngredientListScrollPane.setSize(inventorySlotSize * 5, inventorySlotSize * 5);
		recipeIngredientListScrollPane.setScrollBarPositions(false, true);
		
		GuiComponent guiComponent = new GuiComponent();
		guiComponent.actors.addActor(recipeListScrollPane);
		guiComponent.actors.addActor(recipeIngredientListScrollPane);
		self.add(guiComponent);
	}
	
	private void createIngredientsList() {
		Label recipeNameLabel = new Label(activeRecipe.name, new LabelStyle(TimmysTavern.font, Color.BROWN));
		recipeNameLabel.setAlignment(Align.center);
		recipeNameLabel.setFontScale(2.f);
		recipeIngredientList.add(recipeNameLabel).expandX().colspan(2);
		
		for (Ingredient i : activeRecipe.Ingredients) {
			recipeIngredientList.row();
			Label IngredientLabel = new Label(i.item.guiName, labelStyle);
			IngredientLabel.setFontScale(1.5f);
			IngredientLabel.setAlignment(Align.center);
			recipeIngredientList.add(IngredientLabel).width(recipeIngredientListScrollPane.getWidth() * 0.5f);
			IngredientLabel.setWrap(true);
			Label counter = new Label("0", labelStyle);
			counter.setFontScale(1.5f);
			counter.setAlignment(Align.center);
			recipeIngredientList.add(counter).width(recipeIngredientListScrollPane.getWidth() / 2.f);
		}
		
		recipeIngredientList.row();
		recipeIngredientList.add(goBackButton).maxWidth(recipeIngredientList.getWidth() / 4.f);
		recipeIngredientList.add(cookButton).maxWidth(recipeIngredientList.getWidth() / 4.f);
	}

	@Override
	public void update(float deltaTime) {
		if (activeRecipe == null) {
			for (Recipe r : meals) {
				if (r.button.getClickListener().isPressed()) {
					activeRecipe = r;
					recipeListScrollPane.setVisible(false);
					createIngredientsList();
					recipeIngredientListScrollPane.setVisible(true);
					break;
				}
			}
		}
	}

	@Override
	public void onEventReceived(Entity sender, String eventName) {
		if (eventName.contentEquals("TakeItems")) {
			if (activeRecipe == null || cookable)
				return;
			
			if (completedItems == activeRecipe.Ingredients.size())
				cookable = true;
		} 
	}

	@Override
	public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {
		if ((other.getFilterData().categoryBits & EntityBits.PLAYER_B2D_BIT) != 0) {
			if (activeRecipe == null) {
				recipeListScrollPane.setVisible(true);
			} else {
				recipeIngredientListScrollPane.setVisible(true);
			}
		}
	}

	@Override
	public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {
		if ((other.getFilterData().categoryBits & EntityBits.PLAYER_B2D_BIT) != 0) {
			if (activeRecipe == null) {
				recipeListScrollPane.setVisible(false);
			} else {
				recipeIngredientListScrollPane.setVisible(false);
			}
		}
	}

	@Override
	public void dispose() {
		((TextureRegionDrawable)goBackButton.getStyle().up).getRegion().getTexture().dispose();
		((TextureRegionDrawable)cookButton.getStyle().up).getRegion().getTexture().dispose();
	}
}
