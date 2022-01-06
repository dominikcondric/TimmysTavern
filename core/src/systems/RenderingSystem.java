package systems;


import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import components.AnimationComponent;
import components.EntityBits;
import components.MapComponent;
import components.SpriteComponent;

public class RenderingSystem extends EntitySystem implements Disposable {
	private Family spriteComponentsFamily = Family.all(SpriteComponent.class).get();
	private Family mapComponentFamily = Family.all(MapComponent.class).get();
	private Family animationComponentFamily = Family.all(AnimationComponent.class, SpriteComponent.class).get();
	private ComponentMapper<SpriteComponent> spriteCompMapper = ComponentMapper.getFor(SpriteComponent.class);
	private ComponentMapper<AnimationComponent> animationCompMapper = ComponentMapper.getFor(AnimationComponent.class);
	private ComponentMapper<MapComponent> mapCompMapper = ComponentMapper.getFor(MapComponent.class);
	private SpriteBatch batch;
	private OrthogonalTiledMapRenderer mapRenderer;
	private Box2DDebugRenderer b2DRenderer;
	private OrthographicCamera cam;
	private Stage ui; 
	public boolean drawDebugPhysics = false;
	
	public RenderingSystem(SpriteBatch batch, int priority) {
		super(priority);
		this.batch = batch;
		float aspectRatio = (float)Gdx.graphics.getWidth() / Gdx.graphics.getHeight();
		cam = new OrthographicCamera();
		cam.setToOrtho(false, 30.f * aspectRatio, 30.f);
		cam.update();
		ui = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), batch);
		Gdx.input.setInputProcessor(ui);
		b2DRenderer = new Box2DDebugRenderer();
		mapRenderer = new OrthogonalTiledMapRenderer(null, 1f / 32f, batch);
	}
	
	@Override
	public void update(float deltaTime) {
		Engine engine = getEngine();
		if (Gdx.input.isKeyJustPressed(Keys.P)) {
			drawDebugPhysics = !drawDebugPhysics;
		}
		
		for (Entity e : engine.getEntitiesFor(animationComponentFamily)) {
			AnimationComponent animationComp = animationCompMapper.get(e);
			spriteCompMapper.get(e).setSprite(animationComp.getActiveSprite(deltaTime));
		}
		
		Vector2 playerPosition = null;
		Vector2 mapSize = null;
		batch.begin();
		for (Entity e : engine.getEntitiesFor(mapComponentFamily)) {
			MapComponent mapComp = mapCompMapper.get(e);
			mapRenderer.setMap(mapComp.map);
			mapRenderer.setView(cam);
			mapSize = mapComp.getMapSize();
			for (int i = 0; i < mapComp.getLastBackgroundLayerToRenderIndex(); ++i) {
				mapRenderer.renderTileLayer(mapComp.getTileLayers().get(i));
			}
		}
		
		for (Entity e : engine.getEntitiesFor(spriteComponentsFamily)) {
			SpriteComponent spriteComp = spriteCompMapper.get(e);
			spriteComp.draw(batch);
			if ((e.flags & EntityBits.PLAYER_BIT) != 0) {
				playerPosition = spriteComp.position;
			}
		}
		
		for (Entity e : engine.getEntitiesFor(mapComponentFamily)) {
			MapComponent mapComp = mapCompMapper.get(e);
			mapRenderer.setMap(mapComp.map);
			mapSize = mapComp.getMapSize();
			mapRenderer.setView(cam);
			for (int i = mapComp.getLastBackgroundLayerToRenderIndex(); i < mapComp.getTileLayers().size; ++i) {
				mapRenderer.renderTileLayer(mapComp.getTileLayers().get(i));
			}
		}
		
		updateCamera(playerPosition, mapSize.scl(mapRenderer.getUnitScale()));
		batch.setProjectionMatrix(cam.combined);
		batch.end();
		
		ui.act(deltaTime);
		ui.draw();
		
		if (drawDebugPhysics)
			engine.getSystem(PhysicsSystem.class).drawDebugPhysics(b2DRenderer, cam.combined);
	}
	
	public void addGuiElement(Actor actor) {
		ui.addActor(actor);
	}
	
	public void updateCamera(Vector2 playerPosition, Vector2 mapSize) {
		float windowAspectRatio = (float)Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
		cam.viewportWidth = Math.max(20.f, mapSize.x / 3f);
		cam.viewportHeight = Math.max(20.f * windowAspectRatio, mapSize.y / 3f * windowAspectRatio);
		
		if (playerPosition.x - cam.viewportWidth / 2f < 0f) {
			cam.position.x = cam.viewportWidth / 2f;
		} else if (playerPosition.x + cam.viewportWidth / 2.f > mapSize.x) {
			cam.position.x = mapSize.x - cam.viewportWidth / 2f;
		} else {
			cam.position.x = playerPosition.x;
		}
		
		if (playerPosition.y - cam.viewportHeight / 2f < 0f) {
			cam.position.y = cam.viewportHeight / 2f;
		} else if (playerPosition.y + cam.viewportHeight / 2.f > mapSize.y) {
			cam.position.y = mapSize.y - cam.viewportHeight / 2f;
		} else {
			cam.position.y = playerPosition.y;
		}
		
		cam.update();
	}

	@Override
	public void dispose() {
		b2DRenderer.dispose();
		mapRenderer.dispose();
		ui.dispose();
	}
}
