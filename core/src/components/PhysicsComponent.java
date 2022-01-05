package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.utils.Disposable;

public class PhysicsComponent implements Component, Disposable {
	public Body body;

	@Override
	public void dispose() {
		for (Fixture f : body.getFixtureList()) {
			body.destroyFixture(f);
		}
		
		body.getWorld().destroyBody(body);
	}
}
