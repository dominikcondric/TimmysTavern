package screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.gdx.game.TimmysTavern;

public class MainMenuScreen implements Screen {
	private TimmysTavern game = null;
	private Stage ui;
	private Table menuLayout;
	private TextButton startGameButton;
	private TextButton resumeGameButton;
	private TextButton exitGameButton;
	
	public MainMenuScreen(TimmysTavern game) {
		this.game = game;
		ui = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()), game.batch); 
		menuLayout = new Table();
		
		Gdx.input.setInputProcessor(ui);
		Pixmap buttonBackgroundPixmap = new Pixmap(1, 1, Format.RGB888);
		buttonBackgroundPixmap.setColor(Color.BLUE);
		buttonBackgroundPixmap.fill();
		TextureRegionDrawable buttonBackround = new TextureRegionDrawable(new Texture(buttonBackgroundPixmap)); 
		TextButtonStyle buttonStyle = new TextButtonStyle();
		buttonStyle.font = game.font;
		buttonStyle.fontColor = Color.WHITE;
		buttonStyle.up = buttonBackround;
		startGameButton = new TextButton("Start game", buttonStyle);
		startGameButton.getLabel().setFontScale(2.f);
		resumeGameButton = new TextButton("Resume game", buttonStyle);
		resumeGameButton.getLabel().setFontScale(2.f);
		exitGameButton = new TextButton("Exit game", buttonStyle);
		exitGameButton.getLabel().setFontScale(2.f);
		
		ui.addActor(menuLayout);
		menuLayout.setFillParent(true);
		menuLayout.align(Align.center);
		
		float buttonHeight = ui.getHeight() / 9.f;
 		
		menuLayout.row();
		menuLayout.add(startGameButton).spaceBottom(buttonHeight / 3.f).width(ui.getWidth() / 2).height(buttonHeight * 2f);
		menuLayout.row();
		menuLayout.add(resumeGameButton).spaceBottom(buttonHeight / 3.f).width(ui.getWidth() / 2).height(buttonHeight * 2f);
		menuLayout.row();
		menuLayout.add(exitGameButton).spaceBottom(buttonHeight / 3.f).width(ui.getWidth() / 2).height(buttonHeight * 2f);
		buttonBackgroundPixmap.dispose();		
	}

	@Override
	public void render(float delta) {
		ScreenUtils.clear(Color.BLACK);
		ui.act();
		ui.draw();
		
		if (startGameButton.getClickListener().isPressed()) {
			dispose();
			game.setScreen(new GameScreen(game));
		}
		
		if (exitGameButton.getClickListener().isPressed()) {
			dispose();
			Gdx.app.exit();
		}
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		ui.dispose();
	}

	@Override
	public void show() {
		// TODO Auto-generated method stub
		
	}

}
