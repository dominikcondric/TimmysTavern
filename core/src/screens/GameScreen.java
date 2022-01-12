package screens;

import java.util.ArrayList;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntityListener;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
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
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.gdx.game.Item;
import com.gdx.game.TimmysTavern;

import components.AddEntityComponent;
import components.AnimationComponent;
import components.CameraComponent;
import components.DestroyEntityComponent;
import components.EntityBits;
import components.GuiComponent;
import components.ItemComponent;
import components.MapComponent;
import components.MusicComponent;
import components.PhysicsComponent;
import components.ScriptComponent;
import components.SoundComponent;
import components.SoundComponent.SoundEffect;
import components.SpriteComponent;
import scripts.CookerScript;
import scripts.DoorScript;
import scripts.FollowingCameraScript;
import scripts.ItemScript;
import scripts.PlayerScript;
import scripts.TavernScript;
import systems.AudioSystem;
import systems.PhysicsSystem;
import systems.RenderingSystem;
import systems.SceneManagementSystem;
import systems.ScriptingSystem;

public class GameScreen implements Screen {
	private TimmysTavern game;
	private Engine ecs;
	private ArrayList<Entity> villageEntities;
	private ArrayList<Entity> tavernEntities;

	public GameScreen(TimmysTavern game) {
		this.game = game;
		ecs = new Engine();
		ecs.addSystem(new RenderingSystem(game.batch, 1));
		ecs.addSystem(new PhysicsSystem(new Vector2(0.f, 0.f), 2));
		ecs.addSystem(new ScriptingSystem(0));
		ecs.addSystem(new SceneManagementSystem(4));
		ecs.addSystem(new AudioSystem(3));
		ecs.addEntityListener(Family.all(PhysicsComponent.class).get(), new EntityListener() {
			
			@Override
			public void entityRemoved(Entity entity) {
				if (entity.getComponent(PhysicsComponent.class).body != null)
					entity.getComponent(PhysicsComponent.class).body.setActive(false);
			}
			
			@Override
			public void entityAdded(Entity entity) {
				entity.getComponent(PhysicsComponent.class).body.setActive(true);
			}
		});
		
		ecs.addEntityListener(Family.all(GuiComponent.class).get(), new EntityListener() {
			
			@Override
			public void entityRemoved(Entity entity) {
				entity.getComponent(GuiComponent.class).actors.remove();
			}
			
			@Override
			public void entityAdded(Entity entity) {
				ecs.getSystem(RenderingSystem.class).ui.addActor(entity.getComponent(GuiComponent.class).actors);
			}
		});
		
		ecs.addEntityListener(Family.all(MusicComponent.class).get(), new EntityListener() {
			
			@Override
			public void entityRemoved(Entity entity) {
				entity.getComponent(MusicComponent.class).music.stop();
			}
			
			@Override
			public void entityAdded(Entity entity) {
				entity.getComponent(MusicComponent.class).music.play();
			}
		});
		
		ecs.addEntityListener(Family.all(SoundComponent.class).get(), new EntityListener() {
			
			@Override
			public void entityRemoved(Entity entity) {
				for (SoundEffect sf : entity.getComponent(SoundComponent.class).getSoundEffects()) {
					sf.sound.stop();
				}
			}
			
			@Override
			public void entityAdded(Entity entity) {
			}
		});
		loadScenes();
	}
	
