package components;

import java.util.HashSet;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Disposable;

import scripts.Script;

public class ScriptComponent implements Component, Disposable {
	public Script script;
	public HashSet<String> eventsToListen;
	public HashSet<String> eventsToDispatch;
	
	public ScriptComponent(Script script) {
		this.script = script;
		eventsToDispatch = new HashSet<String>();
		eventsToListen = new HashSet<String>();
	}
	
	@Override
	public void dispose() {
		if (script instanceof Disposable) {
			((Disposable) script).dispose();
		}
	}
}