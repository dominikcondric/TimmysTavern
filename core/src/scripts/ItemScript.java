package scripts;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;
import com.gdx.game.Item;
import com.gdx.game.TimmysTavern;

import components.EntityBits;
import components.GuiComponent;
import components.ScriptComponent;
import components.SoundComponent;

public class ItemScript extends Script {
	Item item;
	private final int maxItems;
	private int itemsAvailable;
	private boolean pickable = false;
	private float newItemTimer = 0.f;
	private float timeToNextItemAppear;
	private ComponentMapper<SoundComponent> soundCompMapper = ComponentMapper.getFor(SoundComponent.class);
	private ComponentMapper<ScriptComponent> scriptCompMapper = ComponentMapper.getFor(ScriptComponent.class);
	
	public ItemScript(Entity self, Item item, final int maxFruits, final float timeToNextFruitGrow) {
		super(self);
		this.item = item;
		this.maxItems = maxFruits;
		itemsAvailable = maxFruits;
		this.timeToNextItemAppear = timeToNextFruitGrow;
		
		GuiComponent guiComp = self.getComponent(GuiComponent.class);
		if (guiComp == null) {
			guiComp = new GuiComponent();
			self.add(guiComp);
		}
		
		Image borderImage = new Image(new Texture(Gdx.files.internal("InventoryItemBorder.png")));
		borderImage.setFillParent(true);
		guiComp.actors.addActor(borderImage);
		
		Label label = new Label("Pritisni ENTER da uzmes predmet: " + item.guiName, new LabelStyle(TimmysTavern.font, Color.WHITE));
		label.setFillParent(true);
		label.setFontScale(2.f);
		label.setWrap(true);
		label.setAlignment(Align.center);
		
		guiComp.actors.setPosition(Gdx.graphics.getWidth() / 12.f, Gdx.graphics.getHeight() * 0.8f);
		guiComp.actors.setSize(Gdx.graphics.getWidth() / 5.f, Gdx.graphics.getHeight() * 0.1f);
		guiComp.actors.addActor(label);
		guiComp.actors.setVisible(false);
	}
	
	@Override
	public void update(float deltaTime) {
		if (pickable && itemsAvailable > 0 && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			scriptCompMapper.get(self).eventsToDispatch.add("PickItem");
			scriptCompMapper.get(self).eventsToListen.add("ItemPicked");
		}
		
		newItemTimer += deltaTime;
		if (newItemTimer >= timeToNextItemAppear) {
			itemsAvailable = Math.min(itemsAvailable + 1, maxItems);
			newItemTimer = 0.f;
		}
	}
	
	@Override
	public void onEventReceived(Entity sender, String eventName) {
		if (eventName == "ItemPicked") {
			scriptCompMapper.get(self).eventsToListen.remove(eventName);
			soundCompMapper.get(self).getSoundEffect("ItemPicked").shouldPlay = true;
			--itemsAvailable;
		}
	}

	@Override
	public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {
		if ((other.getFilterData().categoryBits & EntityBits.PLAYER_B2D_BIT) != 0) {
			pickable = false;
			this.self.getComponent(GuiComponent.class).actors.setVisible(false);
		}
	}
	
	@Override
	public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {
		if ((other.getFilterData().categoryBits & EntityBits.PLAYER_B2D_BIT) != 0) {
			pickable = true;
			this.self.getComponent(GuiComponent.class).actors.setVisible(true);
		}
	}

	@Override
	public void onEventResponse(Entity receiver, String eventName) {
		scriptCompMapper.get(self).eventsToDispatch.remove("PickItem");
	}
}
