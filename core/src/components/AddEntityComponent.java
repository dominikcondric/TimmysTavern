package components;

import java.util.ArrayList;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;

public class AddEntityComponent implements Component {
	public boolean load = false;
	public ArrayList<Entity> entitiesToAdd;
	public boolean sceneChange = true;
	
	public AddEntityComponent(int arrayCapacity) {
		entitiesToAdd = new ArrayList<Entity>(arrayCapacity);
	}
	
	public AddEntityComponent() {
		entitiesToAdd = new ArrayList<Entity>(0);
	}
}
