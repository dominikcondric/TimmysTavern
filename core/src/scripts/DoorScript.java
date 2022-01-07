package scripts;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import components.EntityBits;
import components.ScriptComponent;
import systems.SceneLoadingSystem.Scenes;

public class DoorScript extends Script {
	public Scenes sceneToLoad;
	private boolean openable = false;
	
	public DoorScript(Scenes sceneToLoad) {
		this.sceneToLoad = sceneToLoad;
	}
	
	@Override
	public void update(Entity self, float deltaTime) {
		if (openable && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			ScriptComponent scriptComp = self.getComponent(ScriptComponent.class);
			scriptComp.eventsToDispatch.add("SceneChanged");
			scriptComp.newSceneToLoad = sceneToLoad;
		}
	}
	
	@Override
	public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {
		if ((((Entity)other.getUserData()).flags & EntityBits.PLAYER_BIT) != 0) {
			openable = false;
		}
	}
	
	@Override
	public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {
		if ((((Entity)other.getUserData()).flags & EntityBits.PLAYER_BIT) != 0) {
			openable = true;
		}
	}
}
