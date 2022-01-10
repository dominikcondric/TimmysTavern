package scripts;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

public abstract class Script {
	protected final Entity self;
	
	public Script(Entity selfEntity) {
		self = selfEntity;
	}
	
	public abstract void update(float deltaTime);
	public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {}
	public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {}
	public void onEventReceived(Entity sender, String eventName) {}
	public void onEventResponse(Entity receiver, String eventName) {}
}
