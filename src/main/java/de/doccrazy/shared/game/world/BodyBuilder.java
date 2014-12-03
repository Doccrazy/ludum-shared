package de.doccrazy.shared.game.world;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class BodyBuilder {
    private final BodyDef bodyDef = new BodyDef();
    private final List<FixtureTemplate> fixtures = new ArrayList<>();

    private BodyBuilder(Vector2 position) {
        bodyDef.position.set(position);

        bodyDef.linearDamping = 0.1f;
        bodyDef.angularDamping = 0.8f;
    }

    /* *******************************************************************
     * Factory methods
     ******************************************************************* */

    public static BodyBuilder forDynamic(Vector2 position) {
        BodyBuilder builder = new BodyBuilder(position);
        builder.bodyDef.type = BodyDef.BodyType.DynamicBody;
        builder.newFixture();
        return builder;
    }

    public static BodyBuilder forStatic(Vector2 position) {
        BodyBuilder builder = new BodyBuilder(position);
        builder.bodyDef.type = BodyDef.BodyType.StaticBody;
        builder.newFixture();
        return builder;
    }

    /* *******************************************************************
     * Body attributes
     ******************************************************************* */

    public BodyBuilder velocity(Vector2 linear) {
        return velocity(linear, bodyDef.angularVelocity);
    }

    public BodyBuilder velocity(Vector2 linear, float angular) {
        bodyDef.linearVelocity.set(linear);
        bodyDef.angularVelocity = angular;
        return this;
    }

    public BodyBuilder rotation(float angleRads) {
        bodyDef.angle = angleRads;
        return this;
    }

    public BodyBuilder rotationDeg(float angleDegs) {
        return rotation(MathUtils.degreesToRadians * angleDegs);
    }

    /**
     * @param linear Linear damping is use to reduce the linear velocity.
     * @param angular Angular damping is use to reduce the angular velocity.
     */
    public BodyBuilder damping(float linear, float angular) {
        bodyDef.linearDamping = linear;
        bodyDef.angularDamping = angular;
        return this;
    }

    /** Is this a fast moving body that should be prevented from tunneling through other moving bodies? Note that all bodies are
     * prevented from tunneling through kinematic and static bodies. This setting is only considered on dynamic bodies.
     * @warning You should use this flag sparingly since it increases processing time. */
    public BodyBuilder asBullet() {
        bodyDef.bullet = true;
        return this;
    }

    /** Prevent this body from rotating. Useful for characters. */
    public BodyBuilder noRotate() {
        bodyDef.fixedRotation = true;
        return this;
    }

    /* *******************************************************************
     * Fixture definition and attributes
     ******************************************************************* */

    public BodyBuilder newFixture() {
        fixtures.add(new FixtureTemplate());
        if (bodyDef.type == BodyDef.BodyType.StaticBody) {
            fixtures.get(fixtures.size()-1).fixtureDef.density = 0;
        }
        return this;
    }

    /**
     * @param friction The friction coefficient, usually in the range [0,1].
     * @param restitution The restitution (elasticity) usually in the range [0,1].
     * @param density The density, usually in kg/m^2.
     */
    public BodyBuilder fixProps(float friction, float restitution, float density) {
        fixtures.get(fixtures.size()-1).fixtureDef.friction = friction;
        fixtures.get(fixtures.size()-1).fixtureDef.restitution = restitution;
        fixtures.get(fixtures.size()-1).fixtureDef.density = density;
        return this;
    }

    public BodyBuilder fixShape(ShapeBuilder shape) {
        fixtures.get(fixtures.size()-1).fixtureDef.shape = shape.build();
        return this;
    }

    /**
     * Set custom function to attach the current fixture to the body.
     * Must call body.createFixture(fixtureDef) at some point.
     * You may also choose to ignore the passed FixtureDef and build your own.
     */
    public BodyBuilder fixAttachFunc(BiConsumer<Body, FixtureDef> attachFixtureFunc) {
        fixtures.get(fixtures.size()-1).attachFixtureFunc = attachFixtureFunc;
        return this;
    }

    /** A sensor shape collects contact information but never generates a collision response. */
    public BodyBuilder fixSensor() {
        fixtures.get(fixtures.size()-1).fixtureDef.isSensor = true;
        return this;
    }

    /* *******************************************************************
     * Builder method
     ******************************************************************* */

    public Body build(Box2dWorld world) {
        Body body = world.box2dWorld.createBody(bodyDef);
        for (FixtureTemplate f : fixtures) {
            f.attachFixtureFunc.accept(body, f.fixtureDef);
            //may be null if a custom attach function is used
            if (f.fixtureDef.shape != null) {
                f.fixtureDef.shape.dispose();
            }
        }
        return body;
    }
}

class FixtureTemplate {
    FixtureDef fixtureDef = new FixtureDef();
    BiConsumer<Body, FixtureDef> attachFixtureFunc = FixtureTemplate::defaultAttach;

    public FixtureTemplate() {
        fixtureDef.friction = 3f;
        fixtureDef.restitution = 0.1f;
        fixtureDef.density = 1;
    }

    private static void defaultAttach(Body body, FixtureDef fixtureDef) {
        if (fixtureDef.shape == null) {
            throw new IllegalArgumentException("Fixture must have a shape (must call fixShape)");
        }
        body.createFixture(fixtureDef);
    }
}
