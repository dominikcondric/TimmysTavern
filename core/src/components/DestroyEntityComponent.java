package components;

import com.badlogic.ashley.core.Component;

public class DestroyEntityComponent implements Component {
	public boolean destroy = false;
	public boolean disposeResources = false;
	public boolean destroyOnSceneChange = true;
}
