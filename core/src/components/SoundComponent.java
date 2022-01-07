package components;

import java.util.Collection;
import java.util.HashMap;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

public class SoundComponent implements Component, Disposable {
	private HashMap<String, SoundEffect> soundEffects;
	
	public class SoundEffect {
		SoundEffect(Sound sound, boolean play, boolean loop) {
			this.sound = sound;
			this.shouldPlay = play;
			looped = loop;
		}
		
		public Sound sound;
		public boolean shouldPlay;
		public boolean looped;
	}
	
	public SoundComponent() {
		soundEffects = new HashMap<String, SoundEffect>();
	}
	
	public Collection<SoundEffect> getSoundEffects() {
		return soundEffects.values();
	}
	
	public SoundEffect getSoundEffect(String name) {
		if (!soundEffects.containsKey(name)) {
			System.out.println("Sound name doesn't exist!");
			return null;
		}
		
		return soundEffects.get(name);
	}
	
	public void addSound(String name, FileHandle soundFile, boolean shouldPlay, boolean looped) {
		if (soundEffects.containsKey(name)) {
			System.out.println("Animation name already exists");
			return;
		}
		
		Sound newSound = Gdx.audio.newSound(soundFile);
		soundEffects.put(name, new SoundEffect(newSound, shouldPlay, looped));
	}
	
	public void removeSound(String name, FileHandle soundFile) {
		if (soundEffects.containsKey(name)) {
			System.out.println("Animation name already exists");
			return;
		}
		
		soundEffects.get(name).sound.dispose();
		soundEffects.remove(name);
	}
	
	@Override
	public void dispose() {
		for (SoundEffect ss : soundEffects.values()) {
			ss.sound.dispose();
		}
	}
}
