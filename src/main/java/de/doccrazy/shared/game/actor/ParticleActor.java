package de.doccrazy.shared.game.actor;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool.PooledEffect;
import com.badlogic.gdx.utils.Array;

import de.doccrazy.shared.game.world.Box2dWorld;

public class ParticleActor extends WorldActor {
	private Array<PooledEffect> effects = new Array<>();

	public ParticleActor(Box2dWorld world) {
		super(world);
	}

	@Override
	protected void doAct(float delta) {
		world.pollEvents(ParticleEvent.class, this::addEvent);
		updateEffects(delta);
	}

	protected void addEvent(ParticleEvent event) {
		PooledEffect effect = event.getEffect().obtain();
		effect.setPosition(event.getX(), event.getY());
		effects.add(effect);
	}

	private void updateEffects(float delta) {
		// Update and draw effects:
		for (int i = effects.size - 1; i >= 0; i--) {
		    PooledEffect effect = effects.get(i);
		    effect.update(delta);
		    if (effect.isComplete()) {
		        effect.free();
		        effects.removeIndex(i);
		    }
		}
	}

	@Override
	public void draw(Batch batch, float parentAlpha) {
		for (PooledEffect effect : effects) {
		    effect.draw(batch);
		}
	}

	@Override
	public boolean isNoRemove() {
		return true;
	}
}
