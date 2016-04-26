package de.doccrazy.shared.game.actor;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
 * Determines if the attached {@link Box2dActor} is touching floor or walls by inspecting all contacts and
 * comparing contact normals. Requires circle-shaped body in sidescroller-like world.
 */
public class GroundContactAction extends Action {
    public static final float MAX_FLOOR_ANGLE = MathUtils.sinDeg(45);
    public static final float MAX_WALL_ANGLE = MathUtils.cosDeg(30);
    public static final float FLOOR_CONTACT_TTL = 0.2f;
    public static final float WALL_CONTACT_TTL = 0.25f;

    private World world;
    private float stateTime;
    private boolean touchingFloor, touchingLeftWall, touchingRightWall;
    private float lastFloorContact, lastLeftWallContact, lastRightWallContact;

    @Override
    public void setActor(Actor actor) {
        if (!(actor instanceof Box2dActor)) {
            throw new IllegalArgumentException("Expected Box2dActor");
        }
        world = ((Box2dActor<?>) actor).getWorld().box2dWorld;
        super.setActor(actor);
    }

    @Override
    public boolean act(float delta) {
        stateTime += delta;
        processContacts();
        return false;
    }

    private void processContacts() {
        for (Contact contact : world.getContactList()) {
            Body a = contact.getFixtureA().getBody();
            Body b = contact.getFixtureB().getBody();
            Body other = a.getUserData() == actor ? b : (b.getUserData() == actor ? a : null);
            if (other == null || other.getType() != BodyDef.BodyType.StaticBody || !contact.isTouching()) {
                continue;
            }

            Vector2 normal = contact.getWorldManifold().getNormal();
            if (normal.y > MAX_FLOOR_ANGLE) {
                touchingFloor = true;
                lastFloorContact = stateTime;
            } else if (normal.x > MAX_WALL_ANGLE) {
                touchingLeftWall = true;
                lastLeftWallContact = stateTime;
            } else if (normal.x < -MAX_WALL_ANGLE) {
                touchingRightWall = true;
                lastRightWallContact = stateTime;
            }
        }
        if (stateTime - lastFloorContact > FLOOR_CONTACT_TTL) {
            touchingFloor = false;
        }
        if (stateTime - lastLeftWallContact > WALL_CONTACT_TTL) {
            touchingLeftWall = false;
        }
        if (stateTime - lastRightWallContact > WALL_CONTACT_TTL) {
            touchingRightWall = false;
        }
        //System.out.println("Floor: " + touchingFloor + ", wallLeft: " + touchingLeftWall + ", wallRight: " + touchingRightWall);
    }

    public boolean isTouchingFloor() {
        return touchingFloor;
    }

    public boolean isTouchingLeftWall() {
        return touchingLeftWall;
    }

    public boolean isTouchingRightWall() {
        return touchingRightWall;
    }
}
