package de.doccrazy.shared.game.world;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import box2dLight.RayHandler;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import de.doccrazy.shared.game.actor.WorldActor;
import de.doccrazy.shared.game.base.ActorContactListener;
import de.doccrazy.shared.game.base.ActorListener;
import de.doccrazy.shared.game.event.EventSource;

public abstract class Box2dWorld extends EventSource {
    private static final float PHYSICS_STEP = 1f/300f;

    public final World box2dWorld; // box2d world
    public final RayHandler rayHandler;
    public final Stage stage; // stage containing game actors (not GUI, but actual game elements)

    private float deltaCache;
    private ActorListener actorListener;

    private int score;

    private GameState gameState = null;
    private float stateTime;

    public Box2dWorld(Vector2 gravity) {
        box2dWorld = new World(gravity, true);
        box2dWorld.setContactListener(new ActorContactListener());
        stage = new Stage(); // create the game stage
        rayHandler = new RayHandler(box2dWorld);

        transition(GameState.INIT);
    }

    public final void transition(GameState newState) {
    	if (gameState == newState) {
    		return;
    	}
        if (newState == GameState.INIT) {
            List<Actor> actors = Arrays.asList(stage.getActors().toArray());
            for (Actor actor : actors) {
            	if (actor instanceof WorldActor && ((WorldActor)actor).isNoRemove()) {
            		continue;
            	}
                actor.remove();
            }
        }
        if (newState != GameState.VICTORY && newState != GameState.DEFEAT) {
            score = 0;
        }
        stage.setKeyboardFocus(null);
        doTransition(newState);
        gameState = newState;
        stateTime = 0;
    }

    protected abstract void doTransition(GameState newState);

    public final void reset() {
        transition(GameState.INIT);
        transition(GameState.PRE_GAME);
    }

    public final void update(float delta) {
        deltaCache += delta;

        while (deltaCache >= PHYSICS_STEP) {
            stage.act(PHYSICS_STEP); // update game stage
            box2dWorld.step(PHYSICS_STEP, 6, 3); // update box2d world
            deltaCache -= PHYSICS_STEP;
        }

        stateTime += delta;

        doUpdate(delta);
    }

    protected abstract void doUpdate(float delta);

    public void setActorListener(ActorListener actorListener) {
        this.actorListener = actorListener;
    }

    public void addActor(WorldActor actor) {
        stage.addActor(actor);
        refreshZOrder();
        if (actorListener != null) {
            actorListener.actorAdded(actor);
        }
    }

    public void refreshZOrder() {
        stage.getActors().sort(new Comparator<Actor>() {
            @Override
            public int compare(Actor o1, Actor o2) {
                if (!(o1 instanceof WorldActor) || !(o2 instanceof WorldActor)) {
                    return 0;
                }
                return ((WorldActor)o1).getzOrder() - ((WorldActor)o2).getzOrder();
            }
        });
    }

    public void onActorRemoved(WorldActor actor) {
        if (actorListener != null) {
            actorListener.actorRemoved(actor);
        }
    }

    protected void addScore(int value) {
        score += value;
    }

    public int getScore() {
        return score;
    }

    public float getStateTime() {
        return stateTime;
    }

    public GameState getGameState() {
        return gameState;
    }

    public boolean isGameInProgress() {
        return gameState == GameState.GAME;
    }

    public boolean isGameFinished() {
        return gameState == GameState.VICTORY || gameState == GameState.DEFEAT;
    }
}
