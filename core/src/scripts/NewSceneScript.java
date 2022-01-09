package scripts;

import java.util.ArrayList;

import com.badlogic.ashley.core.Entity;

import utility.B2DBodyCreator;

public interface NewSceneScript {
	void loadNewScene(ArrayList<Entity> entitiesToLoad, B2DBodyCreator bodyCreator);
}
