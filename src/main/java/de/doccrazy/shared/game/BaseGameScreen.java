package de.doccrazy.shared.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import de.doccrazy.shared.core.Debug;
import de.doccrazy.shared.game.world.Box2dWorld;

public abstract class BaseGameScreen<W extends Box2dWorld<W>, R extends BaseGameRenderer> implements Screen {
    private W world; // contains the game world's bodies and actors.
    private R renderer; // our custom game renderer.
    private Stage uiStage; // stage that holds the GUI. Pixel-exact size.
    private SpriteBatch batch;

	@Override
	public void show() {
		batch = new SpriteBatch();
		uiStage = new Stage(new ExtendViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		uiStage.setDebugAll(Debug.ON);

        world = createWorld();
        renderer = createRenderer(world);

		Gdx.input.setInputProcessor(new InputMultiplexer(uiStage, world.stage));

		createUI(uiStage, world, renderer);
	}

	protected abstract W createWorld();

	protected abstract R createRenderer(W world);

	protected abstract void createUI(Stage uiStage, W world, R renderer);

	@Override
	public void render(float delta) {
		update(delta);

		Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_TEXTURE_2D);

        renderer.render(); // draw the box2d world
        uiStage.draw(); // draw the GUI
	}

	private void update(float delta) {
        world.update(delta); // update the box2d world
        uiStage.act(delta); // update GUI
	}

	@Override
	public void resize(int width, int height) {
		uiStage.getViewport().update(width, height, true);
	}

	@Override
	public void hide() {
		Gdx.input.setInputProcessor(null);
		uiStage.dispose();
		batch.dispose();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
	}

}
