package de.doccrazy.shared.game.actor;

import java.util.function.Supplier;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
import com.brashmonkey.spriter.Entity;
import com.brashmonkey.spriter.Player;

import de.doccrazy.shared.game.world.Box2dWorld;
import de.doccrazy.shared.spriter.GdxDrawer;

public abstract class SpriterActor extends ShapeActor {
    protected final Player player;
    private final Supplier<GdxDrawer> drawerProvider;

    public SpriterActor(Box2dWorld world, Vector2 spawn, boolean spawnIsLeftBottom, Entity entity, Supplier<GdxDrawer> drawerProvider) {
        super(world, spawn, spawnIsLeftBottom);
        this.drawerProvider = drawerProvider;
        player = new Player(entity);
        //1 pixel in Spriter <-> 1cm in world coordinates
        player.setScale(0.01f);
    }

    @Override
    protected void doAct(float delta) {
        super.doAct(delta);
        player.speed = (int) (delta * 1000);
        player.update();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        updatePosition();
        applyClientTransform(batch, true);
        drawerProvider.get().withBatch(batch).draw(player);
        resetTransform(batch);
    }
}
