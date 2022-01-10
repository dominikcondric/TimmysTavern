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
import components.NewSceneComponent;
import components.ScriptComponent;

public class DoorScript extends Script {
	private boolean openable = false;
	
	public DoorScript(Entity self) {
		super(self);
		GuiComponent guiComp = self.getComponent(GuiComponent.class);
		if (guiComp == null) {
			guiComp = new GuiComponent();
			self.add(guiComp);
		}
		
		Image borderImage = new Image(new Texture(Gdx.files.internal("InventoryItemBorder.png")));
		borderImage.setPosition(50f, 900f);
		borderImage.setSize(500.f, 50.f);
		guiComp.actors.addActor(borderImage);
		
		Label label = new Label("Press ENTER to open the door", new LabelStyle(TimmysTavern.font, Color.WHITE));
		label.setFontScale(2.f);
		label.setPosition(50f, 900.f);
		label.setSize(500.f, 50.f);
		label.setAlignment(Align.center);
		guiComp.actors.addActor(label);
		guiComp.actors.setVisible(false);
	}
	
	@Override
	public void update(float deltaTime) {
		if (openable && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			ScriptComponent scriptComp = self.getComponent(ScriptComponent.class);
			scriptComp.eventsToDispatch.add("SceneChanged");
			self.getComponent(NewSceneComponent.class).load = true;
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
