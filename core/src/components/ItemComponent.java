package components;

import com.badlogic.ashley.core.Component;
import com.gdx.game.Item;

public class ItemComponent implements Component {
	public Item item;
	
	public ItemComponent(Item item) {
		this.item = item;
	}
}
