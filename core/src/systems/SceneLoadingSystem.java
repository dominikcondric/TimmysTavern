package systems;

import java.util.Hashtable;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Listener;
import com.badlogic.ashley.signals.Signal;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.gdx.game.Item;

import components.AnimationComponent;
import components.EntityBits;
import components.GuiComponent;
import components.ItemComponent;
import components.MapComponent;
import components.MusicComponent;
import components.PhysicsComponent;
import components.Script;
import components.ScriptComponent;
import components.SpriteComponent;

public class SceneLoadingSystem extends EntitySystem {
	private Family sceneTriggerFamily = Family.all(ScriptComponent.class).get();
	private ComponentMapper<ScriptComponent> scriptCompMapper = ComponentMapper.getFor(ScriptComponent.class);
	private BitmapFont font;
	public enum Scenes { NONE, VILLAGE, TAVERN }
	
	public SceneLoadingSystem(int priority) {
		super(priority);
		font = new BitmapFont(false);
	}
	
	@Override
	public void update(float deltaTime) {
		for (Entity e : getEngine().getEntitiesFor(sceneTriggerFamily)) {
			ScriptComponent scriptComp = scriptCompMapper.get(e);
			if (scriptComp.newSceneToLoad != Scenes.NONE) {
				switch (scriptComp.newSceneToLoad) {
					case VILLAGE:
						createVillageScene();
						break;
					case TAVERN:
						createTavernScene();
						break;
				default:
					break;
				}
			}
		}
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		createInitialEntities();
		createVillageScene();
	}

