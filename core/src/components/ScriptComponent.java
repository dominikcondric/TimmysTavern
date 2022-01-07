package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

import scripts.Script;
import systems.SceneLoadingSystem.Scenes;

public class ScriptComponent implements Component, Disposable {
	public Script script;
	public Scenes newSceneToLoad = Scenes.NONE;
	public Array<String> eventsToDispatch;
	public Array<String> eventsToListen;
	
	public ScriptComponent(Script script) {
		this.script = script;
		eventsToDispatch = new Array<String>();
		eventsToListen = new Array<String>();
	}
	
	@Override
	public void dispose() {
		if (script instanceof Disposable) {
			((Disposable) script).dispose();
		}
	}
}