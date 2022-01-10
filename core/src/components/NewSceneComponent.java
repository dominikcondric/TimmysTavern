package components;

import java.util.ArrayList;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

public class NewSceneComponent implements Component {
	public int flagsToSkip = EntityBits.NO_DESTROY_BIT;
	public boolean load = false;
	public final String sceneName;
	public ArrayList<Entity> newEntities;
	
	public NewSceneComponent(String sceneName) {
		this.sceneName = sceneName;
	}
}
