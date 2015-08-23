package de.doccrazy.shared.game.actor;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.physics.box2d.Body;

import box2dLight.Light;
import de.doccrazy.shared.game.world.Box2dWorld;

public abstract class Box2dActor<T extends Box2dWorld> extends WorldActor<T> {
	protected Body body;
	protected List<Light> lights = new ArrayList<>();

	public Box2dActor(T world) {
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
