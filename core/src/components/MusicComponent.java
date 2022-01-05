package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Disposable;

public class MusicComponent implements Component, Disposable {
	public Music music;
	public boolean shouldPlay = true;
	public boolean restartOnStop = true;
	
	public MusicComponent(FileHandle musicPath) {
		music = Gdx.audio.newMusic(musicPath);
	}
	
	@Override
	public void dispose() {
		music.dispose();
	}
}
