package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;

import systems.SceneLoadingSystem.Scenes;

public class ScriptComponent implements Component {
	public Script script;
	public Scenes newSceneToLoad = Scenes.NONE;
	public Array<String> eventsToDispatch;
	public Array<String> eventsToListen;
	
	public ScriptComponent(Script script) {
		this.script = script;
		eventsToDispatch = new Array<String>();
		eventsToListen = new Array<String>();
	}
}