	private void loadScenes() {
		villageEntities = new ArrayList<Entity>();
		tavernEntities = new ArrayList<Entity>();
		ArrayList<AddEntityComponent> addEntityComps = new ArrayList<AddEntityComponent>();
		
		Entity player = new Entity();
		float playerWHRatio = 20f / 28f;
		Vector2 playerPosition = new Vector2(49.f, 28.f);
		Vector2 playerSize = new Vector2(2 * playerWHRatio, 2f);
		
		// Player sprite/animation components
		@SuppressWarnings(value = { "unchecked" })
		Array<Sprite>[] playerAnimationSprites = new Array[8];
		int playerTextureRegionWidth = 20;
		int playerTextureRegionHeight = 30;
		for (int i = 0; i < 8; ++i) {
			playerAnimationSprites[i] = new Array<Sprite>(4);
			for (int j = 0; j < 4; ++j) {
				Sprite sprite = new Sprite(new TextureRegion(new Texture(Gdx.files.internal("Free Asset Pack\\Characters\\Character 1.png")), 2 + j * (playerTextureRegionWidth + 4), 2 + ((playerTextureRegionHeight + 2) * i), playerTextureRegionWidth, playerTextureRegionHeight));
				sprite.setPosition(playerPosition.x, playerPosition.y);
				sprite.setSize(playerSize.x, playerSize.y);
				playerAnimationSprites[i].add(sprite);
			}
		}
		
		player.add(new SpriteComponent(playerAnimationSprites[0].get(0)));
		
		// Player animation component
		AnimationComponent playerAnimationComp = new AnimationComponent();
		playerAnimationComp.addAnimation("IdleDown", playerAnimationSprites[0], 0.2f, PlayMode.LOOP, true);
		playerAnimationComp.addAnimation("IdleUp", playerAnimationSprites[1], 0.2f, PlayMode.LOOP, false);
		playerAnimationComp.addAnimation("IdleRight", playerAnimationSprites[2], 0.2f, PlayMode.LOOP, false);
		playerAnimationComp.addAnimation("IdleLeft", playerAnimationSprites[3], 0.2f, PlayMode.LOOP, false);
		playerAnimationComp.addAnimation("WalkDown", playerAnimationSprites[4], 0.1f, PlayMode.LOOP, false);
		playerAnimationComp.addAnimation("WalkUp", playerAnimationSprites[5], 0.1f, PlayMode.LOOP, false);
		playerAnimationComp.addAnimation("WalkRight", playerAnimationSprites[6], 0.1f, PlayMode.LOOP, false);
		playerAnimationComp.addAnimation("WalkLeft", playerAnimationSprites[7], 0.1f, PlayMode.LOOP, false);
		player.add(playerAnimationComp);
		
		ScriptComponent playerScriptComponent = new ScriptComponent(new PlayerScript(player));
		playerScriptComponent.eventsToListen.add("SceneChanged");
		playerScriptComponent.eventsToListen.add("ItemPicked");
		playerScriptComponent.eventsToListen.add("StartItemChecking");
		playerScriptComponent.eventsToListen.add("CookingStarted");
		playerScriptComponent.eventsToListen.add("BorrowWorld");
		playerScriptComponent.eventsToDispatch.add("FollowMe");
		playerScriptComponent.eventsToDispatch.add("TakeOrder");
		player.add(playerScriptComponent);
		// Player physics component
		PhysicsSystem physicsSystem = ecs.getSystem(PhysicsSystem.class);
		
		// Player physics component
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(playerPosition);
		bodyDef.type = BodyType.DynamicBody;
		Body body = physicsSystem.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(playerSize.x / 8.f, playerSize.y / 12.f, new Vector2(0.f, -playerSize.y / 4f - 0.2f), 0.f);
		fixtureDef.shape = shape;
		fixtureDef.filter.categoryBits = EntityBits.PLAYER_B2D_BIT;
		fixtureDef.filter.maskBits = 0xFF;
		body.createFixture(fixtureDef).setUserData(playerScriptComponent.script);
		shape.dispose();
		player.add(new PhysicsComponent(body));
		
		GuiComponent playerInventory = new GuiComponent();
		playerInventory.actors.setVisible(false);
		Pixmap backgroundBlack = new Pixmap(1, 1, Format.RGBA8888);
		backgroundBlack.setColor(0.f, 0.f, 0.f, 0.2f);
		backgroundBlack.fill();
		Texture borderTexture = new Texture(Gdx.files.internal("InventoryItemBorder.png"));
		LabelStyle labelStyle = new LabelStyle(TimmysTavern.font, Color.WHITE);
		
		final float stageWidth = ecs.getSystem(RenderingSystem.class).ui.getWidth();
		final float stageHeight = ecs.getSystem(RenderingSystem.class).ui.getHeight();
		
		final float inventorySlotSize = stageWidth / 30.f;
		final float firstImageX = stageWidth - inventorySlotSize * 6;
		final float firstImageY = stageHeight - inventorySlotSize * 6;
		
		for (int i = 4; i >= 0; --i) {
			for (int j = 0; j < 5; ++j) {
				Group inventoryItemGroup = new Group();
				inventoryItemGroup.setPosition(firstImageX + j * inventorySlotSize, firstImageY + i * inventorySlotSize);
				inventoryItemGroup.setSize(inventorySlotSize, inventorySlotSize);
				Image border = new Image(borderTexture);
				border.setFillParent(true);
				Label label = new Label("", labelStyle);
				label.setFillParent(true);
				label.setVisible(false);
				label.setAlignment(Align.bottomLeft);
				label.setFontScale(2.f);
				Image emptyImage = new Image();
				inventoryItemGroup.addActor(border);
				inventoryItemGroup.addActor(emptyImage);
				inventoryItemGroup.addActor(label);
				playerInventory.actors.addActor(inventoryItemGroup);
			}
		}
		
		backgroundBlack.dispose();
		player.add(playerInventory);
		
		SoundComponent playerSoundComp = new SoundComponent();
		playerSoundComp.addSound("inventoryOpen", Gdx.files.internal("RPGsounds_Kenney\\OGG\\bookOpen.ogg"), false, false);
		player.add(playerSoundComp);
		
		Entity cameraEntity = new Entity();
		float screenAspectRatio = (float)Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
		CameraComponent camComp = new CameraComponent(30.f * screenAspectRatio, 30.f);
		camComp.camera.position.set(playerPosition, 0.f);
		camComp.xLimits = new Vector2(0.f, 100f);
		camComp.yLimits = new Vector2(0.f, 75f);
		cameraEntity.add(camComp);
		ScriptComponent camScriptComp = (ScriptComponent)cameraEntity.addAndReturn(new ScriptComponent(new FollowingCameraScript(cameraEntity)));
		camScriptComp.eventsToListen.add("SceneChanged");
		camScriptComp.eventsToListen.add("FollowMe");
		
		loadVillageEntities(villageEntities, addEntityComps);
		loadTavernEntities(tavernEntities, addEntityComps);
		
		addEntityComps.get(0).entitiesToAdd = tavernEntities;
		addEntityComps.get(1).entitiesToAdd = villageEntities;
		
		ecs.addEntity(cameraEntity);
		ecs.addEntity(player);
		for (Entity e : villageEntities) {
			ecs.addEntity(e);
		}
	}
	
