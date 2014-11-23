package de.doccrazy.shared.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import de.doccrazy.shared.game.world.Box2dWorld;

public abstract class ShapeActor extends Box2dActor {
    private boolean useRotation = true;

    public ShapeActor(Box2dWorld world, Vector2 spawn, boolean spawnIsLeftBottom) {
        super(world);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.x = spawn.x;
        bodyDef.position.y = spawn.y;
        bodyDef.linearDamping = 0.1f;
        bodyDef.angularDamping = 0.8f;

        customizeBody(bodyDef);

        this.body = world.box2dWorld.createBody(bodyDef);
        this.body.setUserData(this);

        FixtureDef fixDef = new FixtureDef();
        fixDef.friction = 3f;
        fixDef.restitution = 0.1f;
        fixDef.density = 1;

        createFixture(body, fixDef);

        //determine origin / size
        if (fixDef.shape instanceof CircleShape) {
            setOrigin(fixDef.shape.getRadius(), fixDef.shape.getRadius());
            setSize(fixDef.shape.getRadius() * 2f, fixDef.shape.getRadius() * 2f);
        } else if (fixDef.shape instanceof PolygonShape) {
            PolygonShape poly = (PolygonShape) fixDef.shape;
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

        fixDef.shape.dispose();
    }

    protected abstract void customizeBody(BodyDef bodyDef);

    /**
     * Attach a shape to the FixtureDef, then call body.createFixture(fixDef)
     */
    protected abstract void createFixture(Body body, FixtureDef fixtureDef);

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
