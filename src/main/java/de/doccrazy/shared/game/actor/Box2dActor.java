package de.doccrazy.shared.game.actor;

import box2dLight.Light;
import com.badlogic.gdx.physics.box2d.Body;
import de.doccrazy.shared.game.world.Box2dWorld;

import java.util.ArrayList;
import java.util.List;

public abstract class Box2dActor extends WorldActor {
	protected Body body;
	protected List<Light> lights = new ArrayList<>();

	public Box2dActor(Box2dWorld world) {
		super(world);
	}

	public Body getBody() {
		return body;
	}

	@Override
	protected void doRemove() {
		for (Light light : lights) {
			light.remove();
		}
		if (body != null) {
			world.box2dWorld.destroyBody(body);
		}
		body = null;
		super.doRemove();
	}
}