	private void loadVillageEntities(ArrayList<Entity> villageEntities, ArrayList<AddEntityComponent> newSceneComps) {	
		Entity mapEntity = new Entity();
		MapComponent mapComp = new MapComponent("Maps/Village.tmx");
		mapComp.setLastBackgroundLayerToRenderIndex(5);
		mapEntity.add(mapComp);
		mapEntity.add(new DestroyEntityComponent());
		
		// Background music
		MusicComponent musicComp = (MusicComponent)mapEntity.addAndReturn(new MusicComponent(Gdx.files.internal("PGS Fantasy RPG Music Pack/Town-Village Theme 1.ogg")));
		musicComp.music.setLooping(true);
		musicComp.shouldPlay = true;
		musicComp.music.setVolume(0.2f);

		PhysicsSystem physicsSystem = ecs.getSystem(PhysicsSystem.class);
		
		float mapScalingFactor = 1f / mapComp.map.getProperties().get("tilewidth", Integer.class);
		Vector2 mapSize = mapComp.getMapSize();
		
		Body body = null;
		BodyDef bodyDef = null;
		FixtureDef fixtureDef = null;
		PolygonShape polyShape = null;
		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(mapSize.x * mapScalingFactor / 2.f, mapSize.y * mapScalingFactor / 2.f);
		body = physicsSystem.createBody(bodyDef);
				
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
		
		mapEntity.add(new PhysicsComponent(body));
		villageEntities.add(mapEntity);
		
		for (RectangleMapObject object : mapComp.map.getLayers().get("Interactables").getObjects().getByType(RectangleMapObject.class)) {
			Entity interactable = new Entity();
			Rectangle rect = object.getRectangle();
			rect.set(rect.x * mapScalingFactor, rect.y * mapScalingFactor, rect.width * mapScalingFactor, rect.height * mapScalingFactor);
			bodyDef = new BodyDef();
			bodyDef.position.set(rect.getX() + rect.getWidth() / 2.f, rect.getY() + rect.getHeight() / 2.f);
			bodyDef.type = BodyDef.BodyType.StaticBody;
			body = physicsSystem.createBody(bodyDef);
			polyShape = new PolygonShape();
			polyShape.setAsBox(rect.getWidth() / 2f, rect.getHeight() / 2f);
			fixtureDef = new FixtureDef();
			fixtureDef.shape = polyShape;
			fixtureDef.filter.categoryBits = EntityBits.INTERACTABLE_B2D_BIT;
			fixtureDef.filter.groupIndex = -EntityBits.STATIC_SCENERY_B2D_GROUP;
			fixtureDef.filter.maskBits = EntityBits.PLAYER_B2D_BIT;
			
			interactable.add(new PhysicsComponent(body));
			
			switch (object.getName()) {
				case "TavernDoor":
				{
					interactable.add(new ScriptComponent(new DoorScript(interactable, "tavern")));
					AddEntityComponent addEntityComp = (AddEntityComponent)interactable.addAndReturn(new AddEntityComponent());
					newSceneComps.add(addEntityComp);
					fixtureDef.isSensor = true;
					break;
				}
				case "apple":
				{
					interactable.add(new ItemComponent(Item.APPLE));
					interactable.add(new ScriptComponent(new ItemScript(interactable, 20, 60.f)));
					SoundComponent itemSoundComp = new SoundComponent(); 
					fixtureDef.isSensor = true;
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					break;
				}
				case "cranberry":
				{
					interactable.add(new ItemComponent(Item.CRANBERRY));
					interactable.add(new ScriptComponent(new ItemScript(interactable, 40, 10f)));
					SoundComponent itemSoundComp = new SoundComponent(); 
					fixtureDef.isSensor = true;
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					break;
				}
				case "blueberry":
				{
					interactable.add(new ItemComponent(Item.BLUEBERRY));
					interactable.add(new ScriptComponent(new ItemScript(interactable, 40, 10f)));
					SoundComponent itemSoundComp = new SoundComponent(); 
					fixtureDef.isSensor = true;
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					break;
				}
				case "tomato":
				{
					interactable.add(new ItemComponent(Item.TOMATO));
					interactable.add(new ScriptComponent(new ItemScript(interactable, 20, 60.f)));
					SoundComponent itemSoundComp = new SoundComponent(); 
					fixtureDef.isSensor = false;
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					break;
				}
				case "tuna":
				{
					interactable.add(new ItemComponent(Item.TUNA));
					interactable.add(new ScriptComponent(new ItemScript(interactable, 40, 10f)));
					SoundComponent itemSoundComp = new SoundComponent(); 
					fixtureDef.isSensor = true;
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					break;
				}
				case "eggplant":
				{
					interactable.add(new ItemComponent(Item.EGGPLANT));
					interactable.add(new ScriptComponent(new ItemScript(interactable, 40, 10f)));
					SoundComponent itemSoundComp = new SoundComponent(); 
					fixtureDef.isSensor = true;
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					break;
				}
				case "pepper":
				{
					interactable.add(new ItemComponent(Item.PEPPER));
					interactable.add(new ScriptComponent(new ItemScript(interactable, 20, 60.f)));
					SoundComponent itemSoundComp = new SoundComponent(); 
					fixtureDef.isSensor = false;
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					break;
				}
				case "broccoli":
				{
					interactable.add(new ItemComponent(Item.BROCCOLI));
					interactable.add(new ScriptComponent(new ItemScript(interactable, 40, 10f)));
					SoundComponent itemSoundComp = new SoundComponent(); 
					fixtureDef.isSensor = false;
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					break;
				}
				case "carrot":
				{
					interactable.add(new ItemComponent(Item.CARROT));
					interactable.add(new ScriptComponent(new ItemScript(interactable, 40, 10f)));
					SoundComponent itemSoundComp = new SoundComponent(); 
					fixtureDef.isSensor = false;
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					break;
				}
				case "trout":
				{
					interactable.add(new ItemComponent(Item.TROUT));
					interactable.add(new ScriptComponent(new ItemScript(interactable, 40, 10f)));
					SoundComponent itemSoundComp = new SoundComponent(); 
					fixtureDef.isSensor = true;
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					break;
				}
				case "corn":
				{
					interactable.add(new ItemComponent(Item.CORN));
					interactable.add(new ScriptComponent(new ItemScript(interactable, 40, 10f)));
					SoundComponent itemSoundComp = new SoundComponent(); 
					fixtureDef.isSensor = false;
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					break;
				}
				case "potato":
				{
					interactable.add(new ItemComponent(Item.POTATO));
					interactable.add(new ScriptComponent(new ItemScript(interactable, 40, 10f)));
					SoundComponent itemSoundComp = new SoundComponent(); 
					fixtureDef.isSensor = false;
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					break;
				}
			}
			
			body.createFixture(fixtureDef).setUserData(interactable.getComponent(ScriptComponent.class).script);
			polyShape.dispose();
			interactable.add(new DestroyEntityComponent());
			villageEntities.add(interactable);
		}
	}
	
