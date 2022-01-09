package screens;

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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.gdx.game.TimmysTavern;

import components.AnimationComponent;
import components.CameraComponent;
import components.EntityBits;
import components.GuiComponent;
import components.MapComponent;
import components.MusicComponent;
import components.NewSceneComponent;
import components.PhysicsComponent;
import components.ScriptComponent;
import components.SoundComponent;
import components.SpriteComponent;
import scripts.FollowingCameraScript;
import scripts.PlayerScript;
import scripts.Script;
import scripts.VillageSceneScript;
import systems.AudioSystem;
import systems.PhysicsSystem;
import systems.RenderingSystem;
import systems.ScriptingSystem;

public class GameScreen implements Screen {
	private TimmysTavern game;
	private Engine ecs;

	public GameScreen(TimmysTavern game) {
		this.game = game;
		ecs = new Engine();
		ecs.addSystem(new RenderingSystem(game.batch, 2));
		ecs.addSystem(new PhysicsSystem(new Vector2(0.f, 0.f), 0));
		ecs.addSystem(new ScriptingSystem(ecs.getSystem(PhysicsSystem.class).getBodyCreator(), 1));
		ecs.addSystem(new AudioSystem(3));
		addListeners();
		loadInitialScene();
		ecs.addEntityListener(Family.all(GuiComponent.class).get(), new EntityListener() {
			
			@Override
			public void entityRemoved(Entity entity) {
			}
			
			@Override
			public void entityAdded(Entity entity) {
				 ecs.getSystem(RenderingSystem.class).addGuiElement(entity.getComponent(GuiComponent.class).actors);
			}
		});
	}
	
	private void addListeners() {
		addDisposeEntityListener(MapComponent.class);
		addDisposeEntityListener(SpriteComponent.class);
		addDisposeEntityListener(AnimationComponent.class);
		addDisposeEntityListener(PhysicsComponent.class);
		addDisposeEntityListener(GuiComponent.class);
		addDisposeEntityListener(MusicComponent.class);
		addDisposeEntityListener(SoundComponent.class);
	}
	
	private <T extends Component & Disposable> void addDisposeEntityListener(final Class<T> cls) {
		ecs.addEntityListener(Family.all(cls).get(), new EntityListener() {
			@Override
			public void entityRemoved(Entity entity) {
				entity.getComponent(cls).dispose();
			}
			
			@Override
			public void entityAdded(Entity entity) {}
		});
	}
	
	private void loadInitialScene() {
		Entity player = new Entity();
		player.flags |= EntityBits.PLAYER_BIT;
		float playerWHRation = 20f / 28f;
		Vector2 playerPosition = new Vector2(3.f, 12.f);
		Vector2 playerSize = new Vector2(2 * playerWHRation, 2f);
		
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
		
		// Player physics component
		PhysicsSystem physicsSystem = ecs.getSystem(PhysicsSystem.class);
		PhysicsComponent playerPhysicsComp = (PhysicsComponent)player.addAndReturn(new PhysicsComponent());
		
		// Player physics component
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(playerPosition);
		bodyDef.type = BodyType.DynamicBody;
		playerPhysicsComp.body = physicsSystem.createBody(bodyDef);
		
		FixtureDef fixtureDef = new FixtureDef();
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(playerSize.x / 8.f, playerSize.y / 12.f, new Vector2(0.f, -playerSize.y / 4f - 0.2f), 0.f);
		fixtureDef.shape = shape;
		fixtureDef.filter.categoryBits = EntityBits.PLAYER_B2D_BIT;
		fixtureDef.filter.maskBits = 0xFF;
		playerPhysicsComp.body.createFixture(fixtureDef).setUserData(player);
		shape.dispose();
		
		playerPhysicsComp.body.setTransform(new Vector2(48.f, 8.f), 0f);
		
		ScriptComponent playerScriptComponent = new ScriptComponent(new PlayerScript());
		playerScriptComponent.eventsToListen.add("SceneChanged");
		playerScriptComponent.eventsToListen.add("ItemPicked");
		playerScriptComponent.eventsToDispatch.add("FollowMe");
		player.add(playerScriptComponent);
		
		GuiComponent playerInventory = new GuiComponent();
		playerInventory.actors.setVisible(false);
		ecs.getSystem(RenderingSystem.class).addGuiElement(playerInventory.actors);
		Pixmap backgroundBlack = new Pixmap(1, 1, Format.RGBA8888);
		backgroundBlack.setColor(0.f, 0.f, 0.f, 0.2f);
		backgroundBlack.fill();
		Texture borderTexture = new Texture(Gdx.files.internal("InventoryItemBorder.png"));
		LabelStyle labelStyle = new LabelStyle(game.font, Color.WHITE);
		
		final float inventorySlotSize = playerInventory.actors.getStage().getWidth() / 30.f;
		final float firstImageX = playerInventory.actors.getStage().getWidth() - inventorySlotSize * 6;
		final float firstImageY = playerInventory.actors.getStage().getHeight() - inventorySlotSize * 6;
		
		for (int i = 4; i >= 0; --i) {
			for (int j = 0; j < 5; ++j) {
				Image border = new Image(borderTexture);
				border.setPosition(firstImageX + j * inventorySlotSize, firstImageY + i * inventorySlotSize);
				border.setSize(inventorySlotSize, inventorySlotSize);
				Group inventoryItemGroup = new Group();
				Label label = new Label("", labelStyle);
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
		
		backgroundBlack.dispose();
		player.add(playerInventory);
		
		SoundComponent playerSoundComp = new SoundComponent();
		playerSoundComp.addSound("inventoryOpen", Gdx.files.internal("RPGsounds_Kenney\\OGG\\bookOpen.ogg"), false, false);
		player.add(playerSoundComp);
		ecs.addEntity(player);

		Entity transitionEntity = new Entity();
		((NewSceneComponent)transitionEntity.addAndReturn(new NewSceneComponent(new VillageSceneScript(), "village"))).load = true;
		((ScriptComponent)transitionEntity.addAndReturn(new ScriptComponent(new Script() {
			
			@Override
			public void update(Entity self, float deltaTime) {
				// TODO Auto-generated method stub
				
			}
		}))).eventsToDispatch.add("SceneChanged");
		ecs.addEntity(transitionEntity);
		
		
		Entity cameraEntity = new Entity();
		cameraEntity.flags |= EntityBits.CAMERA_BIT;
		float screenAspectRatio = (float)Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
		CameraComponent camComp = new CameraComponent(30.f * screenAspectRatio, 30.f);
		camComp.camera.position.set(playerPosition, 0.f);
		camComp.xLimits = new Vector2(0.f, 100f);
		camComp.yLimits = new Vector2(0.f, 100f);
		cameraEntity.add(camComp);
		ScriptComponent camScriptComp = (ScriptComponent)cameraEntity.addAndReturn(new ScriptComponent(new FollowingCameraScript()));
		camScriptComp.eventsToListen.add("SceneChanged");
		camScriptComp.eventsToListen.add("FollowMe");
		
//		Sprite transitionSprite = new Sprite(new TextureRegion(new Texture(Gdx.files.internal("SceneTransition.png"))));
//		transitionSprite.setSize(1.f, 1.f);
//		SpriteComponent transitionSpriteComp = new SpriteComponent(transitionSprite);
//		transitionSpriteComp.draw = false;
//		cameraEntity.add(transitionSpriteComp);
		
		ecs.addEntity(cameraEntity);
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
		// TODO Auto-generated method stub

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
		for (Entity e : ecs.getEntities()) {
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