	private <T> void createInitialEntities() {
		Entity player = new Entity();
		player.flags |= EntityBits.PLAYER_BIT;
		float playerWHRation = 20f / 28f;
		Vector2 playerPosition = new Vector2(3.f, 12.f);
		Vector2 playerSize = new Vector2(2 * playerWHRation, 2f);
		
		// Player sprite/animation components
		Array<Sprite>[] playerAnimationSprites = new Array[8];
		int playerTextureRegionWidth = 20;
		int playerTextureRegionHeight = 28;
		for (int i = 0; i < 8; ++i) {
			playerAnimationSprites[i] = new Array<Sprite>(4);
			for (int j = 0; j < 4; ++j) {
				Sprite sprite = new Sprite(new TextureRegion(new Texture(Gdx.files.internal("Free Asset Pack\\Characters\\Character 1.png")), 2 + j * (playerTextureRegionWidth + 4), 4 + ((playerTextureRegionHeight + 4) * i), playerTextureRegionWidth, playerTextureRegionHeight));
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
		
		// Player physics component
		PhysicsSystem physicsSystem = getEngine().getSystem(PhysicsSystem.class);
		PhysicsComponent playerPhysicsComp = (PhysicsComponent)player.addAndReturn(new PhysicsComponent());
		
		// Player physics component
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(playerPosition);
		bodyDef.type = BodyType.DynamicBody;
		playerPhysicsComp.body = physicsSystem.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(playerSize.x / 4.f, playerSize.y / 8.f, new Vector2(0.f, -playerSize.y / 4f - 0.1f), 0.f);
		fixtureDef.shape = shape;
		fixtureDef.filter.categoryBits = EntityBits.PLAYER_B2D_BIT;
		fixtureDef.filter.maskBits = 0xFF;
		playerPhysicsComp.body.createFixture(fixtureDef).setUserData(player);
		shape.dispose();
		
		playerPhysicsComp.body.setTransform(new Vector2(48.f, 8.f), 0f);
		
		// Player script component 
		Script playerMovementScript = new Script() {
			
			class InventorySlot {
				public InventorySlot(int slot, int count) {
					slotNumber = slot;
					itemCount = count;
				}
				
				int itemCount;
				int slotNumber;
			}
			
			private int lastMovingDirection = Keys.DOWN;
			private ComponentMapper<PhysicsComponent> physicsComponentMapper = ComponentMapper.getFor(PhysicsComponent.class);
			private ComponentMapper<AnimationComponent> animationComponentMapper = ComponentMapper.getFor(AnimationComponent.class);
			private ComponentMapper<ItemComponent> itemComponentMapper = ComponentMapper.getFor(ItemComponent.class);
			private Hashtable<String, InventorySlot> inventory = new Hashtable<String, InventorySlot>();
			private boolean inventoryOpen = false;
			
			@Override
			public void update(Entity self, float deltaTime) {
				if (Gdx.input.isKeyJustPressed(Keys.I)) {
					inventoryOpen = !inventoryOpen;
					self.getComponent(GuiComponent.class).actors.setVisible(inventoryOpen);
				}
				
				Body body = physicsComponentMapper.get(self).body;
				
				body.setLinearVelocity(0.f, 0.f);
				
				int newMovingDirection = 0;
				int directionKeys[] = { Keys.UP, Keys.DOWN, Keys.RIGHT, Keys.LEFT };
				
				if (lastMovingDirection != 0) {
					for (int i = 0; i < directionKeys.length; ++i) {
						if (directionKeys[i] == lastMovingDirection) {
							directionKeys[i] = directionKeys[0];
							directionKeys[0] = lastMovingDirection;
							break;
						}
					}
				}
				
				for (int key : directionKeys) {
					if (Gdx.input.isKeyPressed(key)) {
						switch (key) {
							case Keys.RIGHT:
								body.setLinearVelocity(4f, 0f);
								newMovingDirection = Keys.RIGHT;
								break;
							case Keys.LEFT:
								body.setLinearVelocity(-4f, 0f);
								newMovingDirection = Keys.LEFT;
								break;
							case Keys.UP:
								body.setLinearVelocity(0f, 4f);
								newMovingDirection = Keys.UP;
								break;
							case Keys.DOWN:
								body.setLinearVelocity(0f, -4f);
								newMovingDirection = Keys.DOWN;
								break;
						}
						
						break;
					}
				}
				
				if (Gdx.input.isKeyPressed(Keys.SHIFT_LEFT))
					body.setLinearVelocity(body.getLinearVelocity().scl(2.f));
				
				AnimationComponent animationComponent = animationComponentMapper.get(self);
				if (newMovingDirection != 0 && lastMovingDirection != newMovingDirection) {
					switch (newMovingDirection) {
						case Keys.DOWN:
							animationComponent.setActiveAnimation("WalkDown", true);
							break;
						case Keys.UP:
							animationComponent.setActiveAnimation("WalkUp", true);
							break;
						case Keys.RIGHT:
							animationComponent.setActiveAnimation("WalkRight", true);
							break;
						case Keys.LEFT:
							animationComponent.setActiveAnimation("WalkLeft", true);
							break;
					}
				} else if (newMovingDirection == 0 && lastMovingDirection != 0) {
					switch (lastMovingDirection) {
						case Keys.DOWN:
							animationComponent.setActiveAnimation("IdleDown", true);
							break;
						case Keys.UP:
							animationComponent.setActiveAnimation("IdleUp", true);
							break;
						case Keys.RIGHT:
							animationComponent.setActiveAnimation("IdleRight", true);
							break;
						case Keys.LEFT:
							animationComponent.setActiveAnimation("IdleLeft", true);
							break;
					}
				}
				
				lastMovingDirection = newMovingDirection;
			}
			
			@Override
			public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {}

			@Override
			public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {}

			@Override
			public void onEventListen(Entity self, Entity sender, String eventName) {
				if (eventName.contentEquals("SceneChanged")) {
					switch (scriptCompMapper.get(sender).newSceneToLoad) {
						case VILLAGE:
							self.getComponent(PhysicsComponent.class).body.setTransform(new Vector2(48.f, 8.f),  0.f);
							self.getComponent(AnimationComponent.class).setActiveAnimation("IdleDown", false);
							break;
						
						case TAVERN:
							self.getComponent(PhysicsComponent.class).body.setTransform(new Vector2(10.f, 2.f),  0.f);
							self.getComponent(AnimationComponent.class).setActiveAnimation("IdleUp", false);
							break;
						
						case NONE:
							break;
					}
					
					scriptCompMapper.get(self).eventsToListen.add("SceneChanged");
				}
				
				if (eventName.contentEquals("ItemPicked")) {
					Item pickedItem = itemComponentMapper.get(sender).item;
					GuiComponent inventoryGui = self.getComponent(GuiComponent.class);
					if (inventory.containsKey(pickedItem.name)) {
						InventorySlot slot = inventory.get(pickedItem.name);
						slot.itemCount++;
						Label slotLabel = (Label)((Group)inventoryGui.actors.getChild(slot.slotNumber)).getChild(2);
						if (slot.itemCount > 1) {
							slotLabel.setVisible(true);
							slotLabel.setText(Integer.toString(slot.itemCount));
						}
					} else {
						Group inventorySlot = ((Group)inventoryGui.actors.getChild(inventory.size()));
						inventory.put(pickedItem.name, new InventorySlot(inventory.size(), 1));
						Image itemImage = (Image)inventorySlot.getChild(1);
						Image borderImage =  (Image)inventorySlot.getChild(0);
						itemImage.setPosition(borderImage.getX(), borderImage.getY());
						itemImage.setSize(borderImage.getWidth(), borderImage.getHeight());
						itemImage.setDrawable(new TextureRegionDrawable(pickedItem.getTextureRegion()));
					}
					
					scriptCompMapper.get(self).eventsToListen.add("ItemPicked");
				}
			}
		};
		
		ScriptComponent playerScriptComponent = new ScriptComponent(playerMovementScript);
		playerScriptComponent.eventsToListen.add("SceneChanged");
		playerScriptComponent.eventsToListen.add("ItemPicked");
		player.add(playerScriptComponent);
		
		GuiComponent playerInventory = new GuiComponent();
		playerInventory.actors.setVisible(false);
		getEngine().getSystem(RenderingSystem.class).addGuiElement(playerInventory.actors);
		Pixmap backgroundBlack = new Pixmap(1, 1, Format.RGBA8888);
		backgroundBlack.setColor(0.f, 0.f, 0.f, 0.2f);
		backgroundBlack.fill();
		Texture borderTexture = new Texture(Gdx.files.internal("InventoryItemBorder.png"));
		LabelStyle labelStyle = new LabelStyle(font, Color.WHITE);
		
		final float inventorySlotSize = 60f;
		final float firstImageX = playerInventory.actors.getStage().getWidth() / 2f - inventorySlotSize * 5f;
		final float firstImageY = playerInventory.actors.getStage().getHeight() / 4f;
		
		for (int i = 2; i >= 0; --i) {
			for (int j = 0; j < 10; ++j) {
				Image border = new Image(borderTexture);
				border.setPosition(firstImageX + j * inventorySlotSize, firstImageY + i * inventorySlotSize);
				border.setSize(inventorySlotSize, inventorySlotSize);
				Group inventoryItemGroup = new Group();
				Label label = new Label("0", labelStyle);
				label.setFontScale(2f);
				label.setPosition(firstImageX + j * inventorySlotSize + inventorySlotSize * (2f/3f), firstImageY + i * inventorySlotSize + inventorySlotSize * (1f/4f));
				label.setSize(10f, 10f);
				label.setVisible(false);
				Image emptyImage = new Image();
				emptyImage.setPosition(firstImageX + j * inventorySlotSize, firstImageY + i * inventorySlotSize);
				emptyImage.setSize(inventorySlotSize, inventorySlotSize);
				inventoryItemGroup.addActor(border);
				inventoryItemGroup.addActor(new Image());
				inventoryItemGroup.addActor(label);
				playerInventory.actors.addActor(inventoryItemGroup);
			}
		}
		
		player.add(playerInventory);
		getEngine().addEntity(player);
		backgroundBlack.dispose();
	}
	
	private void createVillageScene() {
		for (Entity e : getEngine().getEntities()) {
			if ((e.flags & EntityBits.PLAYER_BIT) == 0) {
				getEngine().removeEntity(e);
			}
		}
		
		Entity mapEntity = new Entity();
		MapComponent mapComp = new MapComponent("Maps/Village.tmx");
		mapComp.setLastBackgroundLayerToRenderIndex(5);
		mapEntity.add(mapComp);
		
		// Background music
		MusicComponent musicComp = (MusicComponent)mapEntity.addAndReturn(new MusicComponent(Gdx.files.internal("PGS Fantasy RPG Music Pack/Town-Village Theme 1.ogg")));
		musicComp.music.setLooping(true);
		musicComp.shouldPlay = true;
		musicComp.music.setVolume(0.7f);

		PhysicsSystem physicsSystem = getEngine().getSystem(PhysicsSystem.class);
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
		
		mapPhysicsComp.body = body;
		getEngine().addEntity(mapEntity);
		
		Script doorsScript = new Script() {
			private boolean openable = false;
			
			@Override
			public void update(Entity self, float deltaTime) {
				if (openable && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
					ScriptComponent scriptComp = self.getComponent(ScriptComponent.class);
					scriptComp.eventsToDispatch.add("SceneChanged");
					scriptComp.newSceneToLoad = Scenes.TAVERN;
				}
			}
			
			@Override
			public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {
				if ((((Entity)other.getUserData()).flags & EntityBits.PLAYER_BIT) != 0) {
					openable = false;
				}
			}
			
			@Override
			public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {
				if ((((Entity)other.getUserData()).flags & EntityBits.PLAYER_BIT) != 0) {
					openable = true;
				}
			}

			@Override
			public void onEventListen(Entity self, Entity sender, String eventName) {}
		};
		
		Script appleTreesScript = new Script() {
			private final int maxApples = 20;
			private int applesAvailable = 20;
			private boolean pickable = false;
			private float newAppleTimer = 0.f;
			
			@Override
			public void update(Entity self, float deltaTime) {
				if (pickable && applesAvailable > 0 && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
					self.getComponent(ScriptComponent.class).eventsToDispatch.add("ItemPicked");
					--applesAvailable;
				}
				
				newAppleTimer += deltaTime;
				if (newAppleTimer >= 60f) {
					applesAvailable = Math.min(applesAvailable + 1, maxApples);
					newAppleTimer = 0.f;
				}
			}
			
			@Override
			public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {
				if ((((Entity)other.getUserData()).flags & EntityBits.PLAYER_BIT) != 0) {
					pickable = false;
				}
			}
			
			@Override
			public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {
				if ((((Entity)other.getUserData()).flags & EntityBits.PLAYER_BIT) != 0) {
					pickable = true;
				}
			}

			@Override
			public void onEventListen(Entity self, Entity sender, String eventName) {
				// TODO Auto-generated method stub
			}
		};
		
		for (RectangleMapObject object : mapComp.map.getLayers().get("Interactables").getObjects().getByType(RectangleMapObject.class)) {
			Entity interactable = new Entity();
			PhysicsComponent interactablePhysicsComp = new PhysicsComponent();
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
			fixtureDef.isSensor = true;
			fixtureDef.filter.categoryBits = EntityBits.INTERACTABLE_B2D_BIT;
			fixtureDef.filter.groupIndex = -EntityBits.STATIC_SCENERY_B2D_GROUP;
			fixtureDef.filter.maskBits = EntityBits.PLAYER_B2D_BIT;
			
			interactablePhysicsComp.body = body;
			body.createFixture(fixtureDef).setUserData(interactable);
			polyShape.dispose();
			
			switch (object.getName()) {
				case "TavernDoor":
					interactable.add(new ScriptComponent(doorsScript));
					getEngine().addEntity(interactable);
					break;
					
				case "apple":
					interactable.add(new ScriptComponent(appleTreesScript));
					interactable.add(new ItemComponent(Item.APPLE));
					getEngine().addEntity(interactable);
					break;
			}
		}
	}
	
	private void createTavernScene() {
		for (Entity e : getEngine().getEntities()) {
			if ((e.flags & EntityBits.PLAYER_BIT) == 0) {
				getEngine().removeEntity(e);
			}
		}
		
		Entity mapEntity = new Entity();
		MapComponent mapComp = new MapComponent("Maps/Tavern.tmx");
		mapEntity.add(mapComp);
		
		// Background music
		MusicComponent musicComp = (MusicComponent)mapEntity.addAndReturn(new MusicComponent(Gdx.files.internal("PGS Fantasy RPG Music Pack/Town-Village Theme 3.ogg")));
		musicComp.music.setLooping(true);
		musicComp.shouldPlay = true;
		musicComp.music.setVolume(0.7f);

		PhysicsSystem physicsSystem = getEngine().getSystem(PhysicsSystem.class);
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
			polyShape.dispose();
		}
		
		mapPhysicsComp.body = body;
		getEngine().addEntity(mapEntity);
		
		Entity tavernDoor = new Entity();
		PhysicsComponent tavernDoorPhysicsComp = new PhysicsComponent();
		Rectangle tavernDoorRect = ((RectangleMapObject)mapComp.map.getLayers().get("Interactables").getObjects().get("TavernDoor")).getRectangle();
		tavernDoorRect.set(tavernDoorRect.x * mapScalingFactor, tavernDoorRect.y * mapScalingFactor, tavernDoorRect.width * mapScalingFactor, tavernDoorRect.height * mapScalingFactor);
		bodyDef = new BodyDef();
		bodyDef.position.set(tavernDoorRect.getX() + tavernDoorRect.getWidth() / 2.f, tavernDoorRect.getY() + tavernDoorRect.getHeight() / 2.f);
		bodyDef.type = BodyDef.BodyType.StaticBody;
		body = physicsSystem.createBody(bodyDef);
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
		
		Script tavernDoorScript = new Script() {
			private boolean openable = false;
			
			
			@Override
			public void update(Entity self, float deltaTime) {
				if (openable && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
					ScriptComponent scriptComp = self.getComponent(ScriptComponent.class);
					scriptComp.eventsToDispatch.add("SceneChanged");
					scriptComp.newSceneToLoad = Scenes.VILLAGE;
				}
			}
			
			@Override
			public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {
				if ((((Entity)other.getUserData()).flags & EntityBits.PLAYER_BIT) != 0) {
					openable = false;
				}
			}
			
			@Override
			public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {
				if ((((Entity)other.getUserData()).flags & EntityBits.PLAYER_BIT) != 0) {
					openable = true;
				}
			}

			@Override
			public void onEventListen(Entity self, Entity sender, String eventName) {}
		};
		
		tavernDoor.add(new ScriptComponent(tavernDoorScript));
		getEngine().addEntity(tavernDoor);
	}
}
