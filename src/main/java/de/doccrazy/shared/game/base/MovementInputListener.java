package de.doccrazy.shared.game.base;

import com.badlogic.gdx.math.Vector2;

public interface MovementInputListener {
	Vector2 getMovement();

	boolean isJump();

	boolean pollJump();
}
