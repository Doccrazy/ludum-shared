package de.doccrazy.shared.game.base;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerAdapter;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector2;

public class GamepadMovementListener extends ControllerAdapter implements MovementInputListener {
    private boolean jump;
    private Vector2 move = new Vector2();
    private int jumpButton;

    public GamepadMovementListener(int jumpButton) {
		this.jumpButton = jumpButton;
    }

    @Override
    public boolean buttonDown(Controller controller, int buttonIndex) {
    	if (buttonIndex == jumpButton) {
    		jump = true;
    		return true;
    	}
    	return false;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonIndex) {
    	if (buttonIndex == jumpButton) {
    		jump = false;
    		return true;
    	}
    	return false;
    }

    @Override
    public boolean povMoved(Controller controller, int povIndex, PovDirection value) {
    	switch (value) {
		case center:
			move.set(0, 0);
			break;
		case east:
			move.set(1, 0);
			break;
		case north:
			move.set(0, 1);
			break;
		case northEast:
			move.set(1, 1);
			break;
		case northWest:
			move.set(-1, 1);
			break;
		case south:
			move.set(0, -1);
			break;
		case southEast:
			move.set(1, -1);
			break;
		case southWest:
			move.set(-1, -1);
			break;
		case west:
			move.set(-1, 0);
			break;
		default:
			break;
    	}
    	return true;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisIndex, float value) {
    	if (axisIndex == 1 || axisIndex == 3) {
    		move.x = Math.abs(value) > 0.2 ? value : 0;
    		return true;
    	}
    	return false;
    }

	@Override
	public Vector2 getMovement() {
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
