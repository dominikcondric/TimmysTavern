package systems;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;

import components.NewSceneComponent;
import components.ScriptComponent;
import utility.B2DBodyCreator;

public class ScriptingSystem extends EntitySystem {
	private Family scriptingCompFamily = Family.all(ScriptComponent.class).get();
	private Family newSceneCompFamily = Family.all(ScriptComponent.class, NewSceneComponent.class).get();
	private ComponentMapper<ScriptComponent> scriptCompMapper = ComponentMapper.getFor(ScriptComponent.class);
	private ComponentMapper<NewSceneComponent> newSceneCompMapper = ComponentMapper.getFor(NewSceneComponent.class);
	private HashMap<String, Event> eventsTable;
	private ArrayList<Event> completedEvents;
	private B2DBodyCreator bodyCreator;
	
	private class Event {
		Event(String event) {
			eventName = event;
		}
		
		String eventName;
		ArrayList<Entity> listeners = new ArrayList<Entity>();
		Entity dispatcher;
	}
	
	public ScriptingSystem(B2DBodyCreator bodyCreator, int priority) {
		super(priority);
		this.bodyCreator = bodyCreator;
		eventsTable = new HashMap<String, Event>();
		completedEvents = new ArrayList<>();
	}

	@Override
	public void update(float deltaTime) {
		ImmutableArray<Entity> scriptingEnts = getEngine().getEntitiesFor(scriptingCompFamily);
		for (final Entity e : scriptingEnts) {
			ScriptComponent scriptComp = scriptCompMapper.get(e);
			
			scriptComp.script.update(e, deltaTime);
			for (String eventName : scriptComp.eventsToDispatch) {
				Event event = eventsTable.get(eventName);
				if (event != null) {
					completedEvents.add(eventsTable.get(eventName));
				} else {
					event = eventsTable.put(eventName, new Event(eventName));
					event = eventsTable.get(eventName);
				}
				
				event.dispatcher = e;
				
			}
			
			for (String eventName : scriptComp.eventsToListen) {
				Event event = eventsTable.get(eventName);
				if (event != null && event.dispatcher != null && event.listeners.isEmpty()) {
					completedEvents.add(eventsTable.get(eventName));
				} else if (event == null) {
					eventsTable.put(eventName, new Event(eventName));
					event = eventsTable.get(eventName);
				}
				
				event.listeners.add(e);
			}
		}		
		
		for (Event event : completedEvents) {
			for (Entity listener : event.listeners) {
				scriptCompMapper.get(listener).script.onEventListened(listener, event.dispatcher, event.eventName);
				scriptCompMapper.get(event.dispatcher).script.onEventReceived(event.dispatcher, listener, event.eventName);
			}
		}
		
		eventsTable.clear();
		completedEvents.clear();
		
		for (Entity newSceneEntity : getEngine().getEntitiesFor(newSceneCompFamily)) {
			NewSceneComponent newSceneComp = newSceneCompMapper.get(newSceneEntity);
			if (newSceneComp.load) {
				newSceneComp.load = false;
				for (Entity e : getEngine().getEntities()) {
					if ((e.flags & newSceneComp.flagsToSkip) == 0) {
						getEngine().removeEntity(e);
					}
				}
				
				ArrayList<Entity> newEntitiesToLoad = new ArrayList<Entity>();
				newSceneComp.newSceneScript.loadNewScene(newEntitiesToLoad, bodyCreator);
				for (Entity e : newEntitiesToLoad) {
					getEngine().addEntity(e);
				}
				
				newEntitiesToLoad.clear();
				break;
			}
		}
	}
}
