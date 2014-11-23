package de.doccrazy.shared.game.base;

import de.doccrazy.shared.game.actor.WorldActor;

public interface ActorListener {
	void actorAdded(WorldActor actor);

	void actorRemoved(WorldActor actor);
}
