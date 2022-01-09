package scripts;

import java.util.ArrayList;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;

import components.EntityBits;
import components.MapComponent;
import components.MusicComponent;
import components.NewSceneComponent;
import components.PhysicsComponent;
import components.ScriptComponent;
import utility.B2DBodyCreator;

public class TavernSceneScript implements NewSceneScript {
	
	@Override
	public void loadNewScene(ArrayList<Entity> entitiesToLoad, B2DBodyCreator bodyCreator) {
		Entity mapEntity = new Entity();
		MapComponent mapComp = new MapComponent("Maps/Tavern.tmx");
		mapEntity.add(mapComp);
	
		// Background music
		MusicComponent musicComp = (MusicComponent)mapEntity.addAndReturn(new MusicComponent(Gdx.files.internal("PGS Fantasy RPG Music Pack/Town-Village Theme 3.ogg")));
		musicComp.music.setLooping(true);
		musicComp.shouldPlay = true;
		musicComp.music.setVolume(0.2f);

		PhysicsComponent mapPhysicsComp = (PhysicsComponent)mapEntity.addAndReturn(new PhysicsComponent());
		
		float mapScalingFactor = 1f / mapComp.map.getProperties().get("tilewidth", Integer.class);
		Vector2 mapSize = mapComp.getMapSize();
		
		Body body = null;
		BodyDef bodyDef = null;
		FixtureDef fixtureDef = null;
		PolygonShape polyShape = null;
		
		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(mapSize.x / 2.f, mapSize.y / 2.f);
		body = bodyCreator.createBody(bodyDef);
		
		for (MapObject object : mapComp.map.getLayers().get("Obstacles").getObjects().getByType(RectangleMapObject.class)) {
			Rectangle rect = ((RectangleMapObject)object).getRectangle();
			rect.set(rect.getX() * mapScalingFactor, rect.getY() * mapScalingFactor, rect.getWidth() * mapScalingFactor, rect.getHeight() * mapScalingFactor);

			polyShape = new PolygonShape();
			polyShape.setAsBox(rect.getWidth() / 2f, rect.getHeight() / 2f, rect.getCenter(new Vector2()).sub(body.getPosition()), 0.f);
			fixtureDef = new FixtureDef();
			fixtureDef.shape = polyShape;
			fixtureDef.filter.categoryBits = EntityBits.OBSTACLE_B2D_BIT;
			fixtureDef.filter.maskBits = EntityBits.PLAYER_B2D_BIT;
			fixtureDef.filter.groupIndex = -EntityBits.STATIC_SCENERY_B2D_GROUP;
			
			body.createFixture(fixtureDef);
			polyShape.dispose();
		}
		
		mapPhysicsComp.body = body;
		entitiesToLoad.add(mapEntity);
		
		Entity tavernDoor = new Entity();
		PhysicsComponent tavernDoorPhysicsComp = new PhysicsComponent();
		Rectangle tavernDoorRect = ((RectangleMapObject)mapComp.map.getLayers().get("Interactables").getObjects().get("TavernDoor")).getRectangle();
		tavernDoorRect.set(tavernDoorRect.x * mapScalingFactor, tavernDoorRect.y * mapScalingFactor, tavernDoorRect.width * mapScalingFactor, tavernDoorRect.height * mapScalingFactor);
		bodyDef = new BodyDef();
		bodyDef.position.set(tavernDoorRect.getX() + tavernDoorRect.getWidth() / 2.f, tavernDoorRect.getY() + tavernDoorRect.getHeight() / 2.f);
		bodyDef.type = BodyDef.BodyType.StaticBody;
		body = bodyCreator.createBody(bodyDef);
		polyShape = new PolygonShape();
		polyShape.setAsBox(tavernDoorRect.getWidth() / 2f, tavernDoorRect.getHeight() / 2f);
		fixtureDef = new FixtureDef();
		fixtureDef.shape = polyShape;
		fixtureDef.isSensor = true;
		fixtureDef.filter.categoryBits = EntityBits.INTERACTABLE_B2D_BIT;
		fixtureDef.filter.groupIndex = -EntityBits.STATIC_SCENERY_B2D_GROUP;
		fixtureDef.filter.maskBits = EntityBits.PLAYER_B2D_BIT;
		
		body.createFixture(fixtureDef).setUserData(tavernDoor);
		polyShape.dispose();
		
		tavernDoorPhysicsComp.body = body;
		tavernDoor.add(tavernDoorPhysicsComp);
		
		tavernDoor.add(new ScriptComponent(new DoorScript()));
		tavernDoor.add(new NewSceneComponent(new VillageSceneScript(), "village"));
		entitiesToLoad.add(tavernDoor);
	}
}
