package de.doccrazy.shared.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;

import de.doccrazy.shared.game.world.Box2dWorld;

/**
 * Base class for actors contained in the game world
 */
public abstract class WorldActor extends Actor {
    protected Box2dWorld world;
    protected boolean dead;
    protected float stateTime = 0f;
    protected Tasker task = new Tasker();

    private final Affine2 worldTransform = new Affine2();
    private final Matrix4 computedTransform = new Matrix4();
    private final Matrix4 oldTransform = new Matrix4();

    public WorldActor(Box2dWorld world) {
        this.world = world;
    }

    @Override
    protected void setStage(Stage stage) {
    	super.setStage(stage);
    	if (stage != null) {
    		init();
    	}
    }

    protected void init() {
    }

    @Override
    public final void act(float delta) {
        if (dead) {
            die();
            return;
        }
        super.act(delta);
        stateTime += delta;

        task.update(delta);
        doAct(delta);
    }

    protected abstract void doAct(float delta);

    private void die() {
        remove();
    }

    public final void kill() {
        dead = true;
    }

    public final boolean isDead() {
        return dead;
    }

    @Override
    protected void setParent(Group parent) {
        super.setParent(parent);
        if (parent != null) {
            onActorAdded();
        }
    }

    /**
     * Called when actor has been added to stage
     */
    protected void onActorAdded() {
    }

    @Override
    public final boolean remove() {
        if (super.remove()) {
            doRemove();
            world.onActorRemoved(this);
            return true;
        }
        return false;
    }

    protected void doRemove() {
    }

    /**
     * Apply a transformation matrix so you can draw in this actor's coordinate system
     * @param toOrigin true: (0, 0) is at the origin point; false: (0, 0) is at the lower left
     */
    protected void applyClientTransform(Batch batch, boolean toOrigin) {
        worldTransform.setToTrnRotScl(getX() + getOriginY(), getY() + getOriginY(), getRotation(), getScaleX(), getScaleY());
        if (!toOrigin) {
            worldTransform.translate(-getOriginX() / getScaleX(), -getOriginY() / getScaleY());
        }
        computedTransform.set(worldTransform);
        oldTransform.set(batch.getTransformMatrix());
        batch.setTransformMatrix(computedTransform);
    }

    /**
     * Reset transformation matrix after drawing
     */
    protected void resetTransform(Batch batch) {
        batch.setTransformMatrix(oldTransform);
    }
}
