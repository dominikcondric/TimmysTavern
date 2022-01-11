package scripts;

import java.util.HashMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.gdx.game.Item;

import components.AnimationComponent;
import components.GuiComponent;
import components.ItemComponent;
import components.NewSceneComponent;
import components.PhysicsComponent;
import components.ScriptComponent;
import components.SoundComponent;
import scripts.CookerScript.Ingredient;
import scripts.CookerScript.Recipe;

public class PlayerScript extends Script {
	private int lastMovingDirection = Keys.DOWN;
	private ComponentMapper<PhysicsComponent> physicsComponentMapper = ComponentMapper.getFor(PhysicsComponent.class);
	private ComponentMapper<AnimationComponent> animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);
	private ComponentMapper<ItemComponent> itemComponentMapper = ComponentMapper.getFor(ItemComponent.class);
	private ComponentMapper<GuiComponent> guiComponentMapper = ComponentMapper.getFor(GuiComponent.class);
	private ComponentMapper<SoundComponent> soundComponentMapper = ComponentMapper.getFor(SoundComponent.class);
	private ComponentMapper<ScriptComponent> scriptComponentMapper = ComponentMapper.getFor(ScriptComponent.class);
	HashMap<String, InventorySlot> inventory;
	private boolean inventoryOpen = false;
	
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
			switch (sender.getComponent(NewSceneComponent.class).sceneName) {
				case "village":
					physicsComponentMapper.get(self).body.setTransform(new Vector2(48.f, 8.f),  0.f);
					animationComponentMapper.get(self).setActiveAnimation("IdleDown", false);
					break;
				
				case "tavern":
					physicsComponentMapper.get(self).body.setTransform(new Vector2(10.f, 2.f),  0.f);
					animationComponentMapper.get(self).setActiveAnimation("IdleUp", false);
					break;
			}
		} else if (eventName.contentEquals("ItemPicked")) {
			Item pickedItem = itemComponentMapper.get(sender).item;
			InventorySlot slot = inventory.get(pickedItem.name);
			int newAmount = slot == null ? 1 : slot.itemCount + 1;
			updateInventory(pickedItem, newAmount);
		} else if (eventName == "CookingStarted") {
			Recipe activeReceipt = ((CookerScript)scriptComponentMapper.get(sender).script).activeRecipe;
			for (Ingredient i : activeReceipt.ingredients) {
				updateInventory(i.item, inventory.get(i.item.name).itemCount - i.amount);
			}
		}
	}
	
	private void updateInventory(Item updatingItem, int newAmount) {
		GuiComponent inventoryGui = guiComponentMapper.get(self);
		if (inventory.containsKey(updatingItem.name)) {
			InventorySlot slot = inventory.get(updatingItem.name);
			slot.itemCount = newAmount;
			Label slotLabel = (Label)((Group)inventoryGui.actors.getChild(slot.slotNumber)).getChild(2);
			if (slot.itemCount == 0) {
				slotLabel.setVisible(false);
				Image itemImage = (Image)((Group)inventoryGui.actors.getChild(slot.slotNumber)).getChild(1);
				((TextureRegionDrawable)itemImage.getDrawable()).getRegion().getTexture().dispose();
				itemImage.setDrawable(null);
				inventory.remove(updatingItem.name);
			} else if (slot.itemCount == 1) {
				slotLabel.setVisible(false);
				slotLabel.setText(Integer.toString(slot.itemCount));
			} else if (slot.itemCount > 1) {
				slotLabel.setVisible(true);
				slotLabel.setText(Integer.toString(slot.itemCount));
			}
		} else {
			Group inventorySlot = ((Group)inventoryGui.actors.getChild(inventory.size()));
			inventory.put(updatingItem.name, new InventorySlot(inventory.size(), newAmount));
			Image itemImage = (Image)inventorySlot.getChild(1);
			Image borderImage =  (Image)inventorySlot.getChild(0);
			itemImage.setPosition(borderImage.getX(), borderImage.getY());
			itemImage.setSize(borderImage.getWidth(), borderImage.getHeight());
			itemImage.setDrawable(new TextureRegionDrawable(updatingItem.getTextureRegion()));
		}
	}
}