	void loadTavernEntities(ArrayList<Entity> tavernEntities, ArrayList<AddEntityComponent> addEntityComps) {
		Entity mapEntity = new Entity();
		mapEntity.add(new DestroyEntityComponent());
		MapComponent mapComp = new MapComponent("Maps/Tavern.tmx");
		mapEntity.add(mapComp);
		
		// Script creation
		mapEntity.add(new ScriptComponent(new TavernScript(mapEntity)));
		((AddEntityComponent)mapEntity.addAndReturn(new AddEntityComponent())).sceneChange = false;
	
		// Background music
		MusicComponent musicComp = (MusicComponent)mapEntity.addAndReturn(new MusicComponent(Gdx.files.internal("PGS Fantasy RPG Music Pack/Town-Village Theme 3.ogg")));
		musicComp.music.setLooping(true);
		musicComp.shouldPlay = true;
		musicComp.music.setVolume(0.2f);

		PhysicsSystem physicsSystem = ecs.getSystem(PhysicsSystem.class);
		
		float mapScalingFactor = 1f / mapComp.map.getProperties().get("tilewidth", Integer.class);
		Vector2 mapSize = mapComp.getMapSize();
		
		Body body = null;
		BodyDef bodyDef = null;
		FixtureDef fixtureDef = null;
		PolygonShape polyShape = null;
		
		bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;
		bodyDef.position.set(mapSize.x / 2.f, mapSize.y / 2.f);
		body = physicsSystem.createBody(bodyDef);
		
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
			body.setActive(false);
			polyShape.dispose();
		}
		
