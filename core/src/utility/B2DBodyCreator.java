package utility;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.World;

public class B2DBodyCreator {
	private World world;
	
	public B2DBodyCreator(World world) {
		this.world = world;
	}
	
	public Body createBody(BodyDef bodyDef) {
		return world.createBody(bodyDef);
	}
}
