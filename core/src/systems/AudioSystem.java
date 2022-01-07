package systems;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;

import components.MusicComponent;
import components.SoundComponent;
import components.SoundComponent.SoundEffect;

public class AudioSystem extends EntitySystem {
	private ComponentMapper<MusicComponent> musicCompMapper = ComponentMapper.getFor(MusicComponent.class);
	private ComponentMapper<SoundComponent> soundCompMapper = ComponentMapper.getFor(SoundComponent.class);
	private Family musicCompFamily = Family.all(MusicComponent.class).get();
	private Family soundCompFamily = Family.all(SoundComponent.class).get();
	
	public AudioSystem(int priority) {
		super(priority);
	}
	
	@Override
	public void update(float deltaTime) {
		for (Entity e : getEngine().getEntitiesFor(musicCompFamily)) {
			MusicComponent musicComp = musicCompMapper.get(e);
			if (musicComp.shouldPlay && !musicComp.music.isPlaying()) {
				musicComp.music.play();
			} else if (!musicComp.shouldPlay && musicComp.music.isPlaying()) {
				if (musicComp.restartOnStop)
					musicComp.music.stop();
				else 
					musicComp.music.pause();
			}
		}
		
		for (Entity e : getEngine().getEntitiesFor(soundCompFamily)) {
			SoundComponent soundComp = soundCompMapper.get(e);
			for (SoundEffect se : soundComp.getSoundEffects()) {
				if (se.shouldPlay) {
					if (se.looped) {
						se.sound.loop();
					} else {
						se.sound.play();
						se.shouldPlay = false;
					}
				}
			}
		}
	}
}
