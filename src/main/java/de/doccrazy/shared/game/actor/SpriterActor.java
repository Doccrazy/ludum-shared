package de.doccrazy.shared.game.actor;

import java.util.function.Supplier;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.brashmonkey.spriter.Entity;
import com.brashmonkey.spriter.Player;
import com.brashmonkey.spriter.PlayerTweener;

import de.doccrazy.shared.game.world.Box2dWorld;
import de.doccrazy.shared.spriter.GdxDrawer;

public abstract class SpriterActor<T extends Box2dWorld> extends ShapeActor<T> {
    protected Player player;
    private final Supplier<GdxDrawer> drawerProvider;

    public SpriterActor(T world, Vector2 spawn, boolean spawnIsLeftBottom, Entity entity, Supplier<GdxDrawer> drawerProvider) {
        super(world, spawn, spawnIsLeftBottom);
        this.drawerProvider = drawerProvider;
        player = new Player(entity);
        //1 pixel in Spriter <-> 1cm in world coordinates
        player.setScale(0.01f);
        setUseRotation(false);
    }

    @Override
    protected void doAct(float delta) {
        super.doAct(delta);
        setSpeed(player, (int) (delta * 1000));
        player.update();
    }

    private void setSpeed(Player player2, int speed) {
    	player2.speed = speed;
        if (player2 instanceof PlayerTweener) {
        	setSpeed(((PlayerTweener) player2).getFirstPlayer(), speed);
        	setSpeed(((PlayerTweener) player2).getSecondPlayer(), speed);
        }
	}

    protected void scalePlayer(Player player2, float scale) {
    	player2.setScale(scale);
        if (player2 instanceof PlayerTweener) {
        	scalePlayer(((PlayerTweener) player2).getFirstPlayer(), scale);
        	scalePlayer(((PlayerTweener) player2).getSecondPlayer(), scale);
        }
    }

	@Override
    public void draw(Batch batch, float parentAlpha) {
        updatePosition();
        applyClientTransform(batch, true);
        drawerProvider.get().withBatch(batch).draw(player);
        resetTransform(batch);
    }
}
