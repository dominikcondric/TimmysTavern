package systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;

import components.PhysicsComponent;
import components.ScriptComponent;
import components.SpriteComponent;

public class PhysicsSystem extends EntitySystem implements ContactListener, Disposable {
	private World b2dWorld;
	private ComponentMapper<ScriptComponent> scriptCompMapper;
	private ComponentMapper<SpriteComponent> spriteCompMapper;
	private ComponentMapper<PhysicsComponent> physicsCompMapper;
	private Family physicsFamily = Family.all(PhysicsComponent.class, SpriteComponent.class).get();
	
	public PhysicsSystem(Vector2 gravity, int priority) {
		super(priority);
		b2dWorld = new World(gravity, true);
		scriptCompMapper = ComponentMapper.getFor(ScriptComponent.class);
		spriteCompMapper = ComponentMapper.getFor(SpriteComponent.class);
		physicsCompMapper = ComponentMapper.getFor(PhysicsComponent.class);
		b2dWorld.setContactListener(this);
	}
	
	public void drawDebugPhysics(Box2DDebugRenderer renderer, Matrix4 projectionMatrix) {
		renderer.render(b2dWorld, projectionMatrix);
	}
	
	@Override
	public void update(float deltaTime) {
		b2dWorld.step(deltaTime, 10, 10);
		for (Entity e : getEngine().getEntitiesFor(physicsFamily)) {
			Vector2 bodyPosition = physicsCompMapper.get(e).body.getPosition();
			SpriteComponent spriteComp = spriteCompMapper.get(e);
			Vector2 spriteSize = spriteComp.getSpriteSize();
			spriteComp.position.x = bodyPosition.x - spriteSize.x / 2.f;
			spriteComp.position.y = bodyPosition.y - spriteSize.y / 2.f;
		}
	}
	
	public Body createBody(BodyDef bodyDef) {
		return b2dWorld.createBody(bodyDef);
	}
	
	@Override
	public void beginContact(Contact contact) {
		Entity a = (Entity)contact.getFixtureA().getUserData();
		Entity b = (Entity)contact.getFixtureB().getUserData();
		
		if (a != null && scriptCompMapper.has(a)) {
			scriptCompMapper.get(a).script.onCollisionBegin(contact, contact.getFixtureA(), contact.getFixtureB());
		}
		
		if (b != null && scriptCompMapper.has(b)) {
			scriptCompMapper.get(b).script.onCollisionBegin(contact, contact.getFixtureB(), contact.getFixtureA());
		}
	}

	@Override
	public void endContact(Contact contact) {
		Entity a = (Entity)contact.getFixtureA().getUserData();
		Entity b = (Entity)contact.getFixtureB().getUserData();
		
		if (a != null && scriptCompMapper.has(a))
			scriptCompMapper.get(a).script.onCollisionEnd(contact, contact.getFixtureA(), contact.getFixtureB());
		
		if (b != null && scriptCompMapper.has(b))
			scriptCompMapper.get(b).script.onCollisionEnd(contact, contact.getFixtureB(), contact.getFixtureA());
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		b2dWorld.dispose();
	}
}
