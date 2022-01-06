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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;
import com.gdx.game.TimmysTavern;

import components.AnimationComponent;
import components.GuiComponent;
import components.MapComponent;
import components.MusicComponent;
import components.PhysicsComponent;
import components.SpriteComponent;
import systems.AudioSystem;
import systems.PhysicsSystem;
import systems.RenderingSystem;
import systems.SceneLoadingSystem;
import systems.ScriptingSystem;

public class GameScreen implements Screen {
	private TimmysTavern game;
	private Engine ecs;

	public GameScreen(TimmysTavern game) {
		this.game = game;
		ecs = new Engine();
		ecs.addSystem(new RenderingSystem(game.batch, 3));
		ecs.addSystem(new ScriptingSystem(2));
		ecs.addSystem(new PhysicsSystem(new Vector2(0.f, 0.f), 1));
		ecs.addSystem(new AudioSystem(4));
		ecs.addSystem(new SceneLoadingSystem(0));
		addListeners();
	}
	
	private void addListeners() {
		addDisposeEntityListener(MapComponent.class);
		addDisposeEntityListener(SpriteComponent.class);
		addDisposeEntityListener(AnimationComponent.class);
		addDisposeEntityListener(PhysicsComponent.class);
		addDisposeEntityListener(GuiComponent.class);
		addDisposeEntityListener(MusicComponent.class);
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
