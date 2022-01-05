package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.scenes.scene2d.Group;

public class GuiComponent implements Component {
	public Group actors;
	
	public GuiComponent() {
		actors = new Group();
	}
}
