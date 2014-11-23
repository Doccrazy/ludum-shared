package de.doccrazy.shared.game.base;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

public class ActorContactListener implements ContactListener {
	public ActorContactListener() {
	}

	@Override
	public void beginContact(Contact contact) {
	    Body a = contact.getFixtureA().getBody();
		Body b = contact.getFixtureB().getBody();

		Vector2 normal = contact.getWorldManifold().getNormal();
		Vector2 contactPoint = contact.getWorldManifold().getPoints()[0];
		if (a.getUserData() instanceof CollisionListener) {
			((CollisionListener)a.getUserData()).beginContact(a, b, normal, contactPoint);
		}
		if (b.getUserData() instanceof CollisionListener) {
			((CollisionListener)b.getUserData()).beginContact(b, a, normal, contactPoint);
		}
	}

	@Override
	public void endContact(Contact contact) {
		if (contact.getFixtureA() == null || contact.getFixtureB() == null) {
			return;
		}
		Body a = contact.getFixtureA().getBody();
		Body b = contact.getFixtureB().getBody();
		if (a.getUserData() instanceof CollisionListener) {
			((CollisionListener)a.getUserData()).endContact(b);
		}
		if (b.getUserData() instanceof CollisionListener) {
			((CollisionListener)b.getUserData()).endContact(a);
		}
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
        float[] forces = impulse.getNormalImpulses();
        float force = 0;
        for (float f : forces) {
            force = Math.max(force, f);
        }

        Body a = contact.getFixtureA().getBody();
        Body b = contact.getFixtureB().getBody();
        if (a.getUserData() instanceof CollisionListener) {
            ((CollisionListener)a.getUserData()).hit(force);
        }
        if (b.getUserData() instanceof CollisionListener) {
            ((CollisionListener)b.getUserData()).hit(force);
        }
	}

}
