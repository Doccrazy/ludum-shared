package de.doccrazy.shared.game.base;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;

public class KeyboardMovementListener extends InputListener implements MovementInputListener {
    private Vector2 m1 = new Vector2(0, 0), m2 = new Vector2(0, 0);
    private boolean jump;
    private Vector2 move = new Vector2();

    @Override
	public boolean keyDown(InputEvent event, int keycode) {
		if (Keys.UP == keycode || Keys.W == keycode) {
			m1.y = 1;
			return true;
		}
		if (Keys.DOWN == keycode || Keys.S == keycode) {
			m2.y = 1;
			return true;
		}
		if (Keys.LEFT == keycode || Keys.A == keycode) {
			m2.x = 1;
			return true;
		}
		if (Keys.RIGHT == keycode || Keys.D == keycode) {
			m1.x = 1;
			return true;
		}
		if (Keys.SPACE == keycode) {
			jump = true;
			return true;
		}
		return false;
	}

	@Override
	public boolean keyUp(InputEvent event, int keycode) {
		if (Keys.UP == keycode || Keys.W == keycode) {
			m1.y = 0;
			return true;
		}
		if (Keys.DOWN == keycode || Keys.S == keycode) {
			m2.y = 0;
			return true;
		}
		if (Keys.LEFT == keycode || Keys.A == keycode) {
			m2.x = 0;
			return true;
		}
		if (Keys.RIGHT == keycode || Keys.D == keycode) {
			m1.x = 0;
			return true;
		}
		if (Keys.SPACE == keycode) {
			jump = false;
			return true;
		}
		return false;
	}

	@Override
	public Vector2 getMovement() {
		move.set(m1);
		move.sub(m2);
		return move;
	}

	@Override
	public boolean isJump() {
		return jump;
	}

	@Override
	public boolean pollJump() {
		if (jump) {
			jump = false;
			return true;
		}
		return false;
	}


}
