package components;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

public interface Script {
	public void update(Entity self, float deltaTime);
	public void onCollisionBegin(Contact contact, Fixture self, Fixture other);
	public void onCollisionEnd(Contact contact, Fixture self, Fixture other);
	public void onEventListen(Entity self, Entity sender, String eventName);
}
