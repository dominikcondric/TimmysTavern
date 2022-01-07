package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

public class MusicComponent implements Component, Disposable {
	public Music music = null;
	public boolean shouldPlay = true;
	public boolean restartOnStop = true;
	
	public MusicComponent(FileHandle musicFile) {
		loadMusic(musicFile);
	}
	
	public void loadMusic(FileHandle musicFile) {
		if (music != null)
			dispose();
		
		music = Gdx.audio.newMusic(musicFile);
	}
	
	@Override
	public void dispose() {
		music.dispose();
	}
}
