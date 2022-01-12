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
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
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
	private ComponentMapper<GuiComponent> guiCompMapper = ComponentMapper.getFor(GuiComponent.class);
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
	private Label cookingCounterLabel;
	private float cookingCounter = 0f;
	private ImageButton takeMealButton;
	
	class Ingredient {
		Item item;
		int amount;
		int timeToCook;
		
		Ingredient(Item item, int amount) {
			this.item = item;
			this.amount = amount;
		}
	}
	
	class Recipe {
		Recipe(String name, int timeToCook, Texture texture) {
			this.name = name;
			this.timeToCook = timeToCook;
			ingredients = new ArrayList<>();
			this.texture = texture;
		}
		
		final String name;
		final Texture texture;
		ArrayList<Ingredient> ingredients;
		TextButton button;
		int timeToCook;
	}
	
	public CookerScript(Entity selfEntity) {
		super(selfEntity);
		meals = new ArrayList<Recipe>();
		whiteLabelStyle = new LabelStyle(TimmysTavern.font, Color.WHITE);
		greenLabelStyle = new LabelStyle(TimmysTavern.font, Color.GREEN);
		redLabelStyle = new LabelStyle(TimmysTavern.font, Color.RED);
		cookingCounterLabel = new Label("", whiteLabelStyle);
		cookingCounterLabel.setFontScale(3.f);
		cookingCounterLabel.setAlignment(Align.center);
		
		Recipe recipe = new Recipe("Pita od jabuka", 10, new Texture(Gdx.files.internal("Ghostpixxells_pixelfood\\05_apple_pie.png")));
		meals.add(recipe);
		recipe.ingredients.add(new Ingredient(Item.APPLE, 10));
//		recipe.ingredients.add(new Ingredient(Item.FLOUR, 20));
//		recipe.ingredients.add(new Ingredient(Item.SUGAR, 3));
//		recipe.ingredients.add(new Ingredient(Item.OIL, 5));
//		recipe.ingredients.add(new Ingredient(Item.WATER, 10));
//		
		recipe = new Recipe("Kolac od borovnica i cokolade", 30, new Texture(Gdx.files.internal("Ghostpixxells_pixelfood\\30_chocolatecake.png")));
		meals.add(recipe);
		recipe.ingredients.add(new Ingredient(Item.BLUEBERRY, 40));
		recipe.ingredients.add(new Ingredient(Item.FLOUR, 20));
		recipe.ingredients.add(new Ingredient(Item.WATER, 5));
		recipe.ingredients.add(new Ingredient(Item.OIL, 10));
		recipe.ingredients.add(new Ingredient(Item.BAKING_POWDER, 2));
		
		recipe = new Recipe("Hamburger", 3, new Texture(Gdx.files.internal("Ghostpixxells_pixelfood\\15_burger.png")));
		meals.add(recipe);
		recipe.ingredients.add(new Ingredient(Item.BREAD, 40));
		recipe.ingredients.add(new Ingredient(Item.BEEF, 20));
		recipe.ingredients.add(new Ingredient(Item.SALAD, 5));
		recipe.ingredients.add(new Ingredient(Item.KETCHUP, 1));
		recipe.ingredients.add(new Ingredient(Item.MAYONNAISE, 1));
		
		recipe = new Recipe("Govedji gulas", 30, new Texture(Gdx.files.internal("Ghostpixxells_pixelfood\\41_eggsalad_bowl.png")));
		meals.add(recipe);
		recipe.ingredients.add(new Ingredient(Item.BEEF, 2));
		recipe.ingredients.add(new Ingredient(Item.SALT, 2));
		recipe.ingredients.add(new Ingredient(Item.WATER, 5));
		recipe.ingredients.add(new Ingredient(Item.TOMATO, 10));
		recipe.ingredients.add(new Ingredient(Item.OIL, 1));
		recipe.ingredients.add(new Ingredient(Item.POTATO, 3));
		
		final float guiSize = Gdx.graphics.getWidth() / 6.f;
		final float startX = guiSize / 3f;
		final float startY = Gdx.graphics.getHeight() - guiSize * 1.2f;
		Texture borderTexture = new Texture(Gdx.files.internal("InventoryItemBorder.png"));
		
		recipesList = new Table();
		recipesList.setFillParent(true);
		recipesList.center();
		recipeIngredientList = new Table();
		recipeIngredientList.center();
		recipeIngredientList.setFillParent(true);
		recipesList.setBackground(new TextureRegionDrawable(new TextureRegion(borderTexture)));
		recipeIngredientList.setBackground(new TextureRegionDrawable(new TextureRegion(borderTexture)));
		textButtonStyle = new TextButtonStyle();
		textButtonStyle.font = TimmysTavern.font;
		textButtonStyle.fontColor = Color.WHITE;
		textButtonStyle.overFontColor = Color.YELLOW;
		textButtonStyle.downFontColor = Color.GOLD;
		
		ImageButtonStyle imageButtonStyle = new ImageButtonStyle();
		takeMealButton = new ImageButton(imageButtonStyle);
		takeMealButton.align(Align.center);
		takeMealButton.pad(30f);
		takeMealButton.addListener(new ClickListener(Buttons.LEFT) {

			@Override
			public void clicked(InputEvent event, float x, float y) {
				recipeIngredientList.clear();
				GuiComponent guiComp = guiCompMapper.get(self);
				guiComp.actors.clear();
				guiComp.actors.addActor(recipeListScrollPane);
				activeRecipe = null;
				((TextureRegionDrawable)takeMealButton.getStyle().up).getRegion().getTexture().dispose();
				self.getComponent(SoundComponent.class).getSoundEffect("mealTake").shouldPlay = true;
				cookingCounter = 0f;
			}
			
		});
		
		cookButtonStyle = new TextButtonStyle(textButtonStyle);
		cookButtonStyle.fontColor = Color.LIME;
		cookButtonStyle.overFontColor = Color.GREEN;
		
		goBackButton = new TextButton("Natrag", cookButtonStyle);
		goBackButton.align(Align.center);
		goBackButton.pad(5f);
		goBackButton.getLabel().setFontScale(2.f);
		goBackButton.addListener(new ClickListener(Buttons.LEFT) {
			
			@Override
			public void clicked(InputEvent event, float x, float y) {
				recipeIngredientList.clear();
				GuiComponent guiComp = guiCompMapper.get(self);
				guiComp.actors.clear();
				guiComp.actors.addActor(recipeListScrollPane);
				activeRecipe = null;
				cookable = false;
			}
			
		});
		
		cookButton = new TextButton("Kuhaj!", cookButtonStyle);
		cookButton.align(Align.center);
		cookButton.pad(5f);
		cookButton.getLabel().setFontScale(2.f);
		cookButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				if (cookable) {
					scriptCompMapper.get(self).eventsToDispatch.add("CookingStarted");
				}
			}
		});
		
		for (Recipe r : meals) {
			recipesList.row();
			TextButton tb = new TextButton(r.name, textButtonStyle);
			r.button = tb;
			tb.getLabel().setFontScale(2.f);
			tb.align(Align.center);
			tb.getLabel().setWrap(true);
			recipesList.add(tb).fill().expandX();
			tb.addListener(new ClickListener() {

				@Override
				public void clicked(InputEvent event, float x, float y) {
					for (Recipe r : meals) {
						if (r.button == event.getListenerActor()) {
							activeRecipe = r;
							recipeIngredientList.clear();
							createIngredientsList();
							GuiComponent guiComp = guiCompMapper.get(self);
							guiComp.actors.clear();
							guiComp.actors.addActor(recipeIngredientListScrollPane);
							break;
						}
					}
				}
				
			});
			
		}
		
		recipeListScrollPane = new ScrollPane(recipesList);
		recipeListScrollPane.setFillParent(true);
		recipeListScrollPane.setScrollBarPositions(false, true);
		
		recipeIngredientListScrollPane = new ScrollPane(recipeIngredientList);
		recipeIngredientListScrollPane.setFillParent(true);
		recipeIngredientListScrollPane.setScrollBarPositions(false, true);
		
		GuiComponent guiComponent = new GuiComponent();
		guiComponent.actors.setPosition(startX, startY);
		guiComponent.actors.setSize(guiSize, guiSize);
		guiComponent.actors.addActor(recipeListScrollPane);
		guiComponent.actors.setVisible(false);
		self.add(guiComponent);
		
		SoundComponent soundComp = (SoundComponent)self.addAndReturn(new SoundComponent());
		soundComp.addSound("mealTake", Gdx.files.internal("jingleSounds_Kenney\\OGG\\jingles_SAX\\jingles_SAX16.ogg"), false, false);
		soundComp.addSound("mealComplete", Gdx.files.internal("RPGsounds_Kenney\\OGG\\metalLatch.ogg"), false, false);
	}
	
	private void createIngredientsList() {
		Label recipeNameLabel = new Label(activeRecipe.name, new LabelStyle(TimmysTavern.font, Color.BROWN));
		recipeNameLabel.setWrap(true);
		recipeNameLabel.setAlignment(Align.center);
		recipeNameLabel.setFontScale(2.f);
		recipeIngredientList.add(recipeNameLabel).colspan(2).grow().center();
		
		for (Ingredient i : activeRecipe.ingredients) {
			recipeIngredientList.row();
			Label ingredientLabel = new Label(i.item.guiName, whiteLabelStyle);
			ingredientLabel.setWrap(true);
			ingredientLabel.setFontScale(1.5f);
			ingredientLabel.setAlignment(Align.center);
			recipeIngredientList.add(ingredientLabel).grow().center();
			Label counter = new Label("0", redLabelStyle);
			counter.setFontScale(1.5f);
			counter.setAlignment(Align.center);
			recipeIngredientList.add(counter).maxWidth(recipeIngredientList.getWidth() / 4.f);
		}
		
		recipeIngredientList.row();
		recipeIngredientList.add(goBackButton).maxWidth(recipeIngredientList.getWidth() / 4.f).grow();
		recipeIngredientList.add(cookButton).maxWidth(recipeIngredientList.getWidth() / 4.f).grow();
		scriptCompMapper.get(self).eventsToDispatch.add("StartItemChecking");
		recipeIngredientList.invalidateHierarchy();
		recipeIngredientList.layout();
	}

	@Override
	public void update(float deltaTime) {
		if (cookingCounter > 0) {
			cookingCounterLabel.setText(Integer.toString((int)Math.ceil(cookingCounter)));
			cookingCounter -= deltaTime;
			if (cookingCounter < 0) {
				recipeIngredientList.clear();
				Label recipeNameLabel = new Label(activeRecipe.name, new LabelStyle(TimmysTavern.font, Color.BROWN));
				recipeNameLabel.setAlignment(Align.center);
				recipeNameLabel.setWrap(true);
				recipeNameLabel.setFontScale(2.f);
				recipeIngredientList.add(recipeNameLabel).expandX().fill();
				recipeIngredientList.row();
				takeMealButton.getStyle().up = new TextureRegionDrawable(activeRecipe.texture);
				recipeIngredientList.add(takeMealButton);
				self.getComponent(SoundComponent.class).getSoundEffect("mealComplete").shouldPlay = true;
			}
		}
	}
	
	private void checkCurrentIngredients(HashMap<String, InventorySlot> playerInventory) {
		int completedIngredients = 0;
		int counter = 0;
		for (Ingredient i : activeRecipe.ingredients) {
			int playerIngredientAmount = 0;
			if (playerInventory.containsKey(i.item.name)) {
				playerIngredientAmount = playerInventory.get(i.item.name).itemCount;  
			}
			
			Label label = (Label)recipeIngredientList.getChildren().get(2 + counter * 2);
			label.setText(Integer.toString(Math.min(playerIngredientAmount, i.amount)) + " / " + i.amount);
			 if (playerIngredientAmount >= i.amount) {
				 label.setStyle(greenLabelStyle);
				 ++completedIngredients;
			 } else {
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
			guiCompMapper.get(this.self).actors.setVisible(true);
			if (activeRecipe != null && cookingCounter == 0f) 
				checkCurrentIngredients(((PlayerScript)other.getUserData()).inventory);
		}
	}

	@Override
	public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {
		if ((other.getFilterData().categoryBits & EntityBits.PLAYER_B2D_BIT) != 0) {
			guiCompMapper.get(this.self).actors.setVisible(false);
		}
	}
	
	@Override
	public void onEventResponse(Entity receiver, String eventName) {
		if (eventName == "StartItemChecking") {
			scriptCompMapper.get(self).eventsToDispatch.remove(eventName);
			checkCurrentIngredients(((PlayerScript)receiver.getComponent(ScriptComponent.class).script).inventory);
		} else if (eventName == "CookingStarted") {
			scriptCompMapper.get(self).eventsToDispatch.remove(eventName);
			recipeIngredientList.clear();
			Label recipeNameLabel = new Label(activeRecipe.name, new LabelStyle(TimmysTavern.font, Color.BROWN));
			recipeNameLabel.setAlignment(Align.center);
			recipeNameLabel.setFontScale(2.f);
			recipeIngredientList.add(recipeNameLabel).expand();
			recipeIngredientList.row();
			recipeIngredientList.add(cookingCounterLabel).grow().center();
			cookingCounter = activeRecipe.timeToCook;
			cookable = false;
		}
	}

	@Override
	public void dispose() {
		for (Recipe r : meals) {
			r.texture.dispose();
		}
	}
}
