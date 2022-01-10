package components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;

public class GuiComponent implements Component, Disposable {
	public Group actors;
	
	public GuiComponent() {
		actors = new Group();
	}
	
	@Override
	public void dispose() {
		groupLoop(actors);
		actors.remove();
	}
	
	private void groupLoop(Group group) {
		for (Actor a : group.getChildren()) {
			if (a instanceof Image && ((Image) a).getDrawable() != null) {
				((TextureRegionDrawable)((Image) a).getDrawable()).getRegion().getTexture().dispose();
			} else if (a instanceof Group) {
				groupLoop((Group)a);
			}
			a.remove();
		}
	}
}
