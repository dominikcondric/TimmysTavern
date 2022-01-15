package systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;

import components.AnimationComponent;
import components.CameraComponent;
import components.MapComponent;
import components.SpriteComponent;

public class RenderingSystem extends EntitySystem implements Disposable {
	private Family spriteComponentsFamily = Family.all(SpriteComponent.class).get();
	private Family mapComponentFamily = Family.all(MapComponent.class).get();
	private Family animationComponentFamily = Family.all(AnimationComponent.class, SpriteComponent.class).get();
	private Family camComponentFamily = Family.all(CameraComponent.class).get();
	private ComponentMapper<SpriteComponent> spriteCompMapper = ComponentMapper.getFor(SpriteComponent.class);
	private ComponentMapper<AnimationComponent> animationCompMapper = ComponentMapper.getFor(AnimationComponent.class);
	private ComponentMapper<MapComponent> mapCompMapper = ComponentMapper.getFor(MapComponent.class);
	private ComponentMapper<CameraComponent> camCompMapper = ComponentMapper.getFor(CameraComponent.class);
	private SpriteBatch batch;
	private OrthogonalTiledMapRenderer mapRenderer;
	public Stage ui;
	
	public RenderingSystem(SpriteBatch batch, int priority) {
		super(priority);
		this.batch = batch;
		
		ui = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), batch);
		Gdx.input.setInputProcessor(ui);
		mapRenderer = new OrthogonalTiledMapRenderer(null, 1f / 32f, batch);
	}
	
	@Override
	public void update(float deltaTime) {
		Engine engine = getEngine();
		
		OrthographicCamera cam = null; 
		for (final Entity e : engine.getEntitiesFor(camComponentFamily)) {
			CameraComponent camComp = camCompMapper.get(e);
			if (camComp.active) {
				cam = camComp.camera;
				break;
			}
		}
		
		for (Entity e : engine.getEntitiesFor(animationComponentFamily)) {
			AnimationComponent animationComp = animationCompMapper.get(e);
			spriteCompMapper.get(e).setSprite(animationComp.getActiveSprite(deltaTime), false);
		}
		
		batch.begin();
		for (Entity e : engine.getEntitiesFor(mapComponentFamily)) {
			MapComponent mapComp = mapCompMapper.get(e);
			mapRenderer.setMap(mapComp.map);
			mapRenderer.setView(cam);
			for (int i = 0; i < mapComp.getLastBackgroundLayerToRenderIndex(); ++i) {
				mapRenderer.renderTileLayer(mapComp.getTileLayers().get(i));
			}
		}
		
		for (Entity e : engine.getEntitiesFor(spriteComponentsFamily)) {
			SpriteComponent spriteComp = spriteCompMapper.get(e);
			spriteComp.draw(batch);
		}
		
		for (Entity e : engine.getEntitiesFor(mapComponentFamily)) {
			MapComponent mapComp = mapCompMapper.get(e);
			mapRenderer.setMap(mapComp.map);
			mapRenderer.setView(cam);
			for (int i = mapComp.getLastBackgroundLayerToRenderIndex(); i < mapComp.getTileLayers().size; ++i) {
				mapRenderer.renderTileLayer(mapComp.getTileLayers().get(i));
			}
		}
		
		batch.setProjectionMatrix(cam.combined);
		batch.end();
		
		ui.act(deltaTime);
		ui.draw();
	}
	
	public void addGuiElement(Actor actor) {
		ui.addActor(actor);
	}
	
	@Override
	public void dispose() {
		mapRenderer.dispose();
		ui.dispose();
	}
}
