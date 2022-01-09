package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

public class SpriteComponent implements Component, Disposable {
	private Sprite sprite = null;
	public Color tintColor;
	public Vector2 position;
	public boolean draw = true;
	
	public SpriteComponent(TextureRegion texture, Vector2 spritePosition, Vector2 spriteSize) {
		tintColor = Color.WHITE;
		sprite = new Sprite(texture);
		sprite.setSize(spriteSize.x, spriteSize.y);
		sprite.setColor(tintColor);
		position = new Vector2(spritePosition);
	}
	
	public SpriteComponent(Sprite sprite) {
		tintColor = Color.WHITE;
		this.sprite = sprite;
		this.position = new Vector2(sprite.getX(), sprite.getY());
	}
	
	public Vector2 getSpriteSize() {
		return new Vector2(sprite.getWidth(), sprite.getHeight());
	}
	
	public void draw(SpriteBatch batch) {
		if (draw)
			batch.draw(sprite, position.x, position.y, sprite.getWidth(), sprite.getHeight());
	}
	
	public void setScale(float scale) {
		sprite.scale(scale);
	}
	
	public void setSprite(Sprite sprite, boolean disposePreviousSprite) {
		if (sprite != null && disposePreviousSprite)
			dispose();
		
		this.sprite = sprite;
	}
	
	@Override
	public void dispose() {
		sprite.getTexture().dispose();
	}
}
