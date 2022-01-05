package components;

import java.util.HashMap;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.Animation.PlayMode;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

public class AnimationComponent implements Component, Disposable {
	private HashMap<String, Animation<Sprite>> animationMap;
	private Animation<Sprite> activeAnimation;
	private float stateTimer = 0.f;
	
	public AnimationComponent() {
		animationMap = new HashMap<String, Animation<Sprite>>();
	}
	
	public Sprite getActiveSprite(float deltaTime) {
		stateTimer += deltaTime;
		return activeAnimation.getKeyFrame(stateTimer);
	}
	
	public void addAnimation(String name, Array<Sprite> sprites, float keyFrameTime, PlayMode playMode, boolean active) {
		if (animationMap.containsKey(name)) {
			System.out.println("Animation name already exists");
			return;
		}
		
		Animation<Sprite> newAnimation = new Animation<Sprite>(keyFrameTime, sprites, playMode);
		animationMap.put(name, newAnimation);
		if (active) {
			activeAnimation = newAnimation;
		}
	}
	
	public void setActiveAnimation(String name, boolean resetTimer) {
		if (!animationMap.containsKey(name)) {
			System.out.println("Animation name doesn't exist!");
			return;
		}
		
		activeAnimation = animationMap.get(name);
		if (resetTimer)
			stateTimer = 0.f;
	}
	
	@Override
	public void dispose() {
		for (Animation<Sprite> a : animationMap.values()) {
			for (Object s : a.getKeyFrames()) {
				((Sprite)s).getTexture().dispose();
			}
		}
			
	}
}
