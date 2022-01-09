package scripts;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

public abstract class Script {
	public abstract void update(Entity self, float deltaTime);
	public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {}
	public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {}
	public void onEventListened(Entity self, Entity sender, String eventName) {}
	public void onEventReceived(Entity self, Entity receiver, String eventName) {}
}