		mapEntity.add(new PhysicsComponent(body));
		tavernEntities.add(mapEntity);
		
		for (RectangleMapObject object : mapComp.map.getLayers().get("Interactables").getObjects().getByType(RectangleMapObject.class)) {
			Entity interactable = new Entity();
			Rectangle rect = object.getRectangle();
			rect.set(rect.x * mapScalingFactor, rect.y * mapScalingFactor, rect.width * mapScalingFactor, rect.height * mapScalingFactor);
			bodyDef = new BodyDef();
			bodyDef.position.set(rect.getX() + rect.getWidth() / 2.f, rect.getY() + rect.getHeight() / 2.f);
			bodyDef.type = BodyDef.BodyType.StaticBody;
			body = physicsSystem.createBody(bodyDef);
			body.setActive(false);
			polyShape = new PolygonShape();
			polyShape.setAsBox(rect.getWidth() / 2f, rect.getHeight() / 2f);
			fixtureDef = new FixtureDef();
			fixtureDef.shape = polyShape;
			fixtureDef.isSensor = true;
			fixtureDef.filter.categoryBits = EntityBits.INTERACTABLE_B2D_BIT;
			fixtureDef.filter.groupIndex = -EntityBits.STATIC_SCENERY_B2D_GROUP;
			fixtureDef.filter.maskBits = EntityBits.PLAYER_B2D_BIT;
			
			interactable.add(new PhysicsComponent(body));
			
			switch (object.getName()) {
				case "TavernDoor":
				{
					DoorScript tavernDoorScript = new DoorScript(interactable, "village");
					interactable.add(new ScriptComponent(tavernDoorScript));
					AddEntityComponent addEntityComp = (AddEntityComponent)interactable.addAndReturn(new AddEntityComponent());
					addEntityComps.add(addEntityComp);
					body.createFixture(fixtureDef).setUserData(interactable.getComponent(ScriptComponent.class).script);
					break;
				}
				case "Cooker1":
				case "Cooker2":
				{
					SoundComponent itemSoundComp = new SoundComponent(); 
					itemSoundComp.addSound("ItemPicked", Gdx.files.internal(Item.itemPickingSoundFile), false, false);
					interactable.add(itemSoundComp);
					interactable.add(new ScriptComponent(new CookerScript(interactable)));
					body.createFixture(fixtureDef).setUserData(interactable.getComponent(ScriptComponent.class).script);
					break;
				}
			}
			
			polyShape.dispose();
			interactable.add(new DestroyEntityComponent());
			tavernEntities.add(interactable);
		}
	}
	
	@Override
	public void show() {
	}

	@Override
	public void render(float delta) {
		ecs.update(delta);
		
		if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
			dispose();
			game.setScreen(new MainMenuScreen(game));
		}
	}

	@Override
	public void resize(int width, int height) {
		ecs.getSystem(RenderingSystem.class).ui.getViewport().setScreenSize(width, height);
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		ecs.removeAllEntities();
		
		for (Entity e : villageEntities) {
			for (Component c : e.getComponents()) {
				if (c instanceof Disposable) {
					((Disposable) c).dispose();
				}
			}
		}
		
		for (Entity e : tavernEntities) {
			for (Component c : e.getComponents()) {
				if (c instanceof Disposable) {
					((Disposable) c).dispose();
				}
			}
		}
		
		for (EntitySystem es : ecs.getSystems()) {
			if (es instanceof Disposable) {
				((Disposable) es).dispose();
			}
		}
	}

}
