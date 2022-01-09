package scripts;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;

import components.EntityBits;
import components.ScriptComponent;
import components.SoundComponent;

public class FruitItemScript extends Script {
	private final int maxFruits;
	private int fruitsAvailable;
	private boolean pickable = false;
	private float newFruitTimer = 0.f;
	private float timeToNextFruitGrow;
	private ComponentMapper<SoundComponent> soundCompMapper = ComponentMapper.getFor(SoundComponent.class);
	private ComponentMapper<ScriptComponent> scriptCompMapper = ComponentMapper.getFor(ScriptComponent.class);
	
	public FruitItemScript(final int maxFruits, final float timeToNextFruitGrow) {
		this.maxFruits = maxFruits;
		fruitsAvailable = maxFruits;
		this.timeToNextFruitGrow = timeToNextFruitGrow;
	}
	
	@Override
	public void update(Entity self, float deltaTime) {
		if (pickable && fruitsAvailable > 0 && Gdx.input.isKeyJustPressed(Keys.ENTER)) {
			scriptCompMapper.get(self).eventsToDispatch.add("ItemPicked");
			soundCompMapper.get(self).getSoundEffect("ItemPicked").shouldPlay = true;
			--fruitsAvailable;
		}
		
		newFruitTimer += deltaTime;
		if (newFruitTimer >= timeToNextFruitGrow) {
			fruitsAvailable = Math.min(fruitsAvailable + 1, maxFruits);
			newFruitTimer = 0.f;
		}
	}
	
	@Override
	public void onCollisionEnd(Contact contact, Fixture self, Fixture other) {
		if ((((Entity)other.getUserData()).flags & EntityBits.PLAYER_BIT) != 0) {
			pickable = false;
		}
	}
	
	@Override
	public void onCollisionBegin(Contact contact, Fixture self, Fixture other) {
		if ((((Entity)other.getUserData()).flags & EntityBits.PLAYER_BIT) != 0) {
			pickable = true;
		}
	}

	@Override
	public void onEventListened(Entity self, Entity sender, String eventName) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onEventReceived(Entity self, Entity receiver, String eventName) {
		scriptCompMapper.get(self).eventsToDispatch.remove("ItemPicked");
	}
}
