package components;

import com.badlogic.ashley.core.Component;

import scripts.NewSceneScript;

public class NewSceneComponent implements Component {
	public int flagsToSkip = EntityBits.CAMERA_BIT | EntityBits.PLAYER_BIT;
	public boolean load = false;
	public final String sceneName;
	public NewSceneScript newSceneScript;
	
	public NewSceneComponent(NewSceneScript newSceneScript, String sceneName) {
		this.newSceneScript = newSceneScript;
		this.sceneName = sceneName;
	}
}
