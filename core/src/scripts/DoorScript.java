package scripts;

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
import components.AddEntityComponent;
import components.ScriptComponent;

public class DoorScript extends Script {
	private boolean openable = false;
	String newSceneName;
	
	public DoorScript(Entity self, String newSceneName) {
		super(self);
		this.newSceneName = newSceneName;
		GuiComponent guiComp = self.getComponent(GuiComponent.class);
		if (guiComp == null) {
			guiComp = new GuiComponent();
			self.add(guiComp);
		}
		
		Image borderImage = new Image(new Texture(Gdx.files.internal("InventoryItemBorder.png")));
		borderImage.setFillParent(true);
		guiComp.actors.addActor(borderImage);
		
		Label label = new Label("Pritisni ENTER da otvoris vrata", new LabelStyle(TimmysTavern.font, Color.WHITE));
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
		if (openable && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			ScriptComponent scriptComp = self.getComponent(ScriptComponent.class);
			scriptComp.eventsToDispatch.add("SceneChanged");
			self.getComponent(AddEntityComponent.class).load = true;
		}
	}
	
	@Override
	public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {
		if ((other.getFilterData().categoryBits & EntityBits.PLAYER_B2D_BIT) != 0) {
			openable = false;
			this.self.getComponent(GuiComponent.class).actors.setVisible(false);
		}
	}
	
	@Override
	public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {
		if ((other.getFilterData().categoryBits & EntityBits.PLAYER_B2D_BIT) != 0) {
			openable = true;
			this.self.getComponent(GuiComponent.class).actors.setVisible(true);
		}
	}

	@Override
	public void onEventResponse(Entity receiver, String eventName) {
		if (eventName.contentEquals("SceneChanged")) {
			self.getComponent(ScriptComponent.class).eventsToDispatch.remove(eventName);
		}
	}
	
	
}
