package de.doccrazy.shared.game.base;

import de.doccrazy.shared.game.actor.WorldActor;
import de.doccrazy.shared.game.world.Box2dWorld;

public interface ActorListener<T extends Box2dWorld<T>> {
	void actorAdded(WorldActor<T> actor);

	void actorRemoved(WorldActor<T> actor);
}
