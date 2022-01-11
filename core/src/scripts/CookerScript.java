package scripts;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gdx.game.Item;
import com.gdx.game.TimmysTavern;

import components.EntityBits;
import components.GuiComponent;
import components.ScriptComponent;
import components.SoundComponent;
import scripts.PlayerScript.InventorySlot;

public class CookerScript extends Script implements Disposable {
	private ComponentMapper<ScriptComponent> scriptCompMapper = ComponentMapper.getFor(ScriptComponent.class);
	private ArrayList<Recipe> meals;
	private Table recipesList;
	private Table recipeIngredientList;
	private ScrollPane recipeListScrollPane;
	private ScrollPane recipeIngredientListScrollPane;
	Recipe activeRecipe;
	private boolean cookable = false;
	private TextButtonStyle textButtonStyle;
	private LabelStyle whiteLabelStyle;
	private LabelStyle greenLabelStyle;
	private LabelStyle redLabelStyle;
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
		ArrayList<Ingredient> ingredients;
		TextButton button;
	}
	
	public CookerScript(Entity selfEntity) {
		super(selfEntity);
		meals = new ArrayList<Recipe>();
		whiteLabelStyle = new LabelStyle(TimmysTavern.font, Color.WHITE);
		greenLabelStyle = new LabelStyle(TimmysTavern.font, Color.GREEN);
		redLabelStyle = new LabelStyle(TimmysTavern.font, Color.RED);
		
		Recipe recipe = new Recipe();
		recipe.name = "Pita od jabuka";
		meals.add(recipe);
		recipe.ingredients = new ArrayList<>(5);
		recipe.ingredients.add(new Ingredient(Item.APPLE, 10));
		recipe.ingredients.add(new Ingredient(Item.FLOUR, 20));
		recipe.ingredients.add(new Ingredient(Item.SUGAR, 3));
		recipe.ingredients.add(new Ingredient(Item.OIL, 5));
		recipe.ingredients.add(new Ingredient(Item.WATER, 10));
		
		recipe = new Recipe();
		recipe.name = "Kolac od borovnica";
		meals.add(recipe);
		recipe.ingredients = new ArrayList<>(5);
		recipe.ingredients.add(new Ingredient(Item.BLUEBERRY, 40));
		recipe.ingredients.add(new Ingredient(Item.FLOUR, 20));
		recipe.ingredients.add(new Ingredient(Item.WATER, 5));
		recipe.ingredients.add(new Ingredient(Item.OIL, 10));
		recipe.ingredients.add(new Ingredient(Item.BAKING_POWDER, 2));
		
		recipe = new Recipe();
		recipe.name = "Hamburger";
		meals.add(recipe);
		recipe.ingredients = new ArrayList<>(5);
		recipe.ingredients.add(new Ingredient(Item.BREAD, 40));
		recipe.ingredients.add(new Ingredient(Item.BEEF, 20));
		recipe.ingredients.add(new Ingredient(Item.SALAD, 5));
		recipe.ingredients.add(new Ingredient(Item.KETCHUP, 1));
		recipe.ingredients.add(new Ingredient(Item.MAYONNAISE, 1));
		recipe = new Recipe();
		recipe.name = "Govedji gulas";
		meals.add(recipe);
		recipe.ingredients = new ArrayList<>(5);
		recipe.ingredients.add(new Ingredient(Item.BEEF, 2));
		recipe.ingredients.add(new Ingredient(Item.SALT, 2));
		recipe.ingredients.add(new Ingredient(Item.WATER, 5));
		recipe.ingredients.add(new Ingredient(Item.TOMATO, 10));
		recipe.ingredients.add(new Ingredient(Item.OIL, 1));
		recipe.ingredients.add(new Ingredient(Item.POTATO, 3));
		
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
		textButtonStyle.overFontColor = Color.YELLOW;
		textButtonStyle.downFontColor = Color.GOLD;
		
		cookButtonStyle = new TextButtonStyle(textButtonStyle);
		Pixmap grayBackground = new Pixmap(1, 1, Format.RGB888);
		grayBackground.setColor(Color.GRAY);
		grayBackground.fill();
		cookButtonStyle.up = new TextureRegionDrawable(new Texture(grayBackground));
		cookButtonStyle.fontColor = Color.BLACK;
		cookButtonStyle.overFontColor = Color.GOLD;
		
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
				cookable = false;
			}
			
		});
		
		cookButton = new TextButton("Kuhaj!", cookButtonStyle);
		cookButton.align(Align.center);
		cookButton.getLabel().setFontScale(2.f);
		cookButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (cookable) {
					scriptCompMapper.get(self).eventsToDispatch.add("CookingStarted");
				}
			}
		});
		grayBackground.dispose();
		
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
		
		((SoundComponent)self.addAndReturn(new SoundComponent())).addSound("mealComplete", Gdx.files.internal("jingleSounds_Kenney\\OGG\\jingles_SAX\\jingles_SAX16.ogg"), false, false);
	}
	
	private void createIngredientsList() {
		Label recipeNameLabel = new Label(activeRecipe.name, new LabelStyle(TimmysTavern.font, Color.BROWN));
		recipeNameLabel.setAlignment(Align.center);
		recipeNameLabel.setFontScale(2.f);
		recipeIngredientList.add(recipeNameLabel).expandX().colspan(2);
		
		for (Ingredient i : activeRecipe.ingredients) {
			recipeIngredientList.row();
			Label IngredientLabel = new Label(i.item.guiName, whiteLabelStyle);
			IngredientLabel.setFontScale(1.5f);
			IngredientLabel.setAlignment(Align.center);
			recipeIngredientList.add(IngredientLabel).width(recipeIngredientListScrollPane.getWidth() * 0.5f);
			IngredientLabel.setWrap(true);
			Label counter = new Label("0", redLabelStyle);
			counter.setFontScale(1.5f);
			counter.setAlignment(Align.center);
			recipeIngredientList.add(counter).width(recipeIngredientListScrollPane.getWidth() / 2.f);
		}
		
		recipeIngredientList.row();
		recipeIngredientList.add(goBackButton).maxWidth(recipeIngredientList.getWidth() / 4.f);
		recipeIngredientList.add(cookButton).maxWidth(recipeIngredientList.getWidth() / 4.f);
		scriptCompMapper.get(self).eventsToDispatch.add("StartItemChecking");
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
	
	private void checkCurrentIngredients(HashMap<String, InventorySlot> playerInventory) {
		int completedIngredients = 0;
		int counter = 0;
		for (Ingredient i : activeRecipe.ingredients) {
			if (!playerInventory.containsKey(i.item.name)) {
				continue;
			}
			
			int playerIngredientAmount = playerInventory.get(i.item.name).itemCount;  
			Label label = (Label)recipeIngredientList.getChildren().get(2 + counter * 2);
			 if (playerIngredientAmount >= i.amount) {
				 label.setText(Math.min(playerIngredientAmount, i.amount));
				 label.setStyle(greenLabelStyle);
				 ++completedIngredients;
			 } else {
				 label.setText(playerIngredientAmount);
				 label.setStyle(redLabelStyle);
			 }
			 
			 counter++;
		}
		
		if (completedIngredients == activeRecipe.ingredients.size())
			cookable = true;
	}

	@Override
	public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {
		if ((other.getFilterData().categoryBits & EntityBits.PLAYER_B2D_BIT) != 0) {
			if (activeRecipe == null) {
				recipeListScrollPane.setVisible(true);
			} else {
				recipeIngredientListScrollPane.setVisible(true);
				checkCurrentIngredients(((PlayerScript)other.getUserData()).inventory);
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
	public void onEventResponse(Entity receiver, String eventName) {
		if (eventName == "StartItemChecking") {
			scriptCompMapper.get(self).eventsToDispatch.remove(eventName);
			checkCurrentIngredients(((PlayerScript)receiver.getComponent(ScriptComponent.class).script).inventory);
		} else if (eventName == "CookingStarted") {
			scriptCompMapper.get(self).eventsToDispatch.remove(eventName);
			activeRecipe = null;
			cookable = false;
			recipeIngredientList.clear();
			recipeListScrollPane.setVisible(true);
			recipeIngredientListScrollPane.setVisible(false);
			self.getComponent(SoundComponent.class).getSoundEffect("mealComplete").shouldPlay = true;
		}
	}

	@Override
	public void dispose() {
		((TextureRegionDrawable)goBackButton.getStyle().up).getRegion().getTexture().dispose();
		((TextureRegionDrawable)cookButton.getStyle().up).getRegion().getTexture().dispose();
	}
}
