package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.Disposable;

public class PhysicsComponent implements Component, Disposable {
	public Body body;

	public PhysicsComponent(Body body) {
		this.body = body;
	}
	
	@Override
	public void dispose() {
		if (body == null)
			return;
		
		body.getWorld().destroyBody(body);
		body = null;
	}
}
