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
import com.gdx.game.TimmysTavern;

import components.EntityBits;
import components.GuiComponent;
import components.ItemComponent;
import components.ScriptComponent;
import components.SoundComponent;

public class ItemScript extends Script {
	private final int maxItems;
	private int itemsAvailable;
	private boolean pickable = false;
	private float newItemTimer = 0.f;
	private float timeToNextItemAppear;
	private ComponentMapper<SoundComponent> soundCompMapper = ComponentMapper.getFor(SoundComponent.class);
	private ComponentMapper<ScriptComponent> scriptCompMapper = ComponentMapper.getFor(ScriptComponent.class);
	
	public ItemScript(Entity self, final int maxFruits, final float timeToNextFruitGrow) {
		super(self);
		this.maxItems = maxFruits;
		itemsAvailable = maxFruits;
		this.timeToNextItemAppear = timeToNextFruitGrow;
		
		GuiComponent guiComp = self.getComponent(GuiComponent.class);
		if (guiComp == null) {
			guiComp = new GuiComponent();
			self.add(guiComp);
		}
		
		Image borderImage = new Image(new Texture(Gdx.files.internal("InventoryItemBorder.png")));
		borderImage.setPosition(50f, 900f);
		borderImage.setSize(500f, 50.f);
		guiComp.actors.addActor(borderImage);
		
		Label label = new Label("Press ENTER to pick " + self.getComponent(ItemComponent.class).item.name, new LabelStyle(TimmysTavern.font, Color.WHITE));
		label.setFontScale(2.f);
		label.setPosition(50f, 900f);
		label.setSize(500f, 50.f);
		label.setAlignment(Align.center);
		guiComp.actors.addActor(label);
		guiComp.actors.setVisible(false);
	}
	
	@Override
	public void update(float deltaTime) {
		if (pickable && itemsAvailable > 0 && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			scriptCompMapper.get(self).eventsToDispatch.add("ItemPicked");
			soundCompMapper.get(self).getSoundEffect("ItemPicked").shouldPlay = true;
			--itemsAvailable;
		}
		
		newItemTimer += deltaTime;
		if (newItemTimer >= timeToNextItemAppear) {
			itemsAvailable = Math.min(itemsAvailable + 1, maxItems);
			newItemTimer = 0.f;
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
		scriptCompMapper.get(self).eventsToDispatch.remove("ItemPicked");
	}
}
