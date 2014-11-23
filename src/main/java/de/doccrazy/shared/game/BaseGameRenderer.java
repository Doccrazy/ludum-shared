package de.doccrazy.shared.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import de.doccrazy.shared.core.Debug;
import de.doccrazy.shared.game.actor.WorldActor;
import de.doccrazy.shared.game.base.ActorListener;
import de.doccrazy.shared.game.world.Box2dWorld;

public abstract class BaseGameRenderer implements ActorListener {
	private static final float CAM_PPS = 5f;

    private SpriteBatch batch = new SpriteBatch();
    private Box2DDebugRenderer renderer;

    protected Box2dWorld world;
    protected Vector2 gameViewport;
    protected OrthographicCamera camera;
    protected float zoom = 1;   //camera zoom

    public BaseGameRenderer(Box2dWorld world, Vector2 gameViewport) {
        this.world = world;
        this.gameViewport = gameViewport;

        // set the game stage viewport to the meters size
        world.stage.setViewport(new ExtendViewport(gameViewport.x, gameViewport.y));
        renderer = new Box2DDebugRenderer();

        // we obtain a reference to the game stage camera. The camera is scaled to box2d meter units
        camera = (OrthographicCamera) world.stage.getCamera();

        init();
    }

    /**
     * Set up static world attributes (e.g. lights) independent of level
     */
    protected abstract void init();

    /**
     * Draw a background image; gets called before world rendering, batch will be in world coordinates
     */
    protected abstract void drawBackground(SpriteBatch batch);

    //called from BaseGameScreen
    final void render() {
        ExtendViewport vp = (ExtendViewport)world.stage.getViewport();
        vp.setMinWorldWidth(gameViewport.x*zoom);
        vp.setMinWorldHeight(gameViewport.y*zoom);
        world.stage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        beforeRender();

        camera.update();

        batch.setProjectionMatrix(world.stage.getCamera().combined);
        batch.begin();
        drawBackground(batch);
        batch.end();

        // game stage rendering
        world.stage.draw();

        // box2d debug renderering (optional)
        if (Debug.ON) {
            renderer.render(world.box2dWorld, camera.combined);
        }

        world.rayHandler.setCombinedMatrix(camera.combined);
        world.rayHandler.updateAndRender();
    }

    /**
     * Called every frame before rendering; this is a good place to position your camera
     */
	protected abstract void beforeRender();

	@Override
	public void actorAdded(WorldActor actor) {
	}

	@Override
	public void actorRemoved(WorldActor actor) {
	}

}