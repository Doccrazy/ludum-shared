package de.doccrazy.shared.game.base;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

public abstract class RegularAction extends Action {
    private float delay;

    private float deltaCache = 0;
    private boolean enabled = true;

    private boolean inited;

    public RegularAction(float delay) {
        this.delay = delay;
    }

    @Override
    public final boolean act(float delta) {
        if (!enabled) {
            return false;
        }
        if (!inited) {
            inited = true;
            if (init()) {
                return true;
            }
        }
        deltaCache += delta;

        while (deltaCache >= delay) {
            deltaCache -= delay;
            if (run(delay)) {
                done();
                return true;
            }
        }
        return false;
    }

    protected boolean init() {
        return false;
    }

    protected void done() {
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void setActor(Actor actor) {
        if (actor == null && getActor() != null) {
            done();
        }
        super.setActor(actor);
    }

    public float getDelay() {
        return delay;
    }

    public void setDelay(float delay) {
        this.delay = delay;
    }

    @Override
    public void restart() {
        inited = false;
    }

    abstract protected boolean run(float delta);

}
