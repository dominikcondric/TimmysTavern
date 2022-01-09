package scripts;

import java.util.ArrayList;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.gdx.game.Item;

import components.EntityBits;
import components.ItemComponent;
import components.MapComponent;
import components.MusicComponent;
import components.NewSceneComponent;
import components.PhysicsComponent;
import components.ScriptComponent;
import components.SoundComponent;
import utility.B2DBodyCreator;

public class VillageSceneScript implements NewSceneScript {

	@Override
	public void loadNewScene(ArrayList<Entity> entitiesToLoad, B2DBodyCreator bodyCreator) {
		Entity mapEntity = new Entity();
		MapComponent mapComp = new MapComponent("Maps/Village.tmx");
		mapComp.setLastBackgroundLayerToRenderIndex(5);
		mapEntity.add(mapComp);
		
		// Background music
		MusicComponent musicComp = (MusicComponent)mapEntity.addAndReturn(new MusicComponent(Gdx.files.internal("PGS Fantasy RPG Music Pack/Town-Village Theme 1.ogg")));
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
		bodyDef.position.set(mapSize.x * mapScalingFactor / 2.f, mapSize.y * mapScalingFactor / 2.f);
		body = bodyCreator.createBody(bodyDef);
				
		for (MapObject object : mapComp.map.getLayers().get("Obstacles").getObjects().getByType(RectangleMapObject.class)) {
			Rectangle rect = ((RectangleMapObject)object).getRectangle();
			rect.set(rect.getX() * mapScalingFactor, rect.getY() * mapScalingFactor, rect.getWidth() * mapScalingFactor, rect.getHeight() * mapScalingFactor);
			
			polyShape = new PolygonShape();
			polyShape.setAsBox(rect.getWidth() / 2f, rect.getHeight() / 2.f, rect.getCenter(new Vector2()).sub(body.getPosition()), 0.f);
			fixtureDef = new FixtureDef();
			fixtureDef.shape = polyShape;
			fixtureDef.filter.categoryBits = EntityBits.OBSTACLE_B2D_BIT;
			fixtureDef.filter.groupIndex = -EntityBits.STATIC_SCENERY_B2D_GROUP;
			fixtureDef.filter.maskBits = EntityBits.PLAYER_B2D_BIT;
			
			body.createFixture(fixtureDef);
			polyShape.dispose();
		}
		
		for (MapObject object : mapComp.map.getLayers().get("Obstacles").getObjects().getByType(PolygonMapObject.class)) {
			Polygon poly = ((PolygonMapObject)object).getPolygon();
			poly.setPosition(poly.getX() * mapScalingFactor - body.getPosition().x, poly.getY() * mapScalingFactor - body.getPosition().y);
			
			for (int i = 0; i < poly.getVertices().length; ++i) {
				poly.getVertices()[i] *= mapScalingFactor;
			}
			
			polyShape = new PolygonShape();
			polyShape.set(poly.getTransformedVertices());
			fixtureDef = new FixtureDef();
			fixtureDef.shape = polyShape;
			fixtureDef.filter.categoryBits = EntityBits.OBSTACLE_B2D_BIT;
			fixtureDef.filter.groupIndex = -EntityBits.STATIC_SCENERY_B2D_GROUP;
			fixtureDef.filter.maskBits = EntityBits.PLAYER_B2D_BIT;
			
			body.createFixture(fixtureDef);
			polyShape.dispose();
		}
		
		mapPhysicsComp.body = body;
		entitiesToLoad.add(mapEntity);
		
		for (RectangleMapObject object : mapComp.map.getLayers().get("Interactables").getObjects().getByType(RectangleMapObject.class)) {
			Entity interactable = new Entity();
			PhysicsComponent interactablePhysicsComp = new PhysicsComponent();
			Rectangle rect = object.getRectangle();
			rect.set(rect.x * mapScalingFactor, rect.y * mapScalingFactor, rect.width * mapScalingFactor, rect.height * mapScalingFactor);
			bodyDef = new BodyDef();
			bodyDef.position.set(rect.getX() + rect.getWidth() / 2.f, rect.getY() + rect.getHeight() / 2.f);
			bodyDef.type = BodyDef.BodyType.StaticBody;
			body = bodyCreator.createBody(bodyDef);
			polyShape = new PolygonShape();
			polyShape.setAsBox(rect.getWidth() / 2f, rect.getHeight() / 2f);
			fixtureDef = new FixtureDef();
			fixtureDef.shape = polyShape;
			fixtureDef.isSensor = true;
			fixtureDef.filter.categoryBits = EntityBits.INTERACTABLE_B2D_BIT;
			fixtureDef.filter.groupIndex = -EntityBits.STATIC_SCENERY_B2D_GROUP;
			fixtureDef.filter.maskBits = EntityBits.PLAYER_B2D_BIT;
			
			interactablePhysicsComp.body = body;
			body.createFixture(fixtureDef).setUserData(interactable);
			polyShape.dispose();
			
			switch (object.getName()) {
				case "TavernDoor":
				{
					interactable.add(new ScriptComponent(new DoorScript()));
					interactable.add(new NewSceneComponent(new TavernSceneScript(), "tavern"));
					entitiesToLoad.add(interactable);
					break;
				}
				case "apple":
				{
					interactable.add(new ScriptComponent(new FruitItemScript(20, 60.f)));
					interactable.add(new ItemComponent(Item.APPLE));
					SoundComponent itemSoundComp = new SoundComponent(); 
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					entitiesToLoad.add(interactable);
					break;
				}
				case "cranberry":
				{
					interactable.add(new ScriptComponent(new FruitItemScript(40, 10f)));
					interactable.add(new ItemComponent(Item.CRANBERRY));
					SoundComponent itemSoundComp = new SoundComponent(); 
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					entitiesToLoad.add(interactable);
					break;
				}
				case "blueberry":
				{
					interactable.add(new ScriptComponent(new FruitItemScript(40, 10f)));
					interactable.add(new ItemComponent(Item.BLUEBERRY));
					SoundComponent itemSoundComp = new SoundComponent(); 
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					entitiesToLoad.add(interactable);
					break;
				}
			}
		}
	}
}
