package de.doccrazy.shared.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;

import de.doccrazy.shared.game.world.BodyBuilder;
import de.doccrazy.shared.game.world.Box2dWorld;

public abstract class ShapeActor extends Box2dActor {
    private boolean useRotation = true;

    public ShapeActor(Box2dWorld world, Vector2 spawn, boolean spawnIsLeftBottom) {
        super(world);

        BodyBuilder builder = createBody(spawn);
        this.body = builder.build(world);
        this.body.setUserData(this);

        Shape shape = body.getFixtureList().get(0).getShape();
        //determine origin / size
        if (shape instanceof CircleShape) {
            setOrigin(shape.getRadius(), shape.getRadius());
            setSize(shape.getRadius() * 2f, shape.getRadius() * 2f);
        } else if (shape instanceof PolygonShape) {
            PolygonShape poly = (PolygonShape) shape;
            Rectangle bb = null;
            Vector2 vert = new Vector2();
            for (int i = 0; i < poly.getVertexCount(); i++) {
                poly.getVertex(i, vert);
                if (bb == null) {
                    bb = new Rectangle(vert.x, vert.y, 0, 0);
                } else {
                    bb.merge(vert);
                }
            }
            setOrigin(-bb.x, -bb.y);
            setSize(bb.width, bb.height);
        } else {
            throw new IllegalArgumentException("Need shape");
        }

        if (spawnIsLeftBottom) {
            body.setTransform(spawn.x + getOriginX(), spawn.y + getOriginY(), 0);
        }
    }

    /**
     * You should create a body with a single fixture. The fixture will be used to determine
     * the Actor's origin and size.
     */
    protected abstract BodyBuilder createBody(Vector2 spawn);

    /**
     * If set to false, the shape's rotation from Box2d will not be mapped to the actor
     */
    protected void setUseRotation(boolean useRotation) {
        this.useRotation = useRotation;
    }

    @Override
    protected void doAct(float delta) {
        updatePosition();
    }

    /**
     * Call this before drawing
     */
    protected void updatePosition() {
        Vector2 pos = body.getPosition();
        setPosition(pos.x - getOriginX(), pos.y - getOriginY());
        if (useRotation) {
            setRotation(MathUtils.radiansToDegrees * body.getAngle());
        }
    }

    protected void drawRegion(Batch batch, TextureRegion region) {
        batch.draw(region, getX(), getY(), getOriginX(), getOriginY(),
                getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());
    }
}
