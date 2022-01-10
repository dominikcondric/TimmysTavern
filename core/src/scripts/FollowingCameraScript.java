package scripts;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;

import components.CameraComponent;
import components.NewSceneComponent;
import components.PhysicsComponent;

public class FollowingCameraScript extends Script {
	private ComponentMapper<CameraComponent> camCompMapper = ComponentMapper.getFor(CameraComponent.class);
	private ComponentMapper<PhysicsComponent> physicsCompMapper = ComponentMapper.getFor(PhysicsComponent.class);
	
	public FollowingCameraScript(Entity selfEntity) {
		super(selfEntity);
	}
	
	@Override
	public void update(float deltaTime) {
	}

	@Override
	public void onEventReceived(Entity sender, String eventName) {
		CameraComponent camComp = camCompMapper.get(self);
		OrthographicCamera camera = camComp.camera;
		Vector2 xLimits = camComp.xLimits;
		Vector2 yLimits = camComp.yLimits;
		if (eventName.contentEquals("FollowMe")) {
			Vector2 playerPosition = physicsCompMapper.get(sender).body.getPosition();
			
			float windowAspectRatio = (float)Gdx.graphics.getHeight() / Gdx.graphics.getWidth();
			camera.viewportWidth = Math.max(20.f, xLimits.y / 3f);
			camera.viewportHeight = Math.max(20.f * windowAspectRatio, yLimits.y / 3f * windowAspectRatio);
			
			if (playerPosition.x - camera.viewportWidth / 2f < xLimits.x) {
				camera.position.x = camera.viewportWidth / 2f;
			} else if (playerPosition.x + camera.viewportWidth / 2.f > xLimits.y) {
				camera.position.x = xLimits.y - camera.viewportWidth / 2f;
			} else {
				camera.position.x = playerPosition.x;
			}
			
			if (playerPosition.y - camera.viewportHeight / 2f < yLimits.x) {
				camera.position.y = camera.viewportHeight / 2f;
			} else if (playerPosition.y + camera.viewportHeight / 2.f > yLimits.y) {
				camera.position.y = yLimits.y - camera.viewportHeight / 2f;
			} else {
				camera.position.y = playerPosition.y;
			}
			
			camera.update();
		} else if (eventName.contentEquals("SceneChanged")) {
			switch (sender.getComponent(NewSceneComponent.class).sceneName) {
				case "tavern":
					xLimits.y = 20.f;
					yLimits.y = 15.f;
					break;
				case "village":
					xLimits.y = 100.f;
					yLimits.y = 100.f;
					break;
			}
		}
	}
}
