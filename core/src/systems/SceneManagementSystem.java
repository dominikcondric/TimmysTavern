package systems;

import java.util.ArrayList;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.utils.Disposable;

import components.AddEntityComponent;
import components.DestroyEntityComponent;

public class SceneManagementSystem extends EntitySystem {
	private ComponentMapper<AddEntityComponent> addEntityCompMapper = ComponentMapper.getFor(AddEntityComponent.class);
	private Family addEntityCompFamily = Family.all(AddEntityComponent.class).get();
	private ComponentMapper<DestroyEntityComponent> destroyEntityCompMapper  = ComponentMapper.getFor(DestroyEntityComponent.class);
	private Family destroyEntityCompFamily = Family.all(DestroyEntityComponent.class).get();
	
	public SceneManagementSystem(int priority) {
		super(priority);
	}
	
	@Override
	public void update(float deltaTime) {
		ArrayList<Entity> entitiesToAdd = new ArrayList<>();
		boolean sceneChange = false;
		
		for (Entity e : getEngine().getEntitiesFor(addEntityCompFamily)) {
			AddEntityComponent addEntityComp = addEntityCompMapper.get(e);
			if (addEntityComp.load) {
				sceneChange = sceneChange || addEntityComp.sceneChange; 
				entitiesToAdd.addAll(addEntityComp.entitiesToAdd);
				addEntityComp.load = false;
			}
		}
		
		for (Entity e : getEngine().getEntitiesFor(destroyEntityCompFamily)) {
			DestroyEntityComponent destroyEntityComp = destroyEntityCompMapper.get(e);
			if (destroyEntityComp.destroy || (destroyEntityComp.destroyOnSceneChange && sceneChange)) {
				getEngine().removeEntity(e);
				destroyEntityComp.destroy = false;
				if (destroyEntityComp.disposeResources) {
					for (Component c : e.getComponents()) {
						if (c instanceof Disposable) {
							((Disposable) c).dispose();
						}
					}
				}
			}
		}
		
		for (Entity e : entitiesToAdd) {
			getEngine().addEntity(e);
		}
	}

}
