package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

public class CameraComponent implements Component {
	public OrthographicCamera camera;
	public Vector2 xLimits;
	public Vector2 yLimits;
	public boolean active = true;
	
	public CameraComponent(float width, float height) {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, width, height);
	}
}
