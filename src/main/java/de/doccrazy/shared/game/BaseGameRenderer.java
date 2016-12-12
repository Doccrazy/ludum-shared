package de.doccrazy.shared.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import de.doccrazy.shared.game.actor.WorldActor;
import de.doccrazy.shared.game.base.ActorListener;
import de.doccrazy.shared.game.world.Box2dWorld;

public abstract class BaseGameRenderer<T extends Box2dWorld<T>> implements ActorListener<T> {
	private static final float CAM_PPS = 5f;

    private final SpriteBatch batch = new SpriteBatch();
    private final FrameBuffer frameBuffer;
    private final Box2DDebugRenderer renderer;

    protected final T world;
    protected final Vector2 gameViewport;
    protected final OrthographicCamera camera;
    protected float zoom = 1;   //camera zoom
    protected boolean renderBox2dDebug;

    public BaseGameRenderer(T world, Vector2 gameViewport) {
        this.world = world;
        this.gameViewport = gameViewport;

        // set the game stage viewport to the meters size
        world.stage.setViewport(new ExtendViewport(gameViewport.x, gameViewport.y));
        renderer = new Box2DDebugRenderer();
        renderer.setDrawContacts(true);

        // we obtain a reference to the game stage camera. The camera is scaled to box2d meter units
        camera = (OrthographicCamera) world.stage.getCamera();

        init();

        frameBuffer = new FrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
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

        frameBuffer.begin();
        batch.setProjectionMatrix(world.stage.getCamera().combined);
        batch.begin();
        drawBackground(batch);
        batch.end();

        // game stage rendering
        world.stage.draw();

        // box2d debug renderering (optional)
        if (renderBox2dDebug) {
            renderer.render(world.box2dWorld, camera.combined);
        }
        frameBuffer.end();

        batch.setProjectionMatrix(new Matrix4().idt());
        batch.begin();
        renderFramebufferToScreen(batch, frameBuffer);
        batch.end();

        world.rayHandler.setCombinedMatrix(camera);
        world.rayHandler.updateAndRender();
    }

    protected void renderFramebufferToScreen(SpriteBatch batch, FrameBuffer frameBuffer) {
        batch.draw(frameBuffer.getColorBufferTexture(), -1,1, 2, -2);
    }

    /**
     * Called every frame before rendering; this is a good place to position your camera
     */
	protected abstract void beforeRender();

	@Override
	public void actorAdded(WorldActor<T> actor) {
	}

	@Override
	public void actorRemoved(WorldActor<T> actor) {
	}

	public boolean isRenderBox2dDebug() {
        return renderBox2dDebug;
    }

	public void setRenderBox2dDebug(boolean renderBox2dDebug) {
        this.renderBox2dDebug = renderBox2dDebug;
    }
}