package de.doccrazy.shared.game.actor;

import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;

import de.doccrazy.shared.game.event.Event;

public class ParticleEvent extends Event {
	private ParticleEffectPool effect;

	public ParticleEvent(float x, float y, ParticleEffectPool effect) {
		super(x, y);
		this.effect = effect;
	}

	public ParticleEffectPool getEffect() {
		return effect;
	}

}
