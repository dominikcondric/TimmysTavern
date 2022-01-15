package systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
//import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

//import components.CameraComponent;
import components.PhysicsComponent;
import components.SpriteComponent;
import scripts.Script;

public class PhysicsSystem extends EntitySystem implements ContactListener, Disposable {
	private World b2dWorld;
	private ComponentMapper<SpriteComponent> spriteCompMapper;
	private ComponentMapper<PhysicsComponent> physicsCompMapper;
	private Family physicsFamily = Family.all(PhysicsComponent.class, SpriteComponent.class).get();
//	private Family cameraFamily = Family.all(CameraComponent.class).get();
//	private Box2DDebugRenderer debugRenderer;
	
	public PhysicsSystem(Vector2 gravity, int priority) {
		super(priority);
		b2dWorld = new World(gravity, true);
		spriteCompMapper = ComponentMapper.getFor(SpriteComponent.class);
		physicsCompMapper = ComponentMapper.getFor(PhysicsComponent.class);
		b2dWorld.setContactListener(this);
//		debugRenderer = new Box2DDebugRenderer();
	}
	
	@Override
	public void update(float deltaTime) {
		b2dWorld.step(deltaTime, 10, 10);
		for (Entity e : getEngine().getEntitiesFor(physicsFamily)) {
			PhysicsComponent physicsComp = physicsCompMapper.get(e);
			if (physicsComp.body != null && physicsComp.body.getType() != BodyType.StaticBody) {
				Vector2 bodyPosition = physicsComp.body.getPosition();
				SpriteComponent spriteComp = spriteCompMapper.get(e);
				Vector2 spriteSize = spriteComp.getSpriteSize();
				spriteComp.position.x = bodyPosition.x - spriteSize.x / 2.f;
				spriteComp.position.y = bodyPosition.y - spriteSize.y / 2.f;
			}
		}
		
//		debugRenderer.render(b2dWorld, getEngine().getEntitiesFor(cameraFamily).get(0).getComponent(CameraComponent.class).camera.combined);
	}
	
	public Body createBody(BodyDef bodyDef) {
		return b2dWorld.createBody(bodyDef);
	}
	
	@Override
	public void beginContact(Contact contact) {
		Script a = (Script)contact.getFixtureA().getUserData();
		Script b = (Script)contact.getFixtureB().getUserData();
		
		if (a != null) {
			a.onCollisionBegin(contact, contact.getFixtureA(), contact.getFixtureB());
		}
		
		if (b != null) {
			b.onCollisionBegin(contact, contact.getFixtureB(), contact.getFixtureA());
		}
	}

	@Override
	public void endContact(Contact contact) {
		Script a = (Script)contact.getFixtureA().getUserData();
		Script b = (Script)contact.getFixtureB().getUserData();
		
		if (a != null)
			a.onCollisionEnd(contact, contact.getFixtureA(), contact.getFixtureB());
		
		if (b != null)
			b.onCollisionEnd(contact, contact.getFixtureB(), contact.getFixtureA());
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
