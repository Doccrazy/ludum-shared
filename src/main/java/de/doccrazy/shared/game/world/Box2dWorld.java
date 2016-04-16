package de.doccrazy.shared.game.world;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

import box2dLight.RayHandler;
import de.doccrazy.shared.game.actor.WorldActor;
import de.doccrazy.shared.game.base.ActorContactListener;
import de.doccrazy.shared.game.base.ActorListener;
import de.doccrazy.shared.game.event.EventSource;

public abstract class Box2dWorld<T extends Box2dWorld<T>> extends EventSource {
    private static final float PHYSICS_STEP = 1f/300f;

    public final World box2dWorld; // box2d world
    public final RayHandler rayHandler;
    public final Stage stage; // stage containing game actors (not GUI, but actual game elements)

    private float deltaCache;
    private ActorListener<T> actorListener;

    private int score;

    private GameState gameState = null;
    private float stateTime, lastStateTime;

    public Box2dWorld(Vector2 gravity) {
        box2dWorld = new World(gravity, true);
        box2dWorld.setContactListener(new ActorContactListener());
        stage = new Stage(); // create the game stage
        rayHandler = new RayHandler(box2dWorld);
    }

    public final void transition(GameState newState) {
    	if (gameState == newState) {
    		return;
    	}
        if (newState == GameState.INIT) {
            List<Actor> actors = Arrays.asList(stage.getActors().toArray());
            for (Actor actor : actors) {
            	if (actor instanceof WorldActor && ((WorldActor<?>)actor).isNoRemove()) {
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
        lastStateTime = stateTime;
        stateTime = 0;
    }

    protected abstract void doTransition(GameState newState);

    public final void reset() {
        transition(GameState.INIT);
        transition(GameState.PRE_GAME);
    }

    public final void update(float delta) {
        if (gameState == null) {
            transition(GameState.INIT);
        }

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

    public void setActorListener(ActorListener<T> actorListener) {
        this.actorListener = actorListener;
    }

    public void addActor(WorldActor<T> actor) {
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
                return ((WorldActor<?>)o1).getzOrder() - ((WorldActor<?>)o2).getzOrder();
            }
        });
    }

    public void onActorRemoved(WorldActor<T> actor) {
        if (actorListener != null) {
            actorListener.actorRemoved(actor);
        }
    }

    public void addScore(int value) {
        score += value;
    }

    public int getScore() {
        return score;
    }

    public float getStateTime() {
        return stateTime;
    }

    public float getLastStateTime() {
        return lastStateTime;
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

    public Body bodyAt(Vector2 pos, float radius) {
        return bodyAt(pos, radius, null);
    }

    public Body bodyAt(Vector2 pos, float radius, Body exclude) {
        ClosestBodyQueryCallback cb = new ClosestBodyQueryCallback(pos, exclude, false);
        box2dWorld.QueryAABB(cb, pos.x - radius*0.5f, pos.y - radius*0.5f, pos.x + radius*0.5f, pos.y + radius*0.5f);
        return cb.body;
    }

    public Body staticBodyAt(Vector2 pos, float radius) {
        ClosestBodyQueryCallback cb = new ClosestBodyQueryCallback(pos, null, true);
        box2dWorld.QueryAABB(cb, pos.x - radius*0.5f, pos.y - radius*0.5f, pos.x + radius*0.5f, pos.y + radius*0.5f);
        return cb.body;
    }

    public Set<Body> allBodiesAt(Vector2 pos, float radius) {
        final Set<Body> result = new HashSet<>();
        box2dWorld.QueryAABB(new QueryCallback() {
            @Override
            public boolean reportFixture(Fixture fixture) {
                result.add(fixture.getBody());
                return true;
            }
        }, pos.x - radius*0.5f, pos.y - radius*0.5f, pos.x + radius*0.5f, pos.y + radius*0.5f);
        return result;
    }
}

class ClosestBodyQueryCallback implements QueryCallback {
    Body body;
    private final Vector2 pos;
    private final Body exclude;
    private final boolean onlyStatic;

    public ClosestBodyQueryCallback(Vector2 pos, Body exclude, boolean onlyStatic) {
        this.pos = pos;
        this.exclude = exclude;
        this.onlyStatic = onlyStatic;
    }

    @Override
    public boolean reportFixture(Fixture fixture) {
        if (!fixture.getBody().equals(exclude) && !(fixture.getFilterData().maskBits == 0) &&
                !(onlyStatic && fixture.getBody().getType() != BodyDef.BodyType.StaticBody) &&
                !fixture.isSensor() &&
                (body == null || fixture.getBody().getPosition().dst(pos) < body.getPosition().dst(pos))) {
            body = fixture.getBody();
        }

        return true;
    }
}
