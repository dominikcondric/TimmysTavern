package scripts;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Stack;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.gdx.game.Cookbook.Ingredient;
import com.gdx.game.Cookbook.Recipe;
import com.gdx.game.Item;
import com.gdx.game.TimmysTavern;

import components.AnimationComponent;
import components.GuiComponent;
import components.PhysicsComponent;
import components.ScriptComponent;
import components.SoundComponent;

public class PlayerScript extends Script {
	private int lastMovingDirection = Keys.DOWN;
	private ComponentMapper<PhysicsComponent> physicsComponentMapper = ComponentMapper.getFor(PhysicsComponent.class);
	private ComponentMapper<AnimationComponent> animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);
	private ComponentMapper<GuiComponent> guiComponentMapper = ComponentMapper.getFor(GuiComponent.class);
	private ComponentMapper<SoundComponent> soundComponentMapper = ComponentMapper.getFor(SoundComponent.class);
	private ComponentMapper<ScriptComponent> scriptComponentMapper = ComponentMapper.getFor(ScriptComponent.class);
	HashMap<String, InventorySlot> inventory;
	private boolean inventoryOpen = false;
	private final int MAX_INVENTORY_ITEMS = 25;
	private String activeScene;
	private Stack<Integer> availableSlots;
	
	class InventorySlot {
		public InventorySlot(int slot, int count) {
			slotNumber = slot;
			itemCount = count;
		}
		
		int itemCount;
		int slotNumber;
	}
	
	public PlayerScript(Entity selfEntity) {
		super(selfEntity);
		inventory = new HashMap<String, InventorySlot>();
		availableSlots = new Stack<>();
		for (int i = 24; i >= 0; --i)
			availableSlots.push(i);
		
		GuiComponent playerInventory = new GuiComponent();
		playerInventory.actors.setVisible(false);
		Pixmap backgroundBlack = new Pixmap(1, 1, Format.RGBA8888);
		backgroundBlack.setColor(0.f, 0.f, 0.f, 0.2f);
		backgroundBlack.fill();
		Texture borderTexture = new Texture(Gdx.files.internal("InventoryItemBorder.png"));
		
		final float inventorySlotSize = Gdx.graphics.getWidth() / 30.f;
		final float firstImageX = Gdx.graphics.getWidth() - inventorySlotSize * 6;
		final float firstImageY = Gdx.graphics.getHeight() - inventorySlotSize * 6;
		
		TextButtonStyle tbs = new TextButtonStyle();
		tbs.font = TimmysTavern.font;
		for (int i = 4; i >= 0; --i) {
			for (int j = 0; j < 5; ++j) {
				Group inventoryItemGroup = new Group();
				inventoryItemGroup.setPosition(firstImageX + j * inventorySlotSize, firstImageY + i * inventorySlotSize);
				inventoryItemGroup.setSize(inventorySlotSize, inventorySlotSize);
				Image border = new Image(borderTexture);
				border.setFillParent(true);
				TextButton counter = new TextButton("", tbs);
				counter.getLabel().setFontScale(1.5f);
				counter.getLabel().setAlignment(Align.bottomLeft);
				counter.setFillParent(true);
				counter.setVisible(false);
				counter.align(Align.bottomLeft);
				counter.addListener(new ClickListener(Buttons.RIGHT) {
					
					@Override
					public void clicked(InputEvent event, float x, float y) {
						TextButton button = (TextButton)event.getListenerActor();
						Actor parent = button.getParent();
						Group mainGroup = self.getComponent(GuiComponent.class).actors;
						int slotIndexToRemove;
						for (slotIndexToRemove = 0; slotIndexToRemove < MAX_INVENTORY_ITEMS; ++slotIndexToRemove) {
							if (mainGroup.getChild(slotIndexToRemove) == parent) {
								break;
							}
						}
						
						String entryToRemove = null;
						for (Entry<String, InventorySlot> entry : inventory.entrySet()) {
							if (entry.getValue().slotNumber == slotIndexToRemove) {
								entryToRemove = entry.getKey();

							}
						}
						
						Item item = new Item(entryToRemove, "", "", 0, 0, 32, 32);
						updateInventory(item, 0);
					}
					
				});
				Image emptyImage = new Image();
				inventoryItemGroup.addActor(border);
				inventoryItemGroup.addActor(emptyImage);
				inventoryItemGroup.addActor(counter);
				playerInventory.actors.addActor(inventoryItemGroup);
			}
		}
		
		backgroundBlack.dispose();
		self.add(playerInventory);
	}
	
	@Override
	public void update(float deltaTime) {
		if (Gdx.input.isKeyJustPressed(Keys.I)) {
			inventoryOpen = !inventoryOpen;
			guiComponentMapper.get(self).actors.setVisible(inventoryOpen);
			soundComponentMapper.get(self).getSoundEffect("inventoryOpen").shouldPlay = true;
		}
		
		Body body = physicsComponentMapper.get(self).body;
		
		body.setLinearVelocity(0.f, 0.f);
		
		int newMovingDirection = 0;
		int directionKeys[] = { Keys.UP, Keys.DOWN, Keys.RIGHT, Keys.LEFT };
		
		if (lastMovingDirection != 0) {
			for (int i = 0; i < directionKeys.length; ++i) {
				if (directionKeys[i] == lastMovingDirection) {
					directionKeys[i] = directionKeys[0];
					directionKeys[0] = lastMovingDirection;
					break;
				}
			}
		}
		
		for (int key : directionKeys) {
			if (Gdx.input.isKeyPressed(key)) {
				switch (key) {
					case Keys.RIGHT:
						body.setLinearVelocity(4f, 0f);
						newMovingDirection = Keys.RIGHT;
						break;
					case Keys.LEFT:
						body.setLinearVelocity(-4f, 0f);
						newMovingDirection = Keys.LEFT;
						break;
					case Keys.UP:
						body.setLinearVelocity(0f, 4f);
						newMovingDirection = Keys.UP;
						break;
					case Keys.DOWN:
						body.setLinearVelocity(0f, -4f);
						newMovingDirection = Keys.DOWN;
						break;
				}
				
				break;
			}
		}
		
		if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
			body.setLinearVelocity(body.getLinearVelocity().scl(4.f));
		
		AnimationComponent animationComponent = animationComponentMapper.get(self);
		if (newMovingDirection != 0 && lastMovingDirection != newMovingDirection) {
			switch (newMovingDirection) {
				case Keys.DOWN:
					animationComponent.setActiveAnimation("WalkDown", true);
					break;
				case Keys.UP:
					animationComponent.setActiveAnimation("WalkUp", true);
					break;
				case Keys.RIGHT:
					animationComponent.setActiveAnimation("WalkRight", true);
					break;
				case Keys.LEFT:
					animationComponent.setActiveAnimation("WalkLeft", true);
					break;
			}
		} else if (newMovingDirection == 0 && lastMovingDirection != 0) {
			switch (lastMovingDirection) {
				case Keys.DOWN:
					animationComponent.setActiveAnimation("IdleDown", true);
					break;
				case Keys.UP:
					animationComponent.setActiveAnimation("IdleUp", true);
					break;
				case Keys.RIGHT:
					animationComponent.setActiveAnimation("IdleRight", true);
					break;
				case Keys.LEFT:
					animationComponent.setActiveAnimation("IdleLeft", true);
					break;
			}
		}
		
		lastMovingDirection = newMovingDirection;
	}
	
	@Override
	public void onEventReceived(Entity sender, String eventName) {
		if (eventName.contentEquals("SceneChanged")) {
			switch (((DoorScript)sender.getComponent(ScriptComponent.class).script).newSceneName) {
				case "village":
					if (activeScene.contentEquals("tavern")) {
						physicsComponentMapper.get(self).body.setTransform(new Vector2(49.f, 28.f),  0.f);
						animationComponentMapper.get(self).setActiveAnimation("IdleDown", false);
					} else if (activeScene.contentEquals("shop")) {
						physicsComponentMapper.get(self).body.setTransform(new Vector2(71.f, 35.f),  0.f);
						animationComponentMapper.get(self).setActiveAnimation("IdleDown", false);
					}
					activeScene = "village";
					break;
				
				case "tavern":
					physicsComponentMapper.get(self).body.setTransform(new Vector2(10.f, 2.f),  0.f);
					animationComponentMapper.get(self).setActiveAnimation("IdleUp", false);
					activeScene = "tavern";
					break;
					
				case "shop":
					physicsComponentMapper.get(self).body.setTransform(new Vector2(3.f, 2.f),  0.f);
					animationComponentMapper.get(self).setActiveAnimation("IdleUp", false);
					activeScene = "shop";
					break;
			}
			
		} else if (eventName.contentEquals("PickItem")) {
			Script script = scriptComponentMapper.get(sender).script;
			Item pickedItem = null;
			if (script instanceof CookerScript) {
				Recipe r = ((CookerScript) script).activeRecipe;
				pickedItem = new Item(r.name, "", r.texture.getTextureData().toString(), 0, 0, 32, 32);
			} else if (script instanceof ItemScript) {
				pickedItem = ((ItemScript) script).item;
			}
			
			InventorySlot slot = inventory.get(pickedItem.name);
			if (inventory.size() == MAX_INVENTORY_ITEMS && slot == null) {
				return;
			}
			int newAmount = slot == null ? 1 : slot.itemCount + 1;
			updateInventory(pickedItem, newAmount);
			scriptComponentMapper.get(self).eventsToDispatch.add("ItemPicked");
		} else if (eventName == "CookingStarted") {
			Recipe activeReceipt = ((CookerScript)scriptComponentMapper.get(sender).script).activeRecipe;
			for (Ingredient i : activeReceipt.ingredients) {
				updateInventory(i.item, inventory.get(i.item.name).itemCount - i.amount);
			}
		} else if (eventName == "NPCMealTake") {
			NPCCustomerScript npcScript = (NPCCustomerScript)scriptComponentMapper.get(sender).script;
			Item mealName = npcScript.orderedRecipe;
			InventorySlot slot = inventory.get(mealName.name);
			if (slot == null) {
				return;
			} else { 
				updateInventory(mealName, inventory.get(mealName.name).itemCount - 1);
				npcScript.orderedRecipe = null;
			}
		}
	}
	
	@Override
	public void onEventResponse(Entity receiver, String eventName) {
		if (eventName == "ItemPicked") {
			scriptComponentMapper.get(self).eventsToDispatch.remove(eventName);
		}
	}

	private void updateInventory(Item updatingItem, int newAmount) {
		GuiComponent inventoryGui = guiComponentMapper.get(self);
		if (inventory.containsKey(updatingItem.name)) {
			InventorySlot slot = inventory.get(updatingItem.name);
			slot.itemCount = newAmount;
			TextButton slotLabel = (TextButton)((Group)inventoryGui.actors.getChild(slot.slotNumber)).getChild(2);
			if (slot.itemCount == 0) {
				availableSlots.push(slot.slotNumber);
				slotLabel.setVisible(false);
				Image itemImage = (Image)((Group)inventoryGui.actors.getChild(slot.slotNumber)).getChild(1);
				((TextureRegionDrawable)itemImage.getDrawable()).getRegion().getTexture().dispose();
				itemImage.setDrawable(null);
				inventory.remove(updatingItem.name);
			} else if (slot.itemCount >= 1) {
				slotLabel.setVisible(true);
				slotLabel.setText(Integer.toString(slot.itemCount));
			}
		} else {
			int availableSlot = availableSlots.pop();
			Group inventorySlot = ((Group)inventoryGui.actors.getChild(availableSlot));
			inventory.put(updatingItem.name, new InventorySlot(availableSlot, newAmount));
			Image itemImage = (Image)inventorySlot.getChild(1);
			Image borderImage =  (Image)inventorySlot.getChild(0);
			TextButton counter = (TextButton)inventorySlot.getChild(2);
			counter.setVisible(true);
			counter.setText("1");
			itemImage.setPosition(borderImage.getX(), borderImage.getY());
			itemImage.setSize(borderImage.getWidth(), borderImage.getHeight());
			itemImage.setDrawable(new TextureRegionDrawable(updatingItem.getTextureRegion()));
		}
	}
}
