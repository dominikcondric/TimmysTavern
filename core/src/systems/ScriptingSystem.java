package systems;

import java.util.HashMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;

import components.ScriptComponent;
import systems.SceneLoadingSystem.Scenes;

public class ScriptingSystem extends EntitySystem {
	private Family scriptingCompFamily = Family.all(ScriptComponent.class).get();
	private ComponentMapper<ScriptComponent> scriptCompMapper = ComponentMapper.getFor(ScriptComponent.class);
	private HashMap<String, Entity> eventsTable;
	private boolean shouldClearEvents = false;
	
	public ScriptingSystem(int priority) {
		super(priority);
		eventsTable = new HashMap<String, Entity>();
	}

	@Override
	public void update(float deltaTime) {
		ImmutableArray<Entity> scriptingEnts = getEngine().getEntitiesFor(scriptingCompFamily);
		for (final Entity e : scriptingEnts) {
			ScriptComponent scriptComp = scriptCompMapper.get(e);
			if (scriptComp.newSceneToLoad != Scenes.NONE)
				shouldClearEvents = true;
			
			scriptComp.script.update(e, deltaTime);
			for (String event : scriptComp.eventsToDispatch) {
				if (eventsTable.containsKey(event)) {
					Entity listener = eventsTable.get(event);
					ScriptComponent listenerScriptComponent = scriptCompMapper.get(listener);
					listenerScriptComponent.script.onEventListen(listener, e, event);
					eventsTable.remove(event);
					
				} else {
					eventsTable.put(event, e);
				}
			}
			
			for (String event : scriptComp.eventsToListen) {
				if (eventsTable.containsKey(event)) {
					Entity listener = eventsTable.get(event);
					scriptComp.script.onEventListen(e, listener, event);
					eventsTable.remove(event);
				} else {
					eventsTable.put(event, e);
				}
			}
			
			scriptComp.eventsToDispatch.clear();
			scriptComp.eventsToListen.clear();
			
			if (shouldClearEvents) {
				eventsTable.clear();
				shouldClearEvents = false;
			}
		}
	}
}
