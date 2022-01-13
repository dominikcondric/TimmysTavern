package scripts;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;
import com.gdx.game.Item;
import com.gdx.game.TimmysTavern;

import components.DestroyEntityComponent;
import components.EntityBits;
import components.GuiComponent;
import components.PhysicsComponent;
import components.ScriptComponent;
import components.SpriteComponent;

public class NPCCustomerScript extends Script {
	Item orderedRecipe;
	private boolean recipeTakeable = false;
	
	public NPCCustomerScript(Entity selfEntity) {
		super(selfEntity);
		orderedRecipe = TimmysTavern.cookbook.getRandomRecipeItem();
		
		GuiComponent guiComp = self.getComponent(GuiComponent.class);
		if (guiComp == null) {
			guiComp = new GuiComponent();
			self.add(guiComp);
		}
		
		Image borderImage = new Image(new Texture(Gdx.files.internal("InventoryItemBorder.png")));
		borderImage.setFillParent(true);
		guiComp.actors.addActor(borderImage);
		
		Label label = new Label(orderedRecipe.name, new LabelStyle(TimmysTavern.font, Color.WHITE));
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
		if (orderedRecipe != null) {
			if (recipeTakeable && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
				self.getComponent(ScriptComponent.class).eventsToDispatch.add("NPCMealTake");
				recipeTakeable = false;
			}
		} else {
			self.getComponent(DestroyEntityComponent.class).destroy = true;
		}
	}

	@Override
	public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {
		if ((other.getFilterData().categoryBits & EntityBits.PLAYER_B2D_BIT) != 0) {
			this.self.getComponent(GuiComponent.class).actors.setVisible(true);
			if (((PlayerScript)other.getUserData()).inventory.containsKey(orderedRecipe.name))
				recipeTakeable = true;
		}
	}
	
	@Override
	public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {
		if ((other.getFilterData().categoryBits & EntityBits.PLAYER_B2D_BIT) != 0) {
			recipeTakeable = false;
			this.self.getComponent(GuiComponent.class).actors.setVisible(false);
		}
	}

	@Override
	public void onEventReceived(Entity sender, String eventName) {
		if (eventName == "SceneChanged") {
			Body body = self.getComponent(PhysicsComponent.class).body;
			body.setActive(!body.isActive());
			SpriteComponent spriteComp = self.getComponent(SpriteComponent.class); 
			spriteComp.draw = !spriteComp.draw;
		}
	}

	@Override
	public void onEventResponse(Entity receiver, String eventName) {
		if (eventName == "NPCMealTake") {
			self.getComponent(ScriptComponent.class).eventsToDispatch.remove(eventName);
		}
	}
}